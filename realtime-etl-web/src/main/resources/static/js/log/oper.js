(function () {
    'use strict';

    /** Date PROPOSALyyyy-MM-dd → When?days 00:00:00 ms */
    function toBeginMillis(value) {
        if (!value) {
            return '';
        }
        const t = new Date(value + 'T00:00:00').getTime();
        return isNaN(t) ? '' : t;
    }

    /** Date PROPOSALyyyy-MM-dd → When?days 23:59:59.999 ms */
    function toEndMillis(value) {
        if (!value) {
            return '';
        }
        const t = new Date(value + 'T23:59:59.999').getTime();
        return isNaN(t) ? '' : t;
    }

    function statusBadge(status) {
        if (Number(status) === 1) {
            return '<span class="badge badge-success">Success</span>';
        }
        return '<span class="badge badge-error">Failed</span>';
    }

    function formatJsonBlock(value) {
        if (value === undefined || value === null || value === '') {
            return 'None';
        }
        let text;
        if (typeof value === 'string') {
            text = value;
        } else {
            try {
                text = JSON.stringify(value, null, 2);
            } catch (e) {
                text = String(value);
            }
        }
        return '<pre class="code-block log-json-block"><code>' + escapeHtml(text) + '</code></pre>';
    }

    function getStatusFilter() {
        const vals = statusSelect && typeof statusSelect.getValues === 'function'
            ? statusSelect.getValues() : [];
        const v = vals.length ? vals[0] : ($('#filterStatus').val() || '');
        return v === '-1' || v === '' ? '' : v;
    }

    function getModuleFilter() {
        const vals = moduleSelect && typeof moduleSelect.getValues === 'function'
            ? moduleSelect.getValues() : [];
        return vals.length ? (vals[0] || '') : ($('#filterModule').val() || '');
    }

    function collectParams() {
        return {
            json: $('#filterKeyword').val() || '',
            username: $('#filterUsername').val() || '',
            ip: $('#filterIp').val() || '',
            module: getModuleFilter(),
            status: getStatusFilter(),
            beginTime: toBeginMillis($('#filterBeginTime').val()),
            endTime: toEndMillis($('#filterEndTime').val())
        };
    }

    function showDetail(id) {
        doPoster('/log/detail', {id: id}, function (res) {
            if (!res || res.success !== true) {
                bootGrowl((res && res.message) || 'Failed to search for details', 'danger');
                return;
            }
            const d = res.data || {};
            const body = ''
                + '<div class="form-item"><label class="form-label">Operation module</label><div class="form-control-area">'
                + escapeHtml(d.typeName || d.type || '') + '</div></div>'
                + '<div class="form-item mt-4"><label class="form-label">Login info</label><div class="form-control-area">'
                + escapeHtml((d.username || '') + (d.ip ? ' / ' + d.ip : '')) + '</div></div>'
                + '<div class="form-item mt-4"><label class="form-label">Request Address</label><div class="form-control-area">'
                + escapeHtml(d.requestUri || 'None') + '</div></div>'
                + '<div class="form-item mt-4"><label class="form-label">Request parameters</label><div class="form-control-area">'
                + formatJsonBlock(d.requestParam) + '</div></div>'
                + '<div class="form-item mt-4"><label class="form-label">shed</label><div class="form-control-area">'
                + formatJsonBlock(d.responseParam) + '</div></div>'
                + '<div class="form-item mt-4"><label class="form-label">Status / Time</label><div class="form-control-area">'
                + statusBadge(d.status) + ' &nbsp; ' + formatDate(d.createTime || '')
                + (d.costMs != null ? '(time-consuming) ' + d.costMs + 'ms）' : '')
                + '</div></div>';

            showConfirm({
                title: 'Operation log details',
                icon: 'info',
                size: 'xlarge',
                showCancel: false,
                confirmText: 'Close',
                body: body
            });
        });
    }

    const pagination = new PaginationManager({
        requestUrl: '/log/oper/query',
        tableBodySelector: '#operLogList',
        pageSize: 10,
        customPageSize: true,
        customPageSizeItems: [5, 10, 20, 50, 100],
        storageKey: 'log.oper',
        showBoundaryButtons: true,
        renderRow: function (row) {
            return '<tr>'
                + '<td>' + escapeHtml(row.typeName || row.type || '') + '</td>'
                + '<td class="white-space-none">' + (row.message || row.json || '') + '</td>'
                + '<td>' + (row.username || '') + '</td>'
                + '<td>' + (row.ip || '') + '</td>'
                + '<td>' + statusBadge(row.status) + '</td>'
                + '<td>' + formatDate(row.createTime || '') + '</td>'
                + '<td><button type="button" class="table-action-btn view log-detail-btn" data-id="'
                + escapeHtml(row.id || '') + '"title" is "detail."<i class="fa fa-eye"></i></button></td>'
                + '</tr>';
        },
        refreshCompleted: function () {
            $('#operLogList .log-detail-btn').off('click').on('click', function () {
                showDetail($(this).data('id'));
            });
        }
    });

    function search() {
        pagination.doSearch(collectParams(), 1);
    }

    const moduleSelect = $('#filterModule').dbSelect({
        type: 'single',
        onSelect: function () {
            search();
        }
    });
    const statusSelect = $('#filterStatus').dbSelect({
        type: 'single',
        onSelect: function () {
            search();
        }
    });

    $('#filterKeyword, #filterUsername, #filterIp').on('blur', search);
    $('#filterBeginTime, #filterEndTime').on('change', search);
    $('#resetBtn').on('click', function () {
        $('#filterKeyword').val('');
        $('#filterUsername').val('');
        $('#filterIp').val('');
        if (moduleSelect && typeof moduleSelect.setValues === 'function') {
            moduleSelect.setValues([''], true);
        } else {
            $('#filterModule').val('');
        }
        if (statusSelect && typeof statusSelect.setValues === 'function') {
            statusSelect.setValues(['-1'], true);
        } else {
            $('#filterStatus').val('-1');
        }
        $('#filterBeginTime').val('');
        $('#filterEndTime').val('');
        search();
    });
    $('#clearBtn').on('click', function () {
        showConfirm({
            title: 'Confirming to empty all logs?',
            icon: 'warning',
            size: 'large',
            confirmType: 'danger',
            onConfirm: function () {
                doPoster('/log/clear', {}, function (res) {
                    if (res && res.success) {
                        bootGrowl('Clear Log Success', 'success');
                        search();
                    } else {
                        bootGrowl((res && res.message) || 'Empty failed', 'danger');
                    }
                });
            }
        });
    });
})();
