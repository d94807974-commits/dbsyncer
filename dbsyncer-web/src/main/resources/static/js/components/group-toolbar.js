/**
 * Business groupToolbar:ListFilter / NewEdit·Delete group；AssociationResourceYes.GroupBlast window maintenance.
 * Have agreed:ListScreeninggroupId Empty=All；ResourceandGroupMore to more.
 * See the top-bar layout.GroupDesign§7.2。
 */
(function (window, $) {
    'use strict';

    var groupsCache = null;
    var GROUP_FILTER_STORAGE_PREFIX = 'dbsyncer.group.filter.';

    function escapeHtml(text) {
        if (typeof window.escapeHtml === 'function') {
            return window.escapeHtml(text);
        }
        var div = document.createElement('div');
        div.textContent = text == null ? '' : String(text);
        return div.innerHTML;
    }

    function groupFilterStorageKey(resourceType) {
        return GROUP_FILTER_STORAGE_PREFIX + (resourceType || 'default');
    }

    /**
     * Read CacheGroupScreening (forListFirstscreen queryandRestore ToolbarTotalUse it.
     */
    function peekStoredGroupId(resourceType) {
        if (!resourceType) {
            return '';
        }
        try {
            return localStorage.getItem(groupFilterStorageKey(resourceType)) || '';
        } catch (e) {
            console.warn('[GroupToolbar] Reading group filter cache failed', e);
            return '';
        }
    }

    function saveStoredGroupId(resourceType, groupId) {
        if (!resourceType) {
            return;
        }
        try {
            localStorage.setItem(groupFilterStorageKey(resourceType), groupId || '');
        } catch (e) {
            console.warn('[GroupToolbar] Save group filter cache failed', e);
        }
    }

    function loadGroups(callback) {
        doPoster('/group/list', {}, function (response) {
            if (!response || !response.success) {
                bootGrowl((response && response.message) || 'Failed to load group', 'danger');
                callback([]);
                return;
            }
            groupsCache = response.data || [];
            callback(groupsCache);
        });
    }

    /**
     * ListFilter options:All + RealGroup（does not contain「NotGroup」）。
     */
    function buildFilterData(groups) {
        var data = [{label: 'All', value: ''}];
        (groups || []).forEach(function (g) {
            if (!g || !g.id) {
                return;
            }
            data.push({label: g.name || '', value: g.id});
        });
        return data;
    }

    function isRealGroupId(groupId) {
        return !!groupId;
    }

    function resolveSelected(groups, preferId) {
        var selected = preferId != null ? preferId : '';
        var list = groups || [];
        if (isRealGroupId(selected) && !list.some(function (g) {
            return g.id === selected;
        })) {
            return '';
        }
        return selected || '';
    }

    function findGroupLabel(data, value) {
        var item = (data || []).filter(function (d) {
            return d.value === value;
        })[0];
        return item ? item.label : '';
    }

    /**
     * InitializeorRefreshSingledbSelect，Quietly select the designationValue（Empty String「All」）。
     */
    function applySelectData($select, api, data, selected, onSelect) {
        if (!api) {
            api = $select.dbSelect({
                type: 'single',
                data: data,
                defaultValue: [selected],
                onSelect: function (values) {
                    if (typeof onSelect === 'function') {
                        onSelect((values && values[0]) != null ? values[0] : '');
                    }
                }
            });
            if (api && selected === '') {
                api.setValues([''], true);
            }
            return api;
        }
        api.setData(data);
        api.setValues([selected], true);
        return api;
    }

    /**
     * AssociationResourceMultiple selection: remoteminPage；groupId Empty=AllResource，Easy to tick the Theirs.
     */
    function buildResourceMultiSelectOptions(searchUrl, groupIdParam, preselected) {
        var initialData = preselected || [];
        var defaultValues = initialData.map(function (item) {
            return item.value;
        });
        return {
            type: 'multiple',
            remoteSearch: true,
            pageSize: 50,
            keywordDebounceMs: 300,
            data: initialData.slice(),
            defaultValue: defaultValues,
            loadOptions: function (query, onSuccess, onError) {
                doPoster(searchUrl, {
                    pageNum: query.pageNum,
                    pageSize: query.pageSize,
                    searchKey: query.searchKey || '',
                    groupId: groupIdParam
                }, function (res) {
                    if (!res || res.success !== true) {
                        if (typeof onError === 'function') {
                            onError(res);
                        }
                        return;
                    }
                    var paging = res.data || {};
                    var rows = Array.isArray(paging.data) ? paging.data : [];
                    var data = [];
                    rows.forEach(function (row) {
                        if (row && row.id) {
                            data.push({label: row.name || row.id, value: row.id});
                        }
                    });
                    onSuccess({
                        pageNum: paging.pageNum || query.pageNum,
                        pageSize: paging.pageSize || query.pageSize,
                        total: paging.total || 0,
                        data: data
                    });
                });
            }
        };
    }

    function destroySelectApi(api) {
        if (api && typeof api.destroy === 'function') {
            api.destroy();
        }
    }

    function bindBatch(groupId, resourceType, resourceIds, done) {
        if (!resourceIds || !resourceIds.length) {
            if (typeof done === 'function') {
                done(true);
            }
            return;
        }
        doPoster('/group/bindBatch', {
            groupId: groupId || '',
            type: resourceType,
            resourceIds: resourceIds.join(',')
        }, function (response) {
            if (!response || !response.success) {
                bootGrowl((response && response.message) || 'Associated resources failed', 'danger');
                if (typeof done === 'function') {
                    done(false);
                }
                return;
            }
            if (typeof done === 'function') {
                done(true);
            }
        });
    }

    /** From SpecifyGroupOwn (without prejudice to other groups)*/
    function unbindBatch(groupId, resourceType, resourceIds, done) {
        if (!groupId || !resourceIds || !resourceIds.length) {
            if (typeof done === 'function') {
                done(true);
            }
            return;
        }
        doPoster('/group/unbindBatch', {
            groupId: groupId,
            type: resourceType,
            resourceIds: resourceIds.join(',')
        }, function (response) {
            if (!response || !response.success) {
                bootGrowl((response && response.message) || 'Failed to remove resources', 'danger');
                if (typeof done === 'function') {
                    done(false);
                }
                return;
            }
            if (typeof done === 'function') {
                done(true);
            }
        });
    }

    /**
     * Load a groupAllAssociationResource（Do it.EditBlast windows pre-check.
     */
    function loadGroupResources(searchUrl, groupId, callback) {
        if (!groupId || !searchUrl) {
            callback([]);
            return;
        }
        doPoster(searchUrl, {
            pageNum: 1,
            pageSize: 500,
            searchKey: '',
            groupId: groupId
        }, function (res) {
            if (!res || res.success !== true) {
                callback([]);
                return;
            }
            var paging = res.data || {};
            var rows = Array.isArray(paging.data) ? paging.data : [];
            var data = [];
            rows.forEach(function (row) {
                if (row && row.id) {
                    data.push({label: row.name || row.id, value: row.id});
                }
            });
            callback(data);
        });
    }

    function diffIds(initialIds, finalIds) {
        var initialSet = {};
        (initialIds || []).forEach(function (id) {
            initialSet[id] = true;
        });
        var finalSet = {};
        (finalIds || []).forEach(function (id) {
            finalSet[id] = true;
        });
        var toAdd = [];
        var toRemove = [];
        (finalIds || []).forEach(function (id) {
            if (!initialSet[id]) {
                toAdd.push(id);
            }
        });
        (initialIds || []).forEach(function (id) {
            if (!finalSet[id]) {
                toRemove.push(id);
            }
        });
        return {toAdd: toAdd, toRemove: toRemove};
    }

    /**
     * ListTopbarGroupDistrict.
     * @param {Object} options
     *   - containerSelector Filters/Edit/DeleteMount container (Searchto the right of the frame)
     *   - createButtonHostSelector 「Group」Button Mount PointResource「Add」Left)
     *   - resourceType / searchUrl / onChange
     *   - addPageUrl / addButtonSelector Optional
     */
    function initListToolbar(options) {
        options = options || {};
        var $container = $(options.containerSelector);
        if (!$container.length) {
            console.warn('[GroupToolbar] container not found:', options.containerSelector);
            return null;
        }

        var resourceType = options.resourceType || '';
        var searchUrl = options.searchUrl || '';
        var editPermission = options.editPermission
            || (resourceType === 'connector' ? 'connector:edit' : 'mapping:edit');
        // Recovery from Cache, AvoidEdit/DeleteFullPageFilter Back after Reload「All」
        var currentGroupId = peekStoredGroupId(resourceType);
        var listTotal = 0;
        var hasGroups = false;
        var filterData = [];
        var selectApi = null;
        var silentChange = false;

        // ListThe container is loaded.flex/gap；Filter Areamin-w-200，Draw down with container width
        $container.html(
            '<div class="group-filter-wrap hidden flex-shrink-0 w-320">' +
            '<select class="form-control group-filter-select"></select>' +
            '</div>' +
            '<button type= "button" class=btnbtn-outline group-edit-btnhiddenlex-shrink-0"' +
            ' data-permission="' + editPermission + '"><i class="fa fa-edit"></i></button>' +
            '<button type= "button" class=btn btn-outline group-remove-btnhedden' +
            ' data-permission="' + editPermission + '"><i class="fa fa-trash"></i></button>'
        );
        var $filterWrap = $container.find('.group-filter-wrap');
        var $select = $container.find('.group-filter-select');
        var $editBtn = $container.find('.group-edit-btn');
        var $removeBtn = $container.find('.group-remove-btn');

        // 「Group」Button Hangs onResource「Add」Side
        var $createHost = options.createButtonHostSelector ? $(options.createButtonHostSelector) : $();
        var $addBtn;
        if ($createHost.length) {
            $createHost.html(
                '<button type= "button" class=btnbtn-outline group-add-btnhedden' +
                ' data-permission="' + editPermission + '"\'"Group</button>'
            );
            $addBtn = $createHost.find('.group-add-btn');
        } else {
            $addBtn = $();
        }

        function canManageGroups() {
            if (window.DBSPermission && typeof DBSPermission.has === 'function') {
                return DBSPermission.has(editPermission);
            }
            return false;
        }

        /**
         * Toggle invisible. Buttons are also available..btn（display:inline-flex）and .hidden（display:none）I don't know.
         * Loaded afterbuttons.css It'll cover..hidden，Inline requireddisplay Promise.「All」TimeEdit/DeleteReally hide.
         */
        function setBtnVisible($btn, visible) {
            if (!$btn || !$btn.length) {
                return;
            }
            $btn.toggleClass('hidden', !visible);
            $btn.css('display', visible ? '' : 'none');
        }

        function syncVisibility() {
            var canEdit = canManageGroups();
            var showFilter = hasGroups;
            // 「All」value It's empty.Edit/Delete；NoneEditNo right to show.
            var showEditRemove = canEdit && hasGroups && isRealGroupId(currentGroupId);
            var showAdd = canEdit && listTotal > 0;
            $filterWrap.toggleClass('hidden', !showFilter);
            setBtnVisible($editBtn, showEditRemove);
            setBtnVisible($removeBtn, showEditRemove);
            setBtnVisible($addBtn, showAdd);
            // Intermediate screening area:NoneTake him down.NoneEditDeleteHide
            $container.toggleClass('hidden', !showFilter && !showEditRemove);
            if ($createHost.length) {
                // NoneEditKeep the power hidden and avoid covering it up.data-permission HideResult
                $createHost.toggleClass('hidden', !showAdd);
            }
        }

        function notifyChange() {
            if (silentChange) {
                return;
            }
            if (typeof options.onChange === 'function') {
                options.onChange(currentGroupId);
            }
        }

        function onFilterSelect(value) {
            if (silentChange) {
                return;
            }
            currentGroupId = value || '';
            saveStoredGroupId(resourceType, currentGroupId);
            syncVisibility();
            notifyChange();
        }

        function refreshSelect(preferId) {
            loadGroups(function (groups) {
                hasGroups = (groups || []).length > 0;
                var selected = resolveSelected(groups, preferId != null ? preferId : currentGroupId);
                currentGroupId = selected;
                saveStoredGroupId(resourceType, currentGroupId);
                filterData = buildFilterData(groups);
                // Let's go in and get started.dbSelect，Avoid Parentdisplay:none Time width is0
                syncVisibility();
                if (hasGroups) {
                    silentChange = true;
                    selectApi = applySelectData($select, selectApi, filterData, selected, onFilterSelect);
                    silentChange = false;
                    // Synchronization: Ensure「All」TimeEdit/DeleteHide still
                    syncVisibility();
                } else {
                    destroySelectApi(selectApi);
                    selectApi = null;
                    $select.empty();
                    currentGroupId = '';
                    saveStoredGroupId(resourceType, '');
                    syncVisibility();
                }
            });
        }

        function openCreateDialog() {
            if (!canManageGroups()) {
                bootGrowl('No permission', 'danger');
                return;
            }
            if (!searchUrl || !resourceType) {
                bootGrowl('No resource type configured, could not create a new group', 'danger');
                return;
            }
            var resourceApi = null;
            var dialog = showConfirm({
                title: 'New group',
                size: 'xlarge',
                position: 'center',
                body: '<div class="group-dialog-body">' +
                    '<div class="form-item"><label class="form-label">Name<strong class="text-primary">*</strong></label>' +
                    '<div class="form-control-area"><input type="text" class="form-control group-name-input" maxlength="50" placeholder="Please enter the group name"/></div></div>' +
                    '<div class="form-item mt-4"><label class="form-label">Associated resources</label>' +
                    '<select class="form-control group-resource-select"></select></div>' +
                    '</div>',
                confirmText: 'OK',
                cancelText: 'Cancel',
                closeOnConfirm: false,
                onConfirm: function () {
                    var name = ($('.group-name-input').val() || '').trim();
                    if (!name) {
                        bootGrowl('Group name cannot be empty', 'warning');
                        return;
                    }
                    var selectedIds = resourceApi && resourceApi.getValues ? resourceApi.getValues().slice() : [];
                    doPoster('/group/add', {name: name, type: resourceType}, function (response) {
                        if (!response.success) {
                            bootGrowl(response.message || 'New Failed', 'danger');
                            return;
                        }
                        var newId = response.data || '';
                        bindBatch(newId, resourceType, selectedIds, function (ok) {
                            if (!ok) {
                                return;
                            }
                            destroySelectApi(resourceApi);
                            dialog.close();
                            bootGrowl('New Success', 'success');
                            currentGroupId = newId;
                            refreshSelect(newId);
                            notifyChange();
                        });
                    });
                },
                onCancel: function () {
                    destroySelectApi(resourceApi);
                }
            });
            setTimeout(function () {
                $('.group-name-input').focus();
                resourceApi = $('.group-resource-select').dbSelect(
                    buildResourceMultiSelectOptions(searchUrl, '', [])
                );
            }, 0);
        }

        function openEditDialog() {
            if (!canManageGroups()) {
                bootGrowl('No permission', 'danger');
                return;
            }
            if (!isRealGroupId(currentGroupId) || !searchUrl || !resourceType) {
                return;
            }
            var groupId = currentGroupId;
            var groupName = findGroupLabel(filterData, groupId) || groupId;
            loadGroupResources(searchUrl, groupId, function (preselected) {
                var initialIds = preselected.map(function (item) {
                    return item.value;
                });
                var resourceApi = null;
                var dialog = showConfirm({
                    title: 'Edit Group',
                    size: 'xlarge',
                    position: 'center',
                    body: '<div class="group-dialog-body">' +
                        '<div class="form-item"><label class="form-label">Name<strong class="text-primary">*</strong></label>' +
                        '<div class="form-control-area"><input type="text" class="form-control group-name-input" maxlength="50" value="' +
                        escapeHtml(groupName) + '"/></div></div>' +
                        '<div class="form-item mt-4"><label class="form-label">Associated resources</label>' +
                        '<select class="form-control group-resource-select"></select></div>' +
                        '</div>',
                    confirmText: 'Save',
                    cancelText: 'Cancel',
                    closeOnConfirm: false,
                    onConfirm: function () {
                        var name = ($('.group-name-input').val() || '').trim();
                        if (!name) {
                            bootGrowl('Group name cannot be empty', 'warning');
                            return;
                        }
                        var finalIds = resourceApi && resourceApi.getValues ? resourceApi.getValues().slice() : [];
                        var diff = diffIds(initialIds, finalIds);
                        doPoster('/group/edit', {id: groupId, name: name, type: resourceType}, function (editRes) {
                            if (!editRes.success) {
                                bootGrowl(editRes.message || 'Save failed', 'danger');
                                return;
                            }
                            bindBatch(groupId, resourceType, diff.toAdd, function (addOk) {
                                if (!addOk) {
                                    return;
                                }
                                unbindBatch(groupId, resourceType, diff.toRemove, function (removeOk) {
                                    if (!removeOk) {
                                        return;
                                    }
                                    destroySelectApi(resourceApi);
                                    dialog.close();
                                    bootGrowl('Saved successfully', 'success');
                                    refreshSelect(groupId);
                                    notifyChange();
                                });
                            });
                        });
                    },
                    onCancel: function () {
                        destroySelectApi(resourceApi);
                    }
                });
                setTimeout(function () {
                    $('.group-name-input').focus();
                    // Dropdown BrowseAllResource；Selected Tab Presentation GroupResource，But forklifts out
                    resourceApi = $('.group-resource-select').dbSelect(
                        buildResourceMultiSelectOptions(searchUrl, '', preselected)
                    );
                }, 0);
            });
        }

        $addBtn.on('click', openCreateDialog);
        $editBtn.on('click', openEditDialog);

        $removeBtn.on('click', function () {
            if (!canManageGroups()) {
                bootGrowl('No permission', 'danger');
                return;
            }
            if (!isRealGroupId(currentGroupId)) {
                return;
            }
            var groupName = findGroupLabel(filterData, currentGroupId) || currentGroupId;
            doPoster('/group/memberCount', {id: currentGroupId}, function (countRes) {
                var count = 0;
                if (countRes && countRes.success) {
                    var data = countRes.data || {};
                    count = data.count != null ? data.count : 0;
                }
                showConfirm({
                    title: 'Confirm to delete grouping' + escapeHtml(groupName) + '」？',
                    message: 'The group\'s down. ' + count + ' One resource, deleted and only removed from this composition (retention of the resource itself and other groups).',
                    icon: 'warning',
                    size: 'large',
                    confirmType: 'danger',
                    confirmText: 'Delete',
                    onConfirm: function () {
                        doPoster('/group/remove', {id: currentGroupId, type: resourceType}, function (response) {
                            if (!response.success) {
                                bootGrowl(response.message || 'Delete failed', 'danger');
                                return;
                            }
                            bootGrowl('Deleted successfully', 'success');
                            currentGroupId = '';
                            refreshSelect('');
                            notifyChange();
                        });
                    }
                });
            });
        });

        if (options.addButtonSelector && options.addPageUrl) {
            $(document).off('click.groupAdd', options.addButtonSelector)
                .on('click.groupAdd', options.addButtonSelector, function (e) {
                    e.preventDefault();
                    doLoader(options.addPageUrl);
                });
        }

        syncVisibility();
        refreshSelect(currentGroupId);
        return {
            getGroupId: function () {
                return currentGroupId;
            },
            getSearchParams: function (extra) {
                var params = $.extend({}, extra || {});
                // It has to be visible.groupId（This post is part of our special coverage Syria Protests 2011.NothenPaginationManager We'll keep the last one together.groupId
                params.groupId = currentGroupId || '';
                return params;
            },
            setListTotal: function (total) {
                listTotal = Number(total) || 0;
                syncVisibility();
            },
            refresh: refreshSelect
        };
    }

    window.GroupToolbar = {
        initListToolbar: initListToolbar,
        isRealGroupId: isRealGroupId,
        peekStoredGroupId: peekStoredGroupId
    };
})(window, jQuery);
