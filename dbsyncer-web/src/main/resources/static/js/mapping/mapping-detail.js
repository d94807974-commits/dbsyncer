/**
 * Sync taskResultDetailsPage（Map summary)
 */
(function (window) {
    'use strict';

    function getMappingId() {
        var root = document.getElementById('mappingDetailPage');
        return root ? (root.getAttribute('data-mapping-id') || '') : '';
    }

    function formatFailTotal(fail) {
        var n = Number(fail) || 0;
        if (n > 0) {
            return '<span class="badge badge-error">' + n + '</span>';
        }
        return '<span class="badge badge-success">0</span>';
    }

    function initMappingDetailPage() {
        window.backIndexPage = function () {
            doLoader('/mapping/list');
        };

        var currentMappingId = getMappingId();

        function getFilterValue(selector) {
            var api = $(selector).data('dbSelect');
            if (api && typeof api.getValues === 'function' && api.getValues().length > 0) {
                return api.getValues()[0] || '';
            }
            return $(selector).val() || '';
        }

        function buildSearchParams() {
            return {
                id: currentMappingId,
                detailStatus: getFilterValue('#detailStatusFilter')
            };
        }

        function triggerSearch() {
            resultPagination.doSearch(buildSearchParams(), 1);
        }

        $('#detailStatusFilter').dbSelect({
            type: 'single',
            onSelect: function () {
                triggerSearch();
            }
        });

        window.openMappingEvents = function (tableGroupId) {
            var statusParam = '';
            var filter = getFilterValue('#detailStatusFilter');
            if (filter === 'fail') {
                statusParam = '&status=0';
            } else if (filter === 'success') {
                statusParam = '&status=1';
            }
            doLoader('/mapping/page/events?id=' + encodeURIComponent(currentMappingId)
                + '&tableGroupId=' + encodeURIComponent(tableGroupId || '')
                + statusParam);
        };

        var resultPagination = new PaginationManager({
            requestUrl: '/mapping/searchResult',
            tableBodySelector: '#detail-result-table',
            params: buildSearchParams(),
            pageSize: 10,
            customPageSize: true,
            customPageSizeItems: [5, 10, 20, 50, 100, 200],
            storageKey: 'mapping-detail',
            renderRow: function (row, index) {
                var tableGroupId = row.tableGroupId != null ? String(row.tableGroupId) : '';
                return '<tr>'
                    + '<td>' + index + '</td>'
                    + '<td>' + escapeHtml(row.sourceTable || '-') + '</td>'
                    + '<td>' + escapeHtml(row.targetTable || '-') + '</td>'
                    + '<td>' + (row.successTotal != null ? row.successTotal : 0) + '</td>'
                    + '<td>' + formatFailTotal(row.failTotal) + '</td>'
                    + '<td>' + formatDate(row.updateTime || '') + '</td>'
                    + '<td>== sync, corrected by elderman == @elder man'
                    + ' onclick="openMappingEvents(\'' + tableGroupId.replace(/'/g, '') + '\')">'
                    + '<i class="fa fa-eye"></i></button></td>'
                    + '</tr>';
            },
            emptyHtml: '<td colspan="7" class="text-center"><i class="fa fa-exchange empty-icon"></i>'
                + '<p class="empty-text">Other Organiser</p>'
                + '<p class="empty-description">This is where you will display the maps.</p></td>'
        });
    }

    $(document).ready(initMappingDetailPage);
})(window);
