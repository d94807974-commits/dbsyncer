/**
 * Cluster managementList
 */
(function (window) {
    'use strict';

    var charts = {
        queue: null,
        tps: null,
        workItem: null
    };

    var clusterEnabled = false;
    var pagination = null;
    var metricsByNodeId = {};

    function initClusterList(options) {
        options = options || {};
        clusterEnabled = options.clusterEnabled === true;
        metricsByNodeId = {};

        window.backIndexPage = function () {
            doLoader('/cluster/list');
        };

        bindClusterActions();
        destroyCharts();
        if (clusterEnabled) {
            initCharts();
        }

        pagination = new PaginationManager({
            requestUrl: '/cluster/query',
            tableBodySelector: '#clusterTableBody',
            renderRow: renderClusterRow,
            storageKey: 'cluster-list'
        });

        loadNodeMetrics(true);
        if (typeof PageRefreshManager !== 'undefined' && PageRefreshManager.register) {
            PageRefreshManager.register(function () {
                loadNodeMetrics(false);
            });
        }
    }

    function destroyCharts() {
        Object.keys(charts).forEach(function (key) {
            if (charts[key] && typeof charts[key].destroy === 'function') {
                charts[key].destroy();
            }
            charts[key] = null;
        });
    }

    function initLineChart(canvasId, label, color, solidFill) {
        if (typeof Chart === 'undefined') {
            return null;
        }
        var canvas = document.getElementById(canvasId);
        if (!canvas) {
            return null;
        }
        var bgColor = solidFill ? color.replace('1)', '0.6)') : color.replace('1)', '0.1)');
        return new Chart(canvas, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: label,
                    data: [],
                    borderColor: color,
                    backgroundColor: bgColor,
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4,
                    pointRadius: 0,
                    pointHoverRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    intersect: false,
                    mode: 'index'
                },
                scales: {
                    x: {
                        display: true,
                        grid: {display: false},
                        ticks: {maxTicksLimit: 6}
                    },
                    y: {
                        display: true,
                        beginAtZero: true,
                        grid: {color: 'rgba(0, 0, 0, 0.05)'}
                    }
                },
                plugins: {
                    legend: {display: false},
                    tooltip: {
                        enabled: true,
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        padding: 12
                    }
                }
            }
        });
    }

    function initCharts() {
        charts.queue = initLineChart('clusterQueueChart', 'Backlog', 'rgba(250, 173, 20, 1)', false);
        charts.tps = initLineChart('clusterTpsChart', 'TPS', 'rgba(245, 108, 108, 1)', false);
        charts.workItem = initLineChart('clusterWorkItemChart', 'Shard task', 'rgba(24, 144, 255, 1)', true);
    }

    function updateLineChart(chart, labels, data) {
        if (!chart) {
            return;
        }
        chart.data.labels = labels || [];
        chart.data.datasets[0].data = data || [];
        chart.update('none');
    }

    function toNumberList(values) {
        if (!values || !values.length) {
            return [];
        }
        return values.map(function (item) {
            return Number(item) || 0;
        });
    }

    function updateClusterCharts(overview) {
        if (!clusterEnabled || !overview) {
            return;
        }
        if (overview.tps) {
            updateLineChart(charts.tps, overview.tps.name || [], toNumberList(overview.tps.value));
            var avg = Number(overview.tps.average) || 0;
            $('#clusterTpsTitle').text(avg > 0 ? ('TPS, average:' + Math.floor(avg) + 'Second') : 'TPS');
        }
        if (overview.queue) {
            updateLineChart(charts.queue, overview.queue.name || [], toNumberList(overview.queue.value));
        }
        if (overview.fullWorkItems) {
            updateLineChart(charts.workItem, overview.fullWorkItems.name || [],
                toNumberList(overview.fullWorkItems.value));
        }
    }

    function bindClusterActions() {
        $('#clusterTableBody').on('click', '[data-action]', function () {
            var $btn = $(this);
            var id = $btn.attr('data-id');
            var action = $btn.attr('data-action');
            if (action === 'edit') {
                editNodeName(id);
            } else if (action === 'delete') {
                removeNode(id, $btn.attr('data-name') || id);
            } else if (action === 'recover') {
                forceExpireLeaderGracePeriod();
            }
        });
    }

    function metricOf(nodeId) {
        return metricsByNodeId[nodeId] || null;
    }

    function formatPercent(value) {
        if (value === null || value === undefined || value === '') {
            return '-';
        }
        return Number(value).toFixed(2).replace(/\.00$/, '') + '%';
    }

    function formatUsedTotal(used, total, suffix) {
        if (used === null || used === undefined || total === null || total === undefined) {
            return '-';
        }
        var u = Number(used);
        var t = Number(total);
        if (isNaN(u) || isNaN(t)) {
            return '-';
        }
        var unit = suffix || 'G';
        return trimNum(u) + unit + '/' + trimNum(t) + unit;
    }

    function trimNum(n) {
        if (Math.abs(n - Math.round(n)) < 0.05) {
            return String(Math.round(n));
        }
        return n.toFixed(1);
    }

    function formatDash(value) {
        if (value === null || value === undefined || value === '') {
            return '-';
        }
        return String(value);
    }

    function formatMetric(m, getter) {
        // I 61/1d it.reachable；local=true It should still be shown.
        if (!m || (!m.reachable && !m.local)) {
            return '-';
        }
        return getter(m);
    }

    /** status: 0-Offline；1-Online */
    function formatStatus(status) {
        if (Number(status) === 1) {
            return '<span class="badge badge-success">Online</span>';
        }
        return '<span class="badge badge-error">Offline</span>';
    }

    /** role: 0-Follower；1-Leader */
    function formatRole(role) {
        if (Number(role) === 1) {
            return '<span class="badge badge-info">Leader</span>';
        }
        return '<span class="text-secondary">Follower</span>';
    }

    function resolveNodeAddress(item) {
        if (!item) {
            return null;
        }
        var ip = item.ip;
        var port = Number(item.httpPort) || 0;
        if ((!ip || port <= 0) && item.nodeId) {
            var idx = String(item.nodeId).lastIndexOf(':');
            if (idx > 0) {
                if (!ip) {
                    ip = String(item.nodeId).substring(0, idx);
                }
                if (port <= 0) {
                    port = Number(String(item.nodeId).substring(idx + 1)) || 0;
                }
            }
        }
        if (!ip || port <= 0) {
            return null;
        }
        return {ip: ip, httpPort: port};
    }

    function buildSsoConsoleUrl(item) {
        var addr = resolveNodeAddress(item);
        if (!addr) {
            return '';
        }
        var target = addr.ip + ':' + addr.httpPort;
        return $basePath + '/sso/redirect?target=' + encodeURIComponent(target) + '&redirect=' + encodeURIComponent('/');
    }

    /** Suspended display full address: prioritynodeId，Nothenip:port */
    function resolveNodeEndpoint(item) {
        if (!item) {
            return '';
        }
        if (item.nodeId && String(item.nodeId).indexOf(':') > 0) {
            return String(item.nodeId);
        }
        var addr = resolveNodeAddress(item);
        return addr ? (addr.ip + ':' + addr.httpPort) : '';
    }

    /** Multiline Cells: Tabs+ Value */
    function stackCell(lines) {
        if (!lines || !lines.length) {
            return '-';
        }
        var html = ['<div class="flex flex-col white-space-none text-xs">'];
        lines.forEach(function (line, index) {
            var cls = index === 0 ? '' : ' class="mt-1"';
            html.push('<div' + cls + '><span class="text-tertiary">' + escapeHtml(line.label)
                + '</span> ' + line.value + '</div>');
        });
        html.push('</div>');
        return html.join('');
    }

    function renderClusterRow(item) {
        var nodeId = item.nodeId || '';
        var name = item.name || nodeId || '';
        var localMark = item.local ? ' (ON TV)' : '';
        var endpoint = resolveNodeEndpoint(item);
        var endpointTitle = endpoint ? ' title="' + escapeHtml(endpoint) + '"' : '';
        var m = metricOf(nodeId);
        var fullWorkItems = m ? formatDash(m.fullWorkItemCount) : '-';
        var incremental = m ? formatDash(m.incrementalCount) : '-';
        var tps = formatMetric(m, function (metric) {
            return formatDash(Math.floor(metric.tps || 0));
        });
        var queueUp = formatMetric(m, function (metric) {
            return formatDash(metric.queueUp);
        });
        var storageQueueUp = formatMetric(m, function (metric) {
            return formatDash(metric.storageQueueUp);
        });
        var cpu = (m && (m.reachable || m.local)) ? formatPercent(m.cpuPercent) : '-';
        var memory = (m && (m.reachable || m.local)) ? formatUsedTotal(m.memoryUsed, m.memoryTotal, 'G') : '-';
        var threads = (m && (m.reachable || m.local)) ? formatDash(m.threadLive) : '-';
        var disk = (m && (m.reachable || m.local)) ? formatUsedTotal(m.diskUsed, m.diskTotal, 'G') : '-';
        var pluginSize = formatMetric(m, function (metric) {
            return formatDash(metric.pluginSize);
        });
        var buttons = [];
        if (clusterEnabled) {
            var editId = escapeHtml(nodeId);
            buttons.push(
                '== sync, corrected by elderman == @elder man'
                + editId + '" data-action="edit"><i class="fa fa-pencil"></i></button>'
            );
            // OnlyOfflineNodesDeletedstatus: 0-Offline；1-Online）
            if (Number(item.status) === 0 && !item.local) {
                buttons.push(
                    '== sync, corrected by elderman == @elder man'
                    + editId + '" data-name="' + escapeHtml(name) + '" data-action="delete">'
                    + '<i class="fa fa-trash"></i></button>'
                );
            }
            // Protection period: on-site onlyLeader Line Display「Restore」（Schedule only byLeader Implementation)
            if (Number(item.gracePeriodSeconds) > 0) {
                buttons.push(
                    '<button type="button" class="table-action-btn play" title="Skip Node Restore" data-action="recover">'
                    + '<i class="fa fa-refresh"></i></button>'
                );
            }
        }
        var actions = buttons.length > 0
            ? '<div class="flex items-center">' + buttons.join('') + '</div>'
            : '-';
        var nameText = escapeHtml(name) + localMark;
        var nameHtml;
        if (clusterEnabled && !item.local) {
            var consoleUrl = buildSsoConsoleUrl(item);
            if (consoleUrl) {
                nameHtml = '<a class="text-primary hover-underline"' + endpointTitle
                    + ' href="' + consoleUrl + '">' + nameText + '</a>';
            } else {
                nameHtml = '<span' + endpointTitle + '>' + nameText + '</span>';
            }
        } else {
            nameHtml = '<span' + endpointTitle + '>' + nameText + '</span>';
        }
        var statusHtml = '<div class="flex flex-col white-space-none">'
            + '<div>' + formatRole(item.role) + '</div>'
            + '<div class="mt-1">' + formatStatus(item.status) + '</div>';
        if (Number(item.gracePeriodSeconds) > 0) {
            statusHtml += '<div class="mt-1 text-warning">Other Organiser' + Number(item.gracePeriodSeconds) + ' sec refund</div>';
        }
        statusHtml += '</div>';
        var timeHtml = stackCell([
            {label: 'Heart rate.', value: formatRelativeTime(item.heartbeatTime)},
            {label: 'Start', value: formatDate(item.startTime)}
        ]);
        var taskHtml = stackCell([
            {label: 'Shard task', value: escapeHtml(fullWorkItems)},
            {label: 'Incremental tasks', value: escapeHtml(incremental)}
        ]);
        var throughputHtml = stackCell([
            {label: 'TPS', value: escapeHtml(tps)},
            {label: 'Backlog', value: escapeHtml(queueUp)},
            {label: 'Enduring', value: escapeHtml(storageQueueUp)}
        ]);
        var resourceHtml = stackCell([
            {label: 'CPU', value: escapeHtml(cpu)},
            {label: 'Memory', value: escapeHtml(memory)},
            {label: 'Thread', value: escapeHtml(threads)},
            {label: 'Disk', value: escapeHtml(disk)}
        ]);
        return '<tr>'
            + '<td>' + nameHtml + '</td>'
            + '<td>' + statusHtml + '</td>'
            + '<td>' + timeHtml + '</td>'
            + '<td>' + taskHtml + '</td>'
            + '<td>' + throughputHtml + '</td>'
            + '<td>' + resourceHtml + '</td>'
            + '<td>' + escapeHtml(pluginSize) + '</td>'
            + '<td>' + actions + '</td>'
            + '</tr>';
    }

    function editNodeName(id) {
        if (!id) {
            return;
        }
        var metric = metricOf(id);
        var currentName = (metric && metric.name) ? metric.name : id;
        var inputId = 'clusterNodeNameInput';
        showConfirm({
            title: 'Edit Node',
            icon: 'info',
            confirmText: 'Save',
            body: '<div class="form-item mb-0">'
                + '<label class="form-label" for="' + inputId + '"\'The name of the node</label>'
                + '<div class="form-control-area">'
                + '<input type="text" id="' + inputId + '" class="form-control" maxlength="64"/>'
                + '</div></div>',
            onConfirm: function () {
                var $input = $('#' + inputId);
                var name = ($input.length ? $input.val() : '') || '';
                if (!String(name).trim()) {
                    bootGrowl('Node name cannot be empty', 'warning');
                    return;
                }
                doPoster('/cluster/edit', {id: id, name: String(name).trim()}, function (res) {
                    if (res.success === true) {
                        bootGrowl('Saved', 'success');
                        loadNodeMetrics(false);
                    } else {
                        bootGrowl(res.message || 'Save failed', 'danger');
                    }
                });
            }
        });
        setTimeout(function () {
            var el = document.getElementById(inputId);
            if (el) {
                el.value = currentName;
                el.focus();
                el.select();
            }
        }, 0);
    }

    function removeNode(id, displayName) {
        if (!id) {
            return;
        }
        var tipName = displayName || id;
        showConfirm({
            title: 'Sure Replies',
            icon: 'warning',
            confirmType: 'danger',
            confirmText: 'Delete',
            body: '<p class="mb-0">Remove offline nodes<strong>' + escapeHtml(tipName)
                + '</strong>, this operation cannot be restored.</p>',
            onConfirm: function () {
                doPoster('/cluster/remove', {id: id}, function (res) {
                    if (res.success === true) {
                        bootGrowl(res.data || 'Deleted successfully', 'success');
                        loadNodeMetrics(false);
                    } else {
                        bootGrowl(res.message || 'Delete failed', 'danger');
                    }
                });
            }
        });
    }

    function forceExpireLeaderGracePeriod() {
        showConfirm({
            title: 'Skip waiting for node to recover?',
            icon: 'warning',
            confirmText: 'Confirm',
            body: '<p class="mb-0">Task Instead Reassigned Offline to New Node</p>',
            onConfirm: function () {
                doPoster('/cluster/forceExpireGracePeriod', {}, function (res) {
                    if (res.success === true) {
                        bootGrowl(res.data || 'Skip Recovery', 'success');
                        loadNodeMetrics(false);
                    } else {
                        bootGrowl(res.message || 'Restore Failed', 'danger');
                    }
                });
            }
        });
    }

    function updateTaskSummary(overview) {
        if (!overview) {
            $('#clusterTpsTotal').text('-');
            $('#clusterWorkItemTotal').text('-');
            $('#clusterIncTotal').text('-');
            return;
        }
        $('#clusterTpsTotal').text(formatDash(Math.floor(overview.totalTps || 0)));
        $('#clusterWorkItemTotal').text(formatDash(overview.totalFullWorkItems));
        $('#clusterIncTotal').text(formatDash(overview.totalIncremental));
    }

    function loadNodeMetrics(refreshTable) {
        doGetter('/cluster/metrics', {}, function (res) {
            if (res.success !== true) {
                if (refreshTable) {
                    bootGrowl(res.message || 'Failed to load node indicator', 'warning');
                }
                return;
            }
            metricsByNodeId = {};
            var overview = res.data || {};
            var nodes = overview.nodes || [];
            nodes.forEach(function (item) {
                if (item && item.nodeId) {
                    metricsByNodeId[item.nodeId] = item;
                }
            });
            if (clusterEnabled) {
                updateClusterCharts(overview);
            }
            updateTaskSummary(overview);
            if (pagination && typeof pagination.doSearch === 'function') {
                pagination.doSearch({}, pagination.currentPage || 1);
            }
        });
    }

    window.initClusterList = initClusterList;

    // This script is only availableclusterEnabled Timelist.html Introduction, Direct Initialization
    $(document).ready(function () {
        initClusterList({clusterEnabled: true});
    });
})(window);
