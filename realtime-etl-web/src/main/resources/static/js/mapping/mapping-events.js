/**
 * Sync Event DetailsPage（FromMonitor queryData）
 */
(function (window) {
    'use strict';

    function getPageAttr(name) {
        var root = document.getElementById('mappingEventsPage');
        return root ? (root.getAttribute(name) || '') : '';
    }

    function showMessageDetail($message, icon, title) {
        $message.unbind('click').bind('click', function () {
            var content = $(this).text();
            if (content && content.trim()) {
                showConfirm({
                    title: title,
                    icon: icon,
                    size: 'max',
                    message: content
                });
            }
        });
    }

    function showRetryDetail(taskId, messageId) {
        doLoader('/monitor/page/retry?taskId=' + encodeURIComponent(taskId)
            + '&messageId=' + encodeURIComponent(messageId));
    }

    function jsonToTable(jsonObj) {
        var $content = '<table class="table">';
        $content += '<thead><tr><th></th><th>Fields</th><th>Value</th></tr></thead>';
        $content += '<tbody>';
        var index = 1;
        $.each(jsonObj, function (name, value) {
            $content += '<tr>';
            $content += '<td>' + index + '</td>';
            $content += '<td>' + escapeHtml(name || '') + '</td>';
            $content += '<td class="white-space-none">' + escapeHtml(value || '') + '</td>';
            $content += '</tr>';
            index++;
        });
        $content += '</tbody>';
        $content += '</table>';
        return $content;
    }

    function columnsToTable(columns) {
        var $content = '<table class="table">';
        $content += '<thead><tr><th></th><th>Fields</th><th>Type</th><th>Value</th></tr></thead>';
        $content += '<tbody>';
        if (!columns || !columns.length) {
            $content += '<tr><td colspan="4" class="text-center text-gray-400">Field data not available</td></tr>';
        } else {
            columns.forEach(function (col, i) {
                var val = col.value;
                if (val === null || val === undefined) {
                    val = 'NULL';
                }
                $content += '<tr>';
                $content += '<td>' + (i + 1) + '</td>';
                $content += '<td>' + escapeHtml(col.key || '') + '</td>';
                $content += '<td>' + escapeHtml(col.keyType || '') + '</td>';
                $content += '<td class="white-space-none">' + escapeHtml(String(val)) + '</td>';
                $content += '</tr>';
            });
        }
        $content += '</tbody></table>';
        return $content;
    }

    window.showMappingDataDetail = function (taskId, messageId) {
        doPoster('/monitor/getDataDetail', {taskId: taskId, messageId: messageId}, function (response) {
            if (!response.success) {
                bootGrowl(response.message || 'Failed to load data', 'danger');
                return;
            }
            var message = response.data || {};
            var body;
            if (message.columns && message.columns.length) {
                body = columnsToTable(message.columns);
            } else {
                body = jsonToTable({});
            }
            showConfirm({
                title: 'Data details',
                icon: 'info',
                size: 'max',
                body: body,
                confirmText: 'Close',
                confirmType: 'primary'
            });
        });
    };

    function renderDataState(success) {
        var state = {
            0: {class: 'badge-error', text: 'Failed'},
            1: {class: 'badge-success', text: 'Success'}
        };
        var config = state[success] || {class: 'badge-info', text: '-'};
        return '<span class="badge ' + config.class + '">' + config.text + '</span>';
    }

    function initMappingEventsPage() {
        var mappingId = getPageAttr('data-mapping-id');
        var tableGroupId = getPageAttr('data-table-group-id');
        var statusSelect;
        var pagination;

        window.backDetailPage = function () {
            doLoader('/mapping/page/detail?id=' + encodeURIComponent(mappingId));
        };

        function params() {
            return {
                id: mappingId || '',
                status: (statusSelect && statusSelect.getValues()[0]) || '',
                tableGroupId: tableGroupId || ''
            };
        }

        function search() {
            pagination.doSearch(params());
        }

        statusSelect = $('#searchDataStatus').dbSelect({
            type: 'single',
            onSelect: search
        });

        function renderDataButton(row) {
            var content = [];
            content.push('<button class = "table-action-btn view" title = onclick = showMappingDataDetail'
                + mappingId + '\',\'' + row.id + '\')"><i class="fa fa-eye"></i></button>');
            if (row.success === 0) {
                content.push('♪ Button class ♪'
                    + mappingId + '\',\'' + row.id + '\')"><i class="fa fa-refresh"></i></button>');
            }
            return content.join(' ');
        }

        window.showRetryDetail = showRetryDetail;

        pagination = new PaginationManager({
            requestUrl: '/monitor/queryData',
            tableBodySelector: '#dataTableBody',
            params: params(),
            pageSize: 10,
            customPageSize: true,
            customPageSizeItems: [5, 10, 20, 50, 100],
            storageKey: 'mapping-events',
            showBoundaryButtons: true,
            renderRow: function (d, index) {
                return '<tr>'
                    + '<td>' + index + '</td>'
                    + '<td>' + escapeHtml(d.targetTableName || '') + '</td>'
                    + '<td>' + escapeHtml(d.event || '') + '</td>'
                    + '<td>' + renderDataState(d.success) + '</td>'
                    + '<td><span class="hover-underline cursor-pointer data-error">'
                    + escapeHtml(d.error || '') + '</span></td>'
                    + '<td>' + formatDate(d.createTime) + '</td>'
                    + '<td><div class="flex items-center">' + renderDataButton(d) + '</div></td>'
                    + '</tr>';
            },
            refreshCompleted: function () {
                showMessageDetail($('.data-error'), 'warning', 'Organisation');
            },
            emptyHtml: '<td colspan="7" class="text-center"><i class="fa fa-exchange empty-icon"></i>'
                + '<p class="empty-text">Data not available</p></td>'
        });

        $('#clearDataBtn').unbind('click').bind('click', function () {
            showConfirm({
                title: 'Confirm to empty current table data?',
                icon: 'warning',
                size: 'large',
                confirmType: 'danger',
                onConfirm: function () {
                    doPoster('/monitor/clearData', {
                        id: mappingId || '',
                        tableGroupId: tableGroupId || ''
                    }, function (response) {
                        if (response.success) {
                            bootGrowl(response.data || 'Clear data!', 'success');
                            search();
                        } else {
                            bootGrowl('Could not close temporary folder: %s ' + response.message, 'danger');
                        }
                    });
                }
            });
        });
    }

    $(document).ready(initMappingEventsPage);
})(window);
