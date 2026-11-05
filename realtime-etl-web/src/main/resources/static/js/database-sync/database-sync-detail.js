/**
 * FullLibraryDetails of migration resultsPage
 */
(function (window) {
    'use strict';

    var migrationDetailModal = null;

    function getTaskId() {
        var root = document.getElementById('databaseSyncerDetailPage');
        return root ? (root.getAttribute('data-task-id') || '') : '';
    }

    function formatSourceDb(row) {
        var db = row.sourceDatabase || '';
        var schema = row.sourceSchema || '';
        if (db && schema) {
            return escapeHtml(db) + ' / ' + escapeHtml(schema);
        }
        return escapeHtml(db || schema || '-');
    }

    function formatTargetDb(row) {
        var db = row.targetDatabase || '';
        var schema = row.targetSchema || '';
        if (db && schema) {
            return escapeHtml(db) + ' / ' + escapeHtml(schema);
        }
        return escapeHtml(db || schema || '-');
    }

    function formatFailTotal(fail) {
        var n = Number(fail) || 0;
        if (n > 0) {
            return '<span class="badge badge-error">' + n + '</span>';
        }
        return '<span class="badge badge-success">0</span>';
    }

    function formatPrimaryKey(primaryKey) {
        if (!primaryKey || typeof primaryKey !== 'object') {
            return '';
        }
        var pairs = [];
        Object.keys(primaryKey).forEach(function (name) {
            pairs.push(name + '=' + primaryKey[name]);
        });
        return pairs.join(',');
    }

    function parseMigrationDetailRows(row) {
        var typeCode = row.type || '';
        var content = row.content || '';
        var rows = [];

        if (content) {
            try {
                var detail = typeof content === 'string' ? JSON.parse(content) : content;
                if (detail && Array.isArray(detail.records)) {
                    detail.records.forEach(function (record) {
                        var key = record.identifier || formatPrimaryKey(record.primaryKey) || '-';
                        rows.push({
                            key: key,
                            error: record.error || '-'
                        });
                    });
                    return rows;
                }
            } catch (e) {
                // Compatible with old text formats
            }
        }

        if (typeCode === 'rowData') {
            if (!content) {
                return rows;
            }
            content.split('\n').forEach(function (line) {
                line = (line || '').trim();
                if (!line) {
                    return;
                }
                var errorIndex = line.indexOf(';error=');
                if (errorIndex >= 0) {
                    rows.push({
                        key: line.substring(0, errorIndex),
                        error: line.substring(errorIndex + 7)
                    });
                } else if (line.indexOf('error=') === 0) {
                    rows.push({ key: '-', error: line.substring(6) });
                } else {
                    rows.push({ key: '-', error: line });
                }
            });
            return rows;
        }

        rows.push({
            key: row.sourceTable || row.targetTable || '-',
            error: content || 'Migration successful'
        });
        return rows;
    }

    function buildMigrationDetailBodyHtml(row) {
        var typeCode = row.type || '';
        var isRowData = typeCode === 'rowData';
        var keyHeader = isRowData ? 'Primary key' : 'Table name';
        var detailRows = parseMigrationDetailRows(row);
        var bodyHtml = '';
        if (detailRows.length === 0) {
            var failHint = Number(row.failTotal) > 0
                ? 'Failure count exists but no persisted error samples (rerun historical tasks to view)'
                : 'No error details at this time';
            bodyHtml = '<tr><td colspan="2" class="text-center text-gray-400">' + failHint + '</td></tr>';
        } else {
            detailRows.forEach(function (item) {
                var errorText = item.error || '-';
                bodyHtml += '<tr>'
                    + '<td class="min-w-300">' + escapeHtml(item.key || '-') + '</td>'
                    + '<td><div class="table-cell-scroll text-sm">'
                    + escapeHtml(errorText) + '</div></td>'
                    + '</tr>';
            });
        }
        return '<table class="table">'
            + '<thead><tr><th>' + escapeHtml(keyHeader) + '</th><th class="width60p">Error message</th></tr></thead>'
            + '<tbody>' + bodyHtml + '</tbody>'
            + '</table>';
    }

    function closeMigrationDetailModal() {
        if (migrationDetailModal) {
            migrationDetailModal.close();
            migrationDetailModal = null;
        }
    }

    function showMigrationDetail(detailId, migrationDetailCache) {
        var row = migrationDetailCache[detailId];
        if (!row) {
            bootGrowl('Proposed data not found', 'warning');
            return;
        }

        var typeCode = row.type || '';
        var isRowData = typeCode === 'rowData';
        closeMigrationDetailModal();
        migrationDetailModal = showConfirm({
            title: isRowData ? 'Data migration details' : 'Structure migration details',
            icon: 'info',
            size: 'max',
            position: 'center',
            body: buildMigrationDetailBodyHtml(row),
            confirmText: 'Close',
            showCancel: false,
            onConfirm: function () {
                migrationDetailModal = null;
            }
        });
    }

    function initDatabaseSyncerDetailPage() {
        window.backIndexPage = function () {
            doLoader('/database-sync/list');
        };

        var currentTaskId = getTaskId();
        var migrationDetailCache = {};
        var $taskSelect = $('#detailTaskSelect');
        var defaultTaskId = currentTaskId || $taskSelect.attr('data-default-id') || '';
        var defaultTaskName = $taskSelect.attr('data-default-label') || defaultTaskId;

        $('#detailTaskSelect').dbSelect(buildRemotePagingSelectOptions({
            url: '/database-sync/search',
            defaultValue: defaultTaskId,
            defaultLabel: defaultTaskName,
            onSelect: function (selected) {
                var selectedId = Array.isArray(selected) ? (selected[0] || '') : (selected.value || selected);
                if (selectedId && selectedId !== currentTaskId) {
                    currentTaskId = selectedId;
                    doLoader('/database-sync/page/detail?id=' + currentTaskId);
                }
            }
        }));

        function getFilterValue(selector) {
            var api = $(selector).data('dbSelect');
            if (api && typeof api.getValues === 'function' && api.getValues().length > 0) {
                return api.getValues()[0] || '';
            }
            return $(selector).val() || '';
        }

        function buildSearchParams() {
            return {
                taskId: currentTaskId,
                detailType: getFilterValue('#detailTypeFilter'),
                detailStatus: getFilterValue('#detailStatusFilter')
            };
        }

        function triggerSearch() {
            resultPagination.doSearch(buildSearchParams(), 1);
        }

        function initFilterSelect(selector) {
            $(selector).dbSelect({
                type: 'single',
                onSelect: function () {
                    triggerSearch();
                }
            });
        }

        initFilterSelect('#detailTypeFilter');
        initFilterSelect('#detailStatusFilter');

        var typeTextMap = {
            rowData: 'Data Migration',
            tableSchema: 'Table schema'
        };
        var typeBadgeMap = {
            rowData: 'badge-primary',
            tableSchema: 'badge-info'
        };

        window.showMigrationDetail = function (detailId) {
            showMigrationDetail(detailId, migrationDetailCache);
        };

        var resultPagination = new PaginationManager({
            requestUrl: '/database-sync/searchResult',
            tableBodySelector: '#detail-result-table',
            params: buildSearchParams(),
            pageSize: 10,
            customPageSize: true,
            customPageSizeItems: [5, 10, 20, 50, 100, 200],
            storageKey: 'database-sync-detail',
            renderRow: function (row, index) {
                var typeCode = row.type || '';
                var badgeClass = typeBadgeMap[typeCode] || 'badge-info';
                var typeText = typeTextMap[typeCode] || typeCode;
                var sourceTotal = row.sourceTotal;
                if (sourceTotal === undefined || sourceTotal === null || sourceTotal === '') {
                    sourceTotal = typeCode === 'rowData' ? '-' : '<span class="text-gray-400">-</span>';
                }
                var detailId = row.id != null ? String(row.id) : ('detail_' + index);
                migrationDetailCache[detailId] = {
                    type: typeCode,
                    sourceTable: row.sourceTable || '',
                    targetTable: row.targetTable || '',
                    content: row.content || '',
                    failTotal: row.failTotal
                };
                return '<tr>'
                    + '<td>' + index + '</td>'
                    + '<td><span class="badge ' + badgeClass + '">' + escapeHtml(typeText) + '</span></td>'
                    + '<td>' + formatSourceDb(row) + '</td>'
                    + '<td>' + escapeHtml(row.sourceTable || '-') + '</td>'
                    + '<td>' + formatTargetDb(row) + '</td>'
                    + '<td>' + escapeHtml(row.targetTable || '-') + '</td>'
                    + '<td>' + sourceTotal + '</td>'
                    + '<td>' + (row.successTotal != null ? row.successTotal : '-') + '</td>'
                    + '<td>' + formatFailTotal(row.failTotal) + '</td>'
                    + '<td>' + formatDate(row.updateTime || '') + '</td>'
                    + '<td>{\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFFFF} {\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cH00FFFF} {\cHFFFFFF} {\cHFFFFFF} {\cHFFFFFF} {\cHFFFFFF}{\cH00} {\cH000 {\cH00} {\cH3030303030D0} {\cH00}' + detailId.replace(/'/g, '') + '\')"><i class="fa fa-eye"></i></button></td>'
                    + '</tr>';
            },
            emptyHtml: '<td colspan="11" class="text-center"><i class="fa fa-database empty-icon"></i>'
                + '<p class="empty-text">No relocation results.</p>'
                + '<p class="empty-description">Once the assignment is completed, the tables will be shown here.</p></td>'
        });
    }

    $(document).ready(initDatabaseSyncerDetailPage);
})(window);
