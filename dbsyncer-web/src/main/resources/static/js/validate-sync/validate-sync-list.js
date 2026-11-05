/**
 * Data validationListPage
 */
(function (window) {
    'use strict';

    function copyTask(id) {
        doPoster('/validate-sync/copy', { id: id }, function (response) {
            if (response.success) {
                bootGrowl('Copied successfully', 'success');
                backIndexPage();
            } else {
                bootGrowl('Copy failed: ' + response.message, 'danger');
            }
        });
    }

    function changeTaskState(id, url, title) {
        const btn = event.target.closest('.table-action-btn');
        const originalText = btn.innerHTML;
        btn.innerHTML = '<i class="fa fa-spinner fa-spin" title="' + title + 'I\'ll take it.</i>';
        btn.disabled = true;
        doPoster(url, { id: id }, function (response) {
            btn.innerHTML = originalText;
            btn.disabled = false;
            if (response.success) {
                bootGrowl(title + 'Success', 'success');
                refreshIndexList();
            } else {
                bootGrowl(title + 'Failed: ' + response.message, 'danger');
            }
        });
    }

    function deleteTask(id) {
        showConfirm({
            title: 'Are you sure you want to delete the task?',
            icon: 'warning',
            size: 'large',
            confirmType: 'danger',
            onConfirm: function () {
                doPoster('/validate-sync/remove', { id: id }, function (response) {
                    if (response.success === true) {
                        bootGrowl('Deleted successfully!', 'success');
                        const rawStr = localStorage.getItem('dbsyncer.pagination.validate-sync-list');
                        const raw = JSON.parse(rawStr);
                        raw.pageNum = 1;
                        localStorage.setItem('dbsyncer.pagination.validate-sync-list', JSON.stringify(raw));
                        backIndexPage();
                    } else {
                        bootGrowl(response.message || 'Delete failed', 'danger');
                    }
                });
            }
        });
    }

    function getTaskStateConfig(taskId) {
        return {
            0: {
                icon: 'fa-play',
                title: 'Start',
                onclick: "changeTaskState('" + taskId + "', '/validate-sync/start', 'start')",
                disabled: false,
                class: 'badge-info',
                text: 'Not running'
            },
            1: {
                icon: 'fa-pause',
                title: 'Stop',
                onclick: "changeTaskState('" + taskId + "', '/validate-sync/stop', 'stop')",
                disabled: false,
                class: 'badge-success',
                text: 'Running'
            },
            2: {
                icon: 'fa-spinner fa-spin',
                title: 'Stopping',
                text: 'Stopping',
                disabled: true,
                class: 'badge-warning'
            },
            3: {
                icon: 'fa-play',
                title: 'Start',
                onclick: "changeTaskState('" + taskId + "', '/validate-sync/start', 'start')",
                disabled: false,
                class: 'badge-primary',
                text: 'Completed'
            }
        };
    }

    function renderTaskTriggerText(type) {
        const modelState = {
            timing: { class: 'badge-primary', text: 'Scheduled' },
            once: { class: 'badge-info', text: 'Run once' }
        };
        const config = modelState[type];
        return '<span class="badge ' + config.class + '">' + config.text + '</span>';
    }

    function renderTaskStateText(state) {
        const stateConfig = getTaskStateConfig();
        const config = stateConfig[state] || stateConfig[0];
        return '<span class="badge ' + config.class + '">' + config.text + '</span>';
    }

    function renderTaskStateButton(state, mappingId) {
        const stateConfig = getTaskStateConfig(mappingId);
        const config = stateConfig[state] || stateConfig[0];
        const disabledAttr = config.disabled ? ' disabled' : '';
        const onclickAttr = config.onclick ? ' onclick="' + config.onclick + '"' : '';
        const stateBtn = [];
        stateBtn.push(
            '<button class="table-action-btn play" data-id="' + mappingId + '" title="' + config.title + '"' + onclickAttr + disabledAttr + '>'
            + '<i class="fa ' + config.icon + '"></i></button>'
        );
        if (state === 0 || state === 3) {
            stateBtn.push(
                '{\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FF00} {\cHFFFFFF}{\cH00FF00} {\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FF00} {\cHFFFFFF} {\cHFFFFFF}{\cH00FF} {\cH00FF000 \cH303030303030300 \cH303030303030D}' + mappingId + '\')">'
                + '<i class="fa fa-trash"></i></button>'
            );
        }
        return stateBtn.join('');
    }

    function renderTableProgressText(task) {
        var total = Number(task.totalTableCount);
        var completed = Number(task.completedTableCount);
        if (isNaN(total) || total <= 0) {
            return '';
        }
        if (isNaN(completed) || completed < 0) {
            completed = 0;
        }
        if (completed > total) {
            completed = total;
        }
        return '<span class="text-xs text-secondary whitespace-nowrap">' + completed + '/' + total + 'Watch</span>';
    }

    function renderTaskDurationText(task) {
        // ScheduledTasksIt's been a long time.ListDo not show time-consuming
        if (task.trigger === 'timing') {
            return '';
        }
        var state = Number(task.metaState);
        var RUNNING = 1;
        var STOPPING = 2;
        // Running/Stopping updateTime Unity, often without progress.Refresh，Use front-end currentTimeReal time.
        var endTime = (state === RUNNING || state === STOPPING) ? Date.now() : task.updateTime;
        var durationText = formatElapsedDuration(task.startTime, endTime);
        if (!durationText) {
            return '';
        }
        return '<span class="text-xs text-tertiary whitespace-nowrap">Time-consuming:' + durationText + '</span>';
    }

    function renderResultColumn(task) {
        var n = Number(task.errorCount);
        if (isNaN(n) || n < 0) {
            n = 0;
        }
        var taskId = String(task.id || '').replace(/'/g, '');
        var isRunning = Number(task.metaState) === 1;
        var progressRaw = task.progress;
        if (progressRaw === null || progressRaw === undefined || progressRaw === '') {
            if (isRunning) {
                progressRaw = 0;
            } else {
                return '';
            }
        }
        var progress = Number(progressRaw);
        if (isNaN(progress) || progress < 0) {
            progress = 0;
        }
        if (progress > 100) {
            progress = 100;
        }
        if (progress === 0 && !isRunning) {
            return '';
        }

        var state = 'success';
        if (isRunning) {
            if (progress >= 80) {
                state = 'danger';
            } else if (progress >= 60) {
                state = 'warning';
            }
        } else if (n > 0) {
            state = 'danger';
        }

        var left = (n > 0)
            ? ('Error：<a href="javascript:void(0)" class="hover:underline cursor-pointer" title="Check for abnormal verification" '
                + 'onclick="doLoader(\'/validate-sync/page/detail?id=' + taskId + '&detailStatus=fail\'); return false;">'
                + '<span class="badge badge-error">' + n + '</span></a>')
            : '<span class="badge badge-success">Normal</span>';
        var tableProgressHtml = renderTableProgressText(task);
        var durationHtml = renderTaskDurationText(task);
        var centerParts = [];
        if (tableProgressHtml) {
            centerParts.push(tableProgressHtml);
        }
        if (durationHtml) {
            centerParts.push(durationHtml);
        }
        var centerHtml = centerParts.length
            ? ('<span class="progress-meta">' + centerParts.join('') + '</span>')
            : '';
        var rightText = (isRunning && progress === 0) ? '0%' : (progress + '%');
        return ''
            + '<div class="min-w-200">'
            + '  <div class="progress-header progress-header-compact">'
            + '    <span class="progress-title">' + left + '</span>'
            + centerHtml
            + '    <span class="progress-value progress-value-' + state + '">' + rightText + '</span>'
            + '  </div>'
            + '  <div class="progress-bar progress-bar-compact">'
            + '    <div class="progress-fill ' + state + '" data-progress-width="' + progress + '"></div>'
            + '  </div>'
            + '</div>';
    }

    function renderConnectorCell(task) {
        var source = task.sourceConnector || {};
        var target = task.targetConnector || {};
        var sourceType = escapeHtml((source.config && source.config.connectorType) || '');
        var targetType = escapeHtml((target.config && target.config.connectorType) || '');
        var sourceName = source.name || 'Connector has been deleted';
        var targetName = target.name || 'Connector has been deleted';
        return ''
            + '<div class="flex items-center">'
            + '<img draggable="false" class="connector-logo mr-3" src="' + $basePath + '/img/' + sourceType + '.png" alt="">'
            + '<span>' + escapeHtml(sourceName) + '</span>'
            + '</div>'
            + '<i class="fa fa-angle-double-down ml-3"></i>'
            + '<div class="flex items-center">'
            + '<img draggable="false" class="connector-logo mr-3" src="' + $basePath + '/img/' + targetType + '.png" alt="">'
            + '<span>' + escapeHtml(targetName) + '</span>'
            + '</div>';
    }

    function initValidateSyncListPage() {
        window.backIndexPage = function () {
            doLoader('/validate-sync/list');
        };
        window.copyTask = copyTask;
        window.changeTaskState = changeTaskState;
        window.deleteTask = deleteTask;

        var groupToolbar = null;
        const pagination = new PaginationManager({
            requestUrl: '/validate-sync/search',
            tableBodySelector: '#validate-sync-table',
            storageKey: 'validate-sync-list',
            pageSize: 20,
            params: {groupId: (window.GroupToolbar && GroupToolbar.peekStoredGroupId('validateSync')) || ''},
            refreshCompleted: function () {
                if (groupToolbar) {
                    groupToolbar.setListTotal(pagination.total || 0);
                }
            },
            renderRow: function (task, index) {
                const taskId = String(task.id).replace(/"/g, '');
                return ''
                    + '<tr>'
                    + '<td>' + index + '</td>'
                    + '<td>'
                    + '<a href="javascript:void(0)""class= "text-primary hoover:underline cursor-pointer" '
                    + 'onclick="doLoader(\'/validate-sync/page/detail?id=' + taskId + '\')">' + escapeHtml(task.name) + '</a>'
                    + '</td>'
                    + '<td title="' + escapeHtml((task.sourceConnector && task.sourceConnector.name) || 'Connector has been deleted')
                    + ' > ' + escapeHtml((task.targetConnector && task.targetConnector.name) || 'Connector has been deleted') + '">'
                    + renderConnectorCell(task)
                    + '</td>'
                    + '<td>' + renderTaskTriggerText(task.trigger) + '</td>'
                    + '<td>' + renderResultColumn(task) + '</td>'
                    + '<td>' + renderTaskStateText(task.metaState || 0) + '</td>'
                    + '<td>' + formatDate(task.updateTime || '') + '</td>'
                    + '<td><div class="flex items-center">'
                    + '{\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FFFF} {\cHFFFFFF}{\cH00FF00} {\cHFFFFFF} {\cHFFFFFF}{\cH00FF00} {\cHFFFFFF}{\cH00FF00} {\cHFFFFFF}{\cH00FF00} {\cHFFFFFF} {\cHFFFFFF} {\cHFFFFFF} {\cHFFFFFF} {\cHFFFFFF} {\cHFFFFFF} {\cHFFFFFF} {\cH00FF} {\cHFFFFFF} {\cH {\cH00H00FF00} {\cH3030303030303000}' + task.id + '\')">'
                    + '<i class="fa fa-edit"></i></button>'
                    + renderTaskStateButton(task.metaState || 0, task.id)
                    + '</div></td>'
                    + '</tr>';
            },
            emptyHtml: '<td colspan="9" class="text-center"><i class="fa fa-exchange empty-icon"></i>'
                + '<p class="empty-text">No assignment.</p><p class="empty-description">Click Add button to create the first task</p></td>',
            customPageSize: true
        });

        var searchInput = null;
        groupToolbar = GroupToolbar.initListToolbar({
            containerSelector: '#validateSyncGroupToolbar',
            createButtonHostSelector: '#validateSyncGroupCreateHost',
            resourceType: 'validateSync',
            searchUrl: '/validate-sync/search',
            addPageUrl: '/validate-sync/pageAdd',
            addButtonSelector: '#validateSyncAddBtn',
            onChange: function () {
                pagination.doSearch(groupToolbar.getSearchParams({ searchKey: searchInput ? searchInput.getValue() : '' }), 1);
            }
        });
        groupToolbar.setListTotal(pagination.total || 0);

        searchInput = initSearch('validate-sync-search', function (searchKey) {
            pagination.doSearch(groupToolbar.getSearchParams({ searchKey: searchKey }), 1);
        });

        window.refreshIndexList = function () {
            pagination.doSearch(groupToolbar.getSearchParams({ searchKey: searchInput.getValue() }), pagination.currentPage);
        };

        PageRefreshManager.register(function () {
            pagination.doSearch(groupToolbar.getSearchParams({ searchKey: searchInput.getValue() }), pagination.currentPage);
        });
    }

    $(document).ready(initValidateSyncListPage);
})(window);
