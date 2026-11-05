
// Show Dialog
function showMessageDetail($message, icon, title) {
    $message.unbind('click').bind('click', function() {
        const content = $(this).text();
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

// OpenTry againPageNoodles.
function showRetryDetail(taskId, messageId) {
    doLoader("/monitor/page/retry?taskId=" + taskId + "&messageId=" + messageId);
}

// WillJSON ObjectConvertAs TableHTML
function jsonToTable(jsonObj) {
    let $content = '<table class="table">';
    $content += '<thead><tr><th></th><th>Fields</th><th>Value</th></tr></thead>';
    $content += '<tbody>';
    let index = 1;
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

// ShowData detailsdialogue box
function showDataDetail(id) {
    const $element = $("#" + id);
    const content = $element.text();
    if (!content || !content.trim()) {
        return;
    }

    try {
        // Try ParsingJSON
        const jsonObj = JSON.parse(content);
        showConfirm({
            title: 'Data details',
            icon: 'info',
            size: 'max',
            body: jsonToTable(jsonObj),
            confirmText: 'Close',
            confirmType: 'primary'
        });
    } catch (e) {
        // If parsingFailed，Show original text
        showConfirm({
            title: 'Data details',
            icon: 'info',
            size: 'max',
            message: content
        });
    }
}

// ViewExecut MOU
function bindQueryActuatorEvent() {
    let pagination;
    let metaSelect;
    let searchInput;

    function params() {
        return {
            "id": metaSelect.getValues()[0] || '',
            "name": (searchInput && searchInput.getValue()) || '',
        }
    }

    // SearchFunctions
    function search() {
        pagination.doSearch(params());
    }

    // DriverDraw Down (Loving)minPageSearch，Skip initialized callback to avoid duplicate requests)
    metaSelect = $('#searchActuatorMeta').dbSelect({
        type: 'single',
        remoteSearch: true,
        pageSize: 50,
        keywordDebounceMs: 300,
        loadOptions: function (query, onSuccess, onError) {
            doPoster('/monitor/queryMeta', {
                pageNum: query.pageNum,
                pageSize: query.pageSize,
                searchKey: query.searchKey || ''
            }, function (res) {
                if (res.success === true) {
                    const paging = res.data || {};
                    const rows = Array.isArray(paging.data) ? paging.data : [];
                    let converted = rows.map(function (row) {
                        const name = row.mappingName || '';
                        const model = row.model || '';
                        // Executor indicator byMapping/taskId Linkage, no use.Meta Primary key
                        return {
                            label: name + ' (' + model + ')',
                            value: row.taskId || ''
                        };
                    });
                    if (Number(query.pageNum) === 1) {
                        converted = [{ label: 'None', value: '' }].concat(converted);
                    }
                    onSuccess({
                        pageNum: paging.pageNum || query.pageNum,
                        pageSize: paging.pageSize || query.pageSize,
                        total: paging.total || 0,
                        data: converted
                    });
                } else {
                    bootGrowl(res.message || 'Failed to load driver list', 'danger');
                    if (typeof onError === 'function') {
                        onError();
                    }
                }
            });
        },
        onSelect: search
    });

    // SearchBox InputEvent
    if (document.getElementById('searchActuator')) {
        searchInput = initSearch('searchActuator', search);
    }

    // InitializeminPageManager
    pagination = new PaginationManager({
        requestUrl: '/monitor/queryActuator',
        tableBodySelector: '#actuatorList',
        pageSize: 5,
        params: params(),
        showBoundaryButtons: false,
        renderRow: function (row, index) {
            return `
                <tr>
                    <td>${index}</td>
                    <td>[${row.group || ''}]${row.metricName || ''}</td>
                    <td>${row.measurements[0].value || ''}</td>
                </tr>
            `;
        },
        emptyHtml: '<td colspan="3" class="text-center"><i class="fa fa-tasks empty-icon"></i><p class="empty-text">Data not available</p></td>'
    });
    return {
        search: function() {
            pagination.doSearch(params(), pagination.currentPage);
        }
    }
}

$(function () {
    // Chart Examples
    let charts = {
        queue: null,
        storage: null,
        tps: null,
        cpu: null,
        memory: null
    };

    // Chart.js DefaultConfigure
    Chart.defaults.font.family = '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif';
    Chart.defaults.font.size = 12;
    Chart.defaults.color = '#8c8c8c';

    /**
     * InitializeLine Chart（TPS、CPU、Memory、Backlog、Enduring）
     */
    function initLineChart(canvasId, label, color, solidFill) {
        const ctx = document.getElementById(canvasId);
        if (!ctx) return null;
        
        // IfYesSolid filling, using non-transparent colours;NoUse translucent
        const bgColor = solidFill ? color.replace('1)', '0.6)') : color.replace('1)', '0.1)');
        
        return new Chart(ctx, {
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
                        grid: {
                            display: false
                        },
                        ticks: {
                            maxTicksLimit: 8
                        }
                    },
                    y: {
                        display: true,
                        beginAtZero: true,
                        // suggestedMax: suggestedMax || 100,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        enabled: true,
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        padding: 12,
                        titleColor: '#fff',
                        bodyColor: '#fff'
                    }
                }
            }
        });
    }
    
    /**
     * Update line chart data
     */
    function updateLineChart(chart, labels, data) {
        if (!chart) return;
        chart.data.labels = labels;
        chart.data.datasets[0].data = data;
        chart.update('none');
    }

    // Backlog/Enduring：PressTPS Same thing.TimeTake a sample, use the last spot.ValueCompletion
    const queueSamples = {};
    const storageSamples = {};

    function alignTrend(labels, samples, currentValue) {
        if (!labels || !labels.length) {
            return [];
        }
        const lastLabel = labels[labels.length - 1];
        samples[lastLabel] = Number(currentValue) || 0;

        const alive = {};
        for (let i = 0; i < labels.length; i++) {
            alive[labels[i]] = true;
        }
        Object.keys(samples).forEach(function (key) {
            if (!alive[key]) {
                delete samples[key];
            }
        });

        let last = 0;
        const values = [];
        for (let i = 0; i < labels.length; i++) {
            const key = labels[i];
            if (samples[key] !== undefined) {
                last = samples[key];
            }
            values.push(last);
        }
        return values;
    }

    /**
     * Initialize all charts
     */
    function initCharts() {
        charts.tps = initLineChart('tpsChart', 'TPS', 'rgba(245, 108, 108, 1)', false);
        charts.cpu = initLineChart('cpuChart', 'C Produce Use', 'rgba(82, 196, 26, 1)', false);
        charts.memory = initLineChart('memoryChart', 'Memory Usage', 'rgba(24, 144, 255, 1)', true);
        charts.queue = initLineChart('queueChart', 'Backlog', 'rgba(250, 173, 20, 1)', false);
        charts.storage = initLineChart('storageChart', 'Enduring', 'rgba(114, 46, 209, 1)', false);
    }

    function updateMetricsTable(metrics){
        const trs = [];
        $.each(metrics, function (i) {
            trs.push(`<tr><td>${metrics[i].group}</td><td>${metrics[i].detail}</td></tr>`);
        });
        $("#metrics").html(trs);
    }

    function processBarState(percent) {
        if (percent >= 80) {
            return {
                value: 'danger',
                title: 'progress-value-danger'
            }
        }
        if (percent >= 60) {
            return {
                value: 'warning',
                title: 'progress-value-warning'
            }
        }
        return {
            value: 'success',
            title: 'progress-value-success'
        }
    }

    function updateCpuProcessBar(data){
        if (!data) {
            return;
        }
        let state = processBarState(data.totalPercent);
        $("#cpuProgressBar").html(`<div class="progress-header">
                <span class="progress-title">C Produce Use</span>
                <span class="progress-value ${state.title}">${data.totalPercent}%</span>
            </div>
            <div class="progress-bar">
                <div class="progress-fill ${state.value}" style="width: ${data.totalPercent}%"></div>
            </div>
            <div class="progress-detail">
                <span>Core<strong>${data.core}</strong></span>
                <span>UserState<strong>${data.userPercent}%</strong></span>
                <span>System State<strong>${data.sysPercent}%</strong></span>
            </div>`);
    }

    function updateMemoryProcessBar(data){
        if (!data) {
            return;
        }
        let state = processBarState(data.totalPercent);
        $("#memoryProgressBar").html(`<div class="progress-header">
                <span class="progress-title">Memory UsageRate</span>
                <span class="progress-value ${state.title}">${data.totalPercent}%</span>
            </div>
            <div class="progress-bar">
                <div class="progress-fill ${state.value}" style="width: ${data.totalPercent}%"></div>
            </div>
            <div class="progress-detail">
                <span>TotalMemory <strong>${data.sysTotal} GB</strong></span>
                <span>UserState<strong>${data.jvmUsed} / ${data.jvmTotal} GB</strong></span>
                <span>System State<strong>${data.sysUsed} GB</strong></span>
            </div>`);
    }

    function updateDiskProcessBar(data){
        if (!data) {
            return;
        }
        let state = processBarState(data.usedPercent);
        $("#diskProgressBar").html(`<div class="progress-header">
                <span class="progress-title">DiskUsage</span>
                <span class="progress-value ${state.title}">${data.usedPercent}%</span>
            </div>
            <div class="progress-bar">
                <div class="progress-fill ${state.value}" style="width: ${data.usedPercent}%"></div>
            </div>
            <div class="progress-detail">
                <span>Total capacity<strong>${data.total} GB</strong></span>
                <span>Used<strong>${data.used} GB</strong></span>
            </div>`);
    }

    function updateMonitorData() {
        doGetter("/monitor/metric", {}, function (response) {
            if (response.success === true) {
                const r = response.data;
                // Update line chart data
                updateLineChart(charts.tps, r.tps.name, r.tps.value);
                $("#tps").text(r.tps.average > 0 ? 'Executor TPS, average:'+ r.tps.average + 'Second' : 'Executor TPS');
                updateLineChart(charts.cpu, r.cpu.name, r.cpu.value);
                updateLineChart(charts.memory, r.memory.name, r.memory.value);
                const tpsLabels = (r.tps && r.tps.name) || [];
                updateLineChart(charts.queue, tpsLabels, alignTrend(tpsLabels, queueSamples, r.queueUp));
                updateLineChart(charts.storage, tpsLabels, alignTrend(tpsLabels, storageSamples, r.storageQueueUp));
                updateMetricsTable(r.metrics);
                updateCpuProcessBar(r.cpu);
                updateMemoryProcessBar(r.memory);
                updateDiskProcessBar(r.disk);
            }
        });
    }
    
    // PagePasta LoadFinishInitialize after
    initCharts();

    // DefinitionsBackFunctions，SonPageNoodles.Back
    window.backIndexPage = function () {
        doLoader('/monitor');
    };

    // ImmediatelyRun onceUpdate
    updateMonitorData();

    const queryActuator = bindQueryActuatorEvent();

    // Register to GlobalScheduledRefreshManager
    PageRefreshManager.register(() => {
        updateMonitorData();
        queryActuator.search();
    });
});