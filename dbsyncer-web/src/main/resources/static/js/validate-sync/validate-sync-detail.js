/**
 * RevisedVerify results detailsPage
 */
(function (window) {
    'use strict';

    function initValidateSyncDetailPage(config) {
        config = config || {};
        var currentTaskId = config.taskId || '';
        var currentDetailId = '';
        var manualReviseSubmitting = false;
        var diffModalInstance = null;
        var currentDetailContent = null;
        var $taskSelect = $('#detailTaskSelect');
        var defaultTaskId = currentTaskId || $taskSelect.attr('data-default-id') || '';
        var defaultTaskName = config.taskName || $taskSelect.attr('data-default-label') || defaultTaskId;

        window.backIndexPage = function () {
            doLoader('/validate-sync/list');
        };

        $taskSelect.dbSelect(buildRemotePagingSelectOptions({
            url: '/validate-sync/search',
            defaultValue: defaultTaskId,
            defaultLabel: defaultTaskName,
            onSelect: function (selected) {
                var selectedId = Array.isArray(selected) ? (selected[0] || '') : (selected.value || selected);
                if (selectedId && selectedId !== currentTaskId) {
                    currentTaskId = selectedId;
                    doLoader('/validate-sync/page/detail?id=' + currentTaskId);
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

        initFilterSelect('#detailStatusFilter');

        var typeTextMap = {
            rowData: 'Row data',
            tableSchema: 'Table schema',
            index: 'Index'
        };
        var typeBadgeMap = {
            rowData: 'badge-primary',
            tableSchema: 'badge-info',
            index: 'badge-warning'
        };
        // and CommonTaskStatusEnum Alignment:0Not running / 1Running / 2Stopping / 3Completed
        var statusTextMap = {
            0: 'Not running',
            1: 'Running',
            2: 'Stopping',
            3: 'Completed'
        };
        var statusBadgeMap = {
            0: 'badge-info',
            1: 'badge-success',
            2: 'badge-warning',
            3: 'badge-info'
        };

        function getStatusBadge(status) {
            var code = Number(status);
            var text = statusTextMap[code];
            if (!text) {
                return '<span class="text-gray-400">-</span>';
            }
            var badgeClass = statusBadgeMap[code] || 'badge-info';
            return '<span class="badge ' + badgeClass + '">' + text + '</span>';
        }

        function getSourceTargetCounts(row) {
            if (row.type !== 'rowData') {
                return '';
            }
            var sourceTotal = (row.sourceTotal === undefined || row.sourceTotal === null) ? '-' : row.sourceTotal;
            var targetTotal = (row.targetTotal === undefined || row.targetTotal === null) ? '-' : row.targetTotal;
            return '<span>Source:' + sourceTotal + '</span><br><span>Objectives:' + targetTotal + '</span>';
        }

        function getElapsedDuration(row) {
            var text = formatElapsedDuration(row.startTime, row.updateTime);
            if (!text) {
                return '';
            }
            return '<span class="text-tertiary">Time-consuming:' + escapeHtml(text) + '</span>';
        }

        function getExecutionDetail(row) {
            var parts = [getStatusBadge(row.status != null ? row.status : row.state)];
            var duration = getElapsedDuration(row);
            if (duration) {
                parts.push(duration);
            }
            var counts = getSourceTargetCounts(row);
            if (counts) {
                parts.push(counts);
            }
            return parts.join('<br>');
        }

        function parseContent(contentStr) {
            try {
                return typeof contentStr === 'string' ? JSON.parse(contentStr) : contentStr;
            } catch (e) {
                return null;
            }
        }

        function normalizeDiffList(content) {
            if (!content) {
                return [];
            }
            if (Array.isArray(content)) {
                return content;
            }
            return Array.isArray(content.data) ? content.data : [];
        }

        function getDiffTotal(row) {
            var n = Number(row.diffTotal) || 0;
            if (n > 0) {
                return '<span class="badge badge-error">' + n + '</span>';
            }
            return '<span class="badge badge-success">0</span>';
        }

        function getCorrectedTotal(row) {
            var total = Number(row.fixedTotal) || 0;
            if (total > 0) {
                return '<span class="badge badge-success">' + total + '</span>';
            }
            return '<span class="text-gray-400">0</span>';
        }

        function parseDiffDetails(details) {
            return Array.isArray(details) ? details : [];
        }

        function formatDiffValue(value) {
            if (value === null || value === undefined || value === '') {
                return 'NULL';
            }
            return String(value);
        }

        function buildCompareDiffTable(rows, options) {
            options = options || {};
            var firstHeader = options.firstHeader || 'Field';
            var firstKey = options.firstKey || 'field';
            var html = '<div class="table-inset"><table class="table table-wrap-cells">'
                + '<thead><tr>'
                + '<th class="width38p">' + escapeHtml(firstHeader) + '</th>'
                + '<th class="width31p">Source</th>'
                + '<th class="width31p">Objective</th>'
                + '</tr></thead><tbody>';
            rows.forEach(function (row) {
                var firstVal = row[firstKey];
                if (firstVal === undefined || firstVal === null || firstVal === '') {
                    firstVal = row.field || '-';
                }
                html += '<tr>'
                    + '<td class="font-medium">' + escapeHtml(firstVal) + '</td>'
                    + '<td class="text-primary">' + escapeHtml(formatDiffValue(row.source)) + '</td>'
                    + '<td class="text-error font-medium">' + escapeHtml(formatDiffValue(row.target)) + '</td>'
                    + '</tr>';
            });
            html += '</tbody></table></div>';
            return html;
        }

        function buildFieldDiffTable(details) {
            return buildCompareDiffTable(details, {firstHeader: 'Field', firstKey: 'field'});
        }

        function parseSchemaDiffFromMsg(msg) {
            if (!msg || typeof msg !== 'string') {
                return null;
            }
            var text = msg.trim();
            // EN patterns for localized messages; ZH kept for historical/SPI payloads still emitting Chinese
            var patterns = [
                {re: /^Type mismatch[，,]\s*source[：:]\s*(.+?)[，,]\s*target[：:]\s*(.+)$/i, label: 'Type'},
                {re: /^Length mismatch[，,]\s*source[：:]\s*(.+?)[，,]\s*target[：:]\s*(.+)$/i, label: 'Length'},
                {re: /^Precision mismatch[，,]\s*source[：:]\s*(.+?)[，,]\s*target[：:]\s*(.+)$/i, label: 'Precision'},
                {re: /^Primary key flag mismatch[，,]\s*source[：:]\s*(.+?)[，,]\s*target[：:]\s*(.+)$/i, label: 'Primary key'},
                {re: /^类型不一致[，,]\s*source[：:]\s*(.+?)[，,]\s*target[：:]\s*(.+)$/i, label: 'Type'},
                {re: /^长度不一致[，,]\s*source[：:]\s*(.+?)[，,]\s*target[：:]\s*(.+)$/i, label: 'Length'},
                {re: /^精度不一致[，,]\s*source[：:]\s*(.+?)[，,]\s*target[：:]\s*(.+)$/i, label: 'Precision'},
                {re: /^主键标记不一致[，,]\s*source[：:]\s*(.+?)[，,]\s*target[：:]\s*(.+)$/i, label: 'Primary key'}
            ];
            var i;
            for (i = 0; i < patterns.length; i++) {
                var m = text.match(patterns[i].re);
                if (m) {
                    return {label: patterns[i].label, source: m[1], target: m[2]};
                }
            }
            return null;
        }

        function formatDiffTypeText(type) {
            if (!type) {
                return '-';
            }
            return typeTextMap[type] || type;
        }

        function isSchemaDiffItem(item) {
            return item && item.type === 'tableSchema';
        }

        function buildDiffMsgCell(item) {
            var details = parseDiffDetails(item.details);
            var html = '<div class="table-cell-scroll">';
            if (details.length > 0) {
                html += buildFieldDiffTable(details);
            } else if (isSchemaDiffItem(item)) {
                var schemaRow = parseSchemaDiffFromMsg(item.msg);
                if (schemaRow) {
                    html += buildCompareDiffTable([schemaRow], {firstHeader: 'Variance', firstKey: 'label'});
                } else {
                    html += '<div class="text-secondary">' + escapeHtml(item.msg || '-') + '</div>';
                }
            } else {
                html += '<div class="text-secondary">' + escapeHtml(item.msg || '-') + '</div>';
            }
            html += '</div>';
            return html;
        }

        function resetDiffModalActions() {
            currentDetailId = '';
            currentDetailContent = null;
        }

        function closeDiffDetailModal() {
            if (diffModalInstance) {
                diffModalInstance.close();
                diffModalInstance = null;
            }
            resetDiffModalActions();
        }

        function getDiffDetailConfirmBtn() {
            if (!diffModalInstance) {
                return null;
            }
            var dialog = diffModalInstance.getDialog();
            return dialog ? dialog.querySelector('.confirm-btn-confirm') : null;
        }

        function resolveDiffFixedCounts(row) {
            return {
                diff: Number(row.diffTotal) || 0,
                fixed: Number(row.fixedTotal) || 0
            };
        }

        function buildDiffSummaryTitleExtra(src, tgt, counts) {
            function summaryItem(label, badgeClass, value) {
                return '<div class="flex items-center gap-2">'
                    + '<label class="form-label mb-0 text-secondary">' + label + '</label>'
                    + '<span class="badge ' + badgeClass + '">' + escapeHtml(String(value)) + '</span>'
                    + '</div>';
            }

            return '<div class="grid grid-cols-4 gap-2">'
                + summaryItem('Source table', 'badge-info', src)
                + summaryItem('Objective Combined', 'badge-info', tgt)
                + summaryItem('Variance', 'badge-error', counts.diff)
                + summaryItem('Revised', 'badge-success', counts.fixed)
                + '</div>';
        }

        function buildDiffDetailBodyHtml(data, tableNameOuter) {
            var html = '<div class="white-space-none">';
            var headHtml = '<tr>'
                + '<th class="width18p">Source Table Name</th>'
                + '<th class="width10p">Type</th>'
                + '<th class="width12p">Main key/identify</th>'
                + '<th class="width40p">Unusual</th></tr>';
            var bodyHtml = '';
            data.forEach(function (d) {
                var msgHtml = buildDiffMsgCell(d);
                var typeText = formatDiffTypeText(d.type);
                bodyHtml += '<tr>'
                    + '<td>' + escapeHtml(tableNameOuter) + '</td>'
                    + '<td>' + escapeHtml(typeText) + '</td>'
                    + '<td>' + escapeHtml(d.key || '-') + '</td>'
                    + '<td class="p-3">' + msgHtml + '</td>'
                    + '</tr>';
            });
            if (data.length === 0) {
                bodyHtml = '<tr><td colspan="4" class="text-center text-gray-400">No discrepancies</td></tr>';
            }
            html += '<table class="table table-wrap-cells"><thead>' + headHtml + '</thead><tbody>' + bodyHtml + '</tbody></table>';
            html += '</div>';
            return html;
        }

        function hasTargetExtraDiff(diffList) {
            return diffList.some(function (d) {
                return d && String(d.msg || '') === 'Target is redundant.';
            });
        }

        function hasNonTargetExtraDiff(diffList) {
            return diffList.some(function (d) {
                return d && String(d.msg || '') !== 'Target is redundant.';
            });
        }

        function openManualReviseConfirm() {
            if (!currentDetailId || manualReviseSubmitting) {
                return;
            }
            var hasDelete = hasTargetExtraDiff(currentDetailContent);
            var hasUpsert = hasNonTargetExtraDiff(currentDetailContent);
            var title = 'Are you sure about the manual revisions?';
            var body = '';
            var confirmType = 'primary';
            if (hasDelete && !hasUpsert) {
                title = 'Confirm reverse revisions?';
                body = 'Use the repository as the authoritative benchmark, remove redundant records that do not exist in the target library, and delete operations cannot be restored. Please confirm that the database is complete and continue.';
                confirmType = 'danger';
            } else if (hasDelete && hasUpsert) {
                body = 'GAS: Write source data to the target and delete redundant lines that do not exist in the target library.';
                confirmType = 'warning';
            }
            showConfirm({
                title: title,
                body: body,
                icon: 'warning',
                size: 'large',
                position: 'center',
                confirmType: confirmType,
                onConfirm: function () {
                    manualReviseSubmitting = true;
                    var confirmBtn = getDiffDetailConfirmBtn();
                    if (confirmBtn) {
                        confirmBtn.disabled = true;
                    }
                    doPoster('/validate-sync/manualRevise', {
                        taskId: currentTaskId,
                        id: currentDetailId
                    }, function (res) {
                        manualReviseSubmitting = false;
                        if (confirmBtn) {
                            confirmBtn.disabled = false;
                        }
                        if (!res.success) {
                            bootGrowl(res.message || 'Manual correction failed', 'danger');
                            return;
                        }
                        bootGrowl('Manual revision complete.', 'success');
                        renderDiffDetailModal(res.data || {});
                        resultPagination.doSearch(buildSearchParams());
                    });
                }
            });
        }

        function renderDiffDetailModal(row) {
            var content = parseContent(row.content);
            if (!content) {
                bootGrowl('No discrepancies!', 'warning');
                return;
            }
            var type = row.type || '';
            var data = normalizeDiffList(content);
            var tableNameOuter = row.sourceTableName || '-';
            var src = row.sourceTotal != null ? row.sourceTotal : 0;
            var tgt = row.targetTotal != null ? row.targetTotal : 0;
            var counts = resolveDiffFixedCounts(row);
            var pending = counts.diff > counts.fixed;

            closeDiffDetailModal();
            currentDetailId = row.id || '';
            currentDetailContent = data;

            diffModalInstance = showConfirm({
                title: 'Details of variances',
                titleExtra: type === 'rowData' ? buildDiffSummaryTitleExtra(src, tgt, counts) : '',
                icon: 'info',
                size: 'max',
                position: 'center',
                body: buildDiffDetailBodyHtml(data, tableNameOuter),
                confirmText: pending ? 'Manual revision (up to 100)' : 'Close',
                showCancel: pending,
                cancelText: 'Close',
                confirmType: 'primary',
                closeOnConfirm: !pending,
                onConfirm: function () {
                    if (pending) {
                        openManualReviseConfirm();
                        return;
                    }
                    diffModalInstance = null;
                    resetDiffModalActions();
                },
                onCancel: function () {
                    diffModalInstance = null;
                    resetDiffModalActions();
                }
            });
        }

        window.showDiffDetail = function (detailId) {
            if (!detailId) {
                return;
            }
            doPoster('/validate-sync/getResultDetail', {taskId: currentTaskId, id: detailId}, function (res) {
                if (!res.success) {
                    bootGrowl(res.message || 'Loading failed', 'danger');
                    return;
                }
                renderDiffDetailModal(res.data || {});
            });
        };

        $(document).off('click.validateSyncDiff', '.diff-detail-btn')
            .on('click.validateSyncDiff', '.diff-detail-btn', function () {
                var id = $(this).data('detail-id');
                if (id) {
                    window.showDiffDetail(String(id));
                }
            });

        var resultPagination = new PaginationManager({
            requestUrl: '/validate-sync/searchResult',
            tableBodySelector: '#detail-result-table',
            params: buildSearchParams(),
            renderRow: function (row, index) {
                var typeCode = row.type || '';
                var badgeClass = typeBadgeMap[typeCode] || 'badge-info';
                var typeText = typeTextMap[typeCode] || typeCode;
                var detailId = row.id != null ? row.id : '';
                return '<tr>'
                    + '<td>' + index + '</td>'
                    + '<td><span class="badge ' + badgeClass + '">' + escapeHtml(typeText) + '</span></td>'
                    + '<td>' + escapeHtml(row.sourceTableName || '-') + '</td>'
                    + '<td>' + escapeHtml(row.targetTableName || '-') + '</td>'
                    + '<td>' + getExecutionDetail(row) + '</td>'
                    + '<td>' + getDiffTotal(row) + '</td>'
                    + '<td>' + getCorrectedTotal(row) + '</td>'
                    + '<td>' + formatDate(row.updateTime || '') + '</td>'
                    + '<td><button type = "button" class = "table-action-btn view diff-detail-btn"'
                    + String(detailId).replace(/"/g, '') + '"><i class="fa fa-eye"></i></button></td>'
                    + '</tr>';
            },
            emptyHtml: '<td colspan="9" class="text-center"><i class="fa fa-file-text-o empty-icon"></i>'
                + '<p class="empty-text">There is no confirmation.</p><p class="empty-description">Please do the validation first.</p></td>',
            customPageSize: true
        });

        // Register to GlobalScheduledRefreshManager (maintenance current)PageandFilterCondition）
        PageRefreshManager.register(function () {
            resultPagination.doSearch(buildSearchParams(), resultPagination.currentPage);
        });
    }

    window.initValidateSyncDetailPage = initValidateSyncDetailPage;
})(window);
