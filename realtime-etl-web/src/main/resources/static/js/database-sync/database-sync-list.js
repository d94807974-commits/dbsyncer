/**
 * Database migrationListPage
 */
(function (window) {
    'use strict';

    /** and CommonTaskStatusEnum.DONE Alignment: current round of operationsCompleted */
    var META_STATE_DONE = 3;

    function databaseSyncStart(taskId) {
        doPoster('/database-sync/start', {id: taskId}, function (response) {
            if (response.success) {
                bootGrowl(response.data || 'Started successfully', 'success');
                refreshIndexList();
            } else {
                bootGrowl(response.message || 'Start Failed', 'danger');
            }
        });
    }

    function databaseSyncStop(taskId) {
        doPoster('/database-sync/stop', {id: taskId}, function (response) {
            if (response.success) {
                bootGrowl(response.data || 'Stopped successfully', 'success');
                refreshIndexList();
            } else {
                bootGrowl(response.message || 'Stop Failure', 'danger');
            }
        });
    }

    function databaseSyncRemove(taskId) {
        showConfirm({
            title: 'Are you sure you want to delete the task?',
            icon: 'warning',
            size: 'large',
            confirmType: 'danger',
            onConfirm: function () {
                doPoster('/database-sync/remove', {id: taskId}, function (response) {
                    if (response.success) {
                        bootGrowl(response.data || 'Deleted successfully', 'success');
                        refreshIndexList();
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
                onclick: "databaseSyncStart('" + taskId + "')",
                disabled: false,
                class: 'badge-info',
                text: 'Not running'
            },
            1: {
                icon: 'fa-pause',
                title: 'Stop',
                onclick: "databaseSyncStop('" + taskId + "')",
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
                onclick: "databaseSyncStart('" + taskId + "')",
                disabled: false,
                class: 'badge-primary',
                text: 'Completed'
            }
        };
    }

    function renderTaskStateText(state) {
        const stateConfig = getTaskStateConfig();
        const config = stateConfig[state] || stateConfig[0];
        return '<span class="badge ' + config.class + '">' + config.text + '</span>';
    }

    function renderTaskStateButton(state, taskId) {
        const stateConfig = getTaskStateConfig(taskId);
        const config = stateConfig[state] || stateConfig[0];
        const disabledAttr = config.disabled ? ' disabled' : '';
        const onclickAttr = config.onclick ? ' onclick="' + config.onclick + '"' : '';
        let html = '<button class="table-action-btn play" title="' + config.title + '"' + onclickAttr + disabledAttr + '>'
            + '<i class="fa ' + config.icon + '"></i></button>';
        if (state === 0 || state === 3) {
            html += '<button class= "table-action-btn delete" title= onclick="databaseSyncRemove(\ ♪ I\'m sorry ♪' + taskId + '\')">'
                + '<i class="fa fa-trash"></i></button>';
        }
        return html;
    }

    function formatCount(n) {
        if (n === null || n === undefined || isNaN(n)) {
            return '0';
        }
        try {
            return Number(n).toLocaleString('en-US');
        } catch (e) {
            return String(n);
        }
    }

    /**
     * PressCompositeModeandPhase presentation: Structurea/b orData c/dSoldierminMother not all as「Statistics」）。
     */
    function renderProgressMetaText(task) {
        var isRunning = Number(task.metaState) === 1;
        var tableTotal = Number(task.totalTableCount);
        var schemaDone = Number(task.schemaCompletedCount);
        var completed = Number(task.completedTableCount);
        var synced = Number(task.syncedRows);
        var sourceTotal = Number(task.sourceTotal);
        if (isNaN(tableTotal) || tableTotal < 0) {
            tableTotal = 0;
        }
        if (isNaN(schemaDone) || schemaDone < 0) {
            schemaDone = 0;
        }
        if (isNaN(completed) || completed < 0) {
            completed = 0;
        }
        if (isNaN(synced) || synced < 0) {
            synced = 0;
        }
        if (isNaN(sourceTotal) || sourceTotal < 0) {
            sourceTotal = 0;
        }
        if (schemaDone > tableTotal && tableTotal > 0) {
            schemaDone = tableTotal;
        }
        if (completed > tableTotal && tableTotal > 0) {
            completed = tableTotal;
        }
        if (sourceTotal > 0 && synced > sourceTotal) {
            synced = sourceTotal;
        }

        var enableSchema = !!task.enableCopySchema;
        var enableData = !!task.enableCopyData;

        if (!isRunning) {
            if (enableData && sourceTotal > 0) {
                return '<span class="text-xs text-secondary whitespace-nowrap" title="Walked with / Source">'
                    + formatCount(synced) + '/' + formatCount(sourceTotal) + 'Okay.</span>';
            }
            if (tableTotal > 0) {
                return '<span class="text-xs text-secondary whitespace-nowrap" title="Completed tables/ summary tables">'
                    + completed + '/' + tableTotal + 'Watch</span>';
            }
            return '';
        }

        if (enableSchema && !enableData) {
            if (tableTotal <= 0) {
                return '';
            }
            return '<span class="text-xs text-secondary whitespace-nowrap" title="Structure completion table / summary statement">'
                + 'Table structure ' + schemaDone + '/' + tableTotal + '</span>';
        }

        if (enableSchema && enableData && schemaDone < tableTotal) {
            if (tableTotal <= 0) {
                return '';
            }
            return '<span class="text-xs text-secondary whitespace-nowrap" title="Structure completion table / summary statement">'
                + 'Table structure ' + schemaDone + '/' + tableTotal + '</span>';
        }

        if (enableData) {
            var totalText = sourceTotal > 0 ? formatCount(sourceTotal) : 'Statistics';
            return '<span class="text-xs text-secondary whitespace-nowrap" title="Walked with / Source">'
                + 'Data ' + formatCount(synced) + '/' + totalText + '</span>';
        }

        return '';
    }

    function renderTaskDurationText(task) {
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
        } else if (Number(task.metaState) !== META_STATE_DONE && !isRunning && progress > 0 && progress < 100) {
            state = 'warning';
        } else if (n > 0) {
            state = 'danger';
        }

        var left = (n > 0)
            ? ('Error：<a href="javascript:void(0)" class="hover:underline cursor-pointer" title="View failed migration results" '
                + 'onclick="doLoader(\'/database-sync/page/detail?id=' + taskId + '&detailStatus=fail\'); return false;">'
                + '<span class="badge badge-error">' + n + '</span></a>')
            : '<span class="badge badge-success">Normal</span>';
        var tableProgressHtml = renderProgressMetaText(task);
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

    function initDatabaseSyncerListPage() {
        window.backIndexPage = function () {
            doLoader('/database-sync/list');
        };
        window.databaseSyncStart = databaseSyncStart;
        window.databaseSyncStop = databaseSyncStop;
        window.databaseSyncRemove = databaseSyncRemove;

        var groupToolbar = null;
        const pagination = new PaginationManager({
            requestUrl: '/database-sync/search',
            tableBodySelector: '#database-sync-table',
            storageKey: 'database-sync-list',
            params: {groupId: (window.GroupToolbar && GroupToolbar.peekStoredGroupId('databaseSync')) || ''},
            refreshCompleted: function () {
                if (groupToolbar) {
                    groupToolbar.setListTotal(pagination.total || 0);
                }
            },
            renderRow: function (task, index) {
                const mappingCount = task.mappingCount != null ? task.mappingCount : 0;
                const taskId = String(task.id || '').replace(/"/g, '');
                return ''
                    + '<tr>'
                    + '<td>' + index + '</td>'
                    + '<td>'
                    + '<a href="javascript:void(0)""class="text-primary hoover:underline cursor-pointer"title= "click to see the migration results" '
                    + 'onclick="doLoader(\'/database-sync/page/detail?id=' + taskId + '\')">'
                    + escapeHtml(task.name || '') + '</a>'
                    + '</td>'
                    + '<td>' + mappingCount + '</td>'
                    + '<td>' + renderResultColumn(task) + '</td>'
                    + '<td>' + renderTaskStateText(task.metaState || 0) + '</td>'
                    + '<td>' + formatDate(task.updateTime || '') + '</td>'
                    + '<td><div class="flex items-center">'
                    + '=/database/page/edit? ='
                    + taskId + '\')">'
                    + '<i class="fa fa-edit"></i></button>'
                    + renderTaskStateButton(task.metaState || 0, task.id)
                    + '</div></td>'
                    + '</tr>';
            },
            emptyHtml: '<td colspan="8" class="text-center">'
                + '<i class="fa fa-database empty-icon"></i>'
                + '<p class="empty-text">No assignment.</p>'
                + '<p class="empty-description">Click Add to create the first task</p></td>',
            customPageSize: true
        });

        var searchInput = null;
        groupToolbar = GroupToolbar.initListToolbar({
            containerSelector: '#databaseSyncGroupToolbar',
            createButtonHostSelector: '#databaseSyncGroupCreateHost',
            resourceType: 'databaseSync',
            searchUrl: '/database-sync/search',
            addPageUrl: '/database-sync/pageAdd',
            addButtonSelector: '#databaseSyncAddBtn',
            onChange: function () {
                pagination.doSearch(groupToolbar.getSearchParams({ searchKey: searchInput ? searchInput.getValue() : '' }), 1);
            }
        });
        groupToolbar.setListTotal(pagination.total || 0);

        searchInput = initSearch('database-sync-search', function (searchKey) {
            pagination.doSearch(groupToolbar.getSearchParams({ searchKey: searchKey }), 1);
        });

        window.refreshIndexList = function () {
            pagination.doSearch(groupToolbar.getSearchParams({ searchKey: searchInput.getValue() }), pagination.currentPage);
        };

        PageRefreshManager.register(function () {
            pagination.doSearch(groupToolbar.getSearchParams({ searchKey: searchInput.getValue() }), pagination.currentPage);
        });
    }

    $(document).ready(initDatabaseSyncerListPage);
})(window);
