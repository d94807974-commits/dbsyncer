/**
 * jQuery Select ComponentPlugin
 * @param {Object} options - ConfigureOptions
 *   - type: 'single' or'multiple'（Default'single'）
 *   - data: Options data array[{label: '', value: ''}, ...]
 *   - defaultValue: Default SelectionValue
 *   - disabled: YesNoDisable
 *   - customButtons: Custombutton array, up to 2
 *     [{text: 'Button Text', callback: function() {}}]
 *   - onSelect: Select the echo of the change
 *   - onCustomButton: CustomTurnback on button click
 */
(function(window, $) {
    'use strict';
    
    // Global Variable: Tracking Current OpeneddbSelect Examples
    window.dbSelectCurrentOpenInstance = null;

    /**
     * Destruction of initialized contents of containersdbSelect（PageToggle front-to-face to avoid residual rolling listening)
     */
    window.destroyDbSelectsIn = function(container) {
        const $container = container ? $(container) : $(document.body);
        if (!$container.length) {
            return;
        }
        if (window.dbSelectCurrentOpenInstance && typeof window.dbSelectCurrentOpenInstance.close === 'function') {
            window.dbSelectCurrentOpenInstance.close();
        }
        $container.find('select').each(function() {
            const api = $(this).data('dbSelect');
            if (api && typeof api.destroy === 'function') {
                api.destroy();
            }
        });
    };
    
    // HTMLConversion
    function escapeHtml(text) {
        if (typeof window.escapeHtml === 'function') {
            return window.escapeHtml(text);
        }
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    // Properties Conversion
    function escapeAttr(text) {
        return String(text).replace(/[&<>"']/g, function(s) {
            return {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[s];
        });
    }
    
    $.fn.dbSelect = function(options) {
        options = options || {};
        
        const $select = $(this);
        if (!$select.length) return null;
        
        // Prevention of duplicate initialization, directBackExistingAPI Examples
        if ($select.data('dbSelectInitialized')) {
            return $select.data('dbSelect');
        }
        $select.data('dbSelectInitialized', true);

        // DefaultConfigure
        let defaultValue = options.defaultValue || null;

        // If notSettings defaultValue，Try FromHTML Medium Readoption:selected Yes.Value
        if (!defaultValue && $select.is('select')) {
            const $selectedOption = $select.find('option:selected');
            if ($selectedOption.length > 0) {
                const selectedValue = $selectedOption.val();
                if (selectedValue && selectedValue !== '') {
                    defaultValue = selectedValue;
                }
            }
        }

        const config = {
            type: options.type || 'single',
            data: options.data || [],
            defaultValue: defaultValue,
            disabled: options.disabled || false,
            maxRenderOptions: options.maxRenderOptions || 1000,
            remoteSearch: options.remoteSearch || false,
            pageSize: options.pageSize || 100,
            keywordDebounceMs: options.keywordDebounceMs || 300,
            loadOptions: options.loadOptions || null,
            customButtons: (options.customButtons || []).slice(0, 2), // Up to 2
            onSelect: options.onSelect || function() {},
            onReady: options.onReady || function() {},
            onCustomButton: options.onCustomButton || function() {}
        };

        // FromHTML Medium Read Options
        if ($select.is('select') && config.data.length === 0) {
            config.data = [];
            $select.find('option').each(function() {
                const $option = $(this);
                const item = {
                    label: $option.text(),
                    value: $option.val(),
                    disabled: $option.prop('disabled')
                };
                
                // Read Alldata-* Properties
                const dataAttrs = {};
                $.each($option[0].attributes, function(i, attr) {
                    if (attr.name.startsWith('data-')) {
                        // Willdata-type ConvertYestype，data-custom-attr ConvertYescustomAttr
                        const key = attr.name.replace(/^data-/, '').replace(/-([a-z])/g, function(g) {
                            return g[1].toUpperCase();
                        });
                        dataAttrs[key] = attr.value;
                    }
                });
                // If there is Locateddata properties, store
                if (Object.keys(dataAttrs).length > 0) {
                    item.data = dataAttrs;
                }
                
                config.data.push(item);
            });
        }

        // TermsSelect PersonsHTML（Use More OnlyID，Avoid rapid repetition of initialization conflicts)
        const timestamp = Date.now();
        const random = Math.random().toString(36).substring(2, 9);
        const selectId = 'dbsyncer-select-' + timestamp + '-' + random;
        const dropdownId = 'dbsyncer-select-dropdown-' + timestamp + '-' + random;
        
        // 从原生 select 的类名中提取 select-mini，传递到生成的组件
        let miniClass = $select.hasClass('form-control-mini') ? 'dbsyncer-select-mini' : '';
        miniClass = $select.hasClass('form-control-md') ? 'dbsyncer-select-md' : miniClass;

        const selectHTML = `
            <div class="dbsyncer-select ${config.disabled ? 'disabled' : ''} ${miniClass}" id="${selectId}">
                <div class="dbsyncer-select-trigger" data-toggle>
                    <div class="dbsyncer-select-trigger-content">
                        <span class="dbsyncer-select-trigger-text">${config.type === 'single' ? 'Please select ' : 'Please select '}</span>
                        <div class="dbsyncer-select-trigger-tags"></div>
                    </div>
                    <div class="dbsyncer-select-arrow">
                        <i class="fa fa-angle-down"></i>
                    </div>
                </div>
                <div class="dbsyncer-select-dropdown hidden" id="${dropdownId}">
                    <div class="dbsyncer-select-search-wrapper">
                        <div class="dbsyncer-select-search">
                            <i class="fa fa-search"></i>
                            <input type="text" class="dbsyncer-select-search-input" placeholder="Search...">
                            <div class="dbsyncer-select-search-clear"><i class="fa fa-times"></i></div>
                        </div>
                    </div>
                    <div class="dbsyncer-select-actions"></div>
                    <div class="dbsyncer-select-options"></div>
                    <div class="dbsyncer-select-empty hidden">Data not available</div>
                </div>
            </div>
        `;

        $select.hide();
        // After the original elementAddCustomComponent
        $select.after(selectHTML);
        const $component = $('#' + selectId);
        const $trigger = $component.find('.dbsyncer-select-trigger');
        const $dropdown = $component.find('.dbsyncer-select-dropdown');
        const $searchInput = $component.find('.dbsyncer-select-search-input');
        const $searchClear = $component.find('.dbsyncer-select-search-clear');
        const $options = $component.find('.dbsyncer-select-options');
        const $actions = $component.find('.dbsyncer-select-actions');
        const $empty = $component.find('.dbsyncer-select-empty');
        const $tags = $component.find('.dbsyncer-select-trigger-tags');
        const $text = $component.find('.dbsyncer-select-trigger-text');
        let hasRenderedOnce = false;
        let loadingRemote = false;
        let remotePageNum = 1;
        let remoteHasMore = false;
        let remoteKeyword = '';
        let keywordTimer = null;

        // SelectedValue
        let selectedValues = config.defaultValue ? (Array.isArray(config.defaultValue) ? config.defaultValue : [config.defaultValue]) : [];

        // Render Options
        function renderOptions(filterText = '') {
            $options.empty();
            const filterLower = filterText.toLowerCase();
            let hasVisible = false;
            let renderedCount = 0;
            const maxRender = Math.max(50, config.maxRenderOptions);
            const enableLimit = !filterText;

            config.data.forEach(function(item, index) {
                const matches = !filterText || item.label.toLowerCase().indexOf(filterLower) > -1;
                if (matches) {
                    hasVisible = true;
                }
                if (!matches) {
                    return;
                }
                if (enableLimit && renderedCount >= maxRender) {
                    return;
                }
                renderedCount++;

                const isSelected = selectedValues.indexOf(item.value) > -1;
                const inputType = config.type === 'single' ? 'radio' : 'checkbox';

                // Hide check box when single, display when multiple selection
                const checkboxClass = config.type === 'single' ? 'dbsyncer-select-option-checkbox hidden' : 'dbsyncer-select-option-checkbox';

                const $option = $(`
                    <label class="dbsyncer-select-option ${matches ? '' : 'hidden'} ${isSelected ? 'selected' : ''} ${item.disabled ? 'disabled' : ''}" title="${escapeHtml(item.label)}">
                        <input type="${inputType}" value="${escapeAttr(item.value)}" ${isSelected ? 'checked' : ''} ${item.disabled ? 'disabled' : ''} data-dbselect-internal>
                        <div class="${checkboxClass}"></div>
                        <span class="dbsyncer-select-option-text">${escapeHtml(item.label)}</span>
                    </label>
                `);

                $option.on('click.dbSelect-' + selectId, function(e) {
                    if (item.disabled) {
                        e.preventDefault();
                        return;
                    }
                    e.preventDefault();
                    handleOptionChange($option, item.value, item.label);
                });

                $options.append($option);
            });

            $empty.toggleClass('hidden', hasVisible);
            if (enableLimit && hasVisible && config.data.length > renderedCount) {
                $options.append(
                    $('<div class="dbsyncer-select-option disabled"></div>')
                        .text('More data, just before display ' + renderedCount + ' entry, enter keyword search')
                );
            }
            if (config.remoteSearch && loadingRemote) {
                $options.append(
                    $('<div class="dbsyncer-select-option disabled"></div>')
                        .text('Loading...')
                );
            }
            if (config.remoteSearch && !loadingRemote && !hasVisible) {
                $empty.removeClass('hidden').text('Data not available');
            }
        }

        /**
         * Merge RemoteResultAnd go heavy.
         *
         * @param {Array} items
         * @param {boolean} replace Overwrite?Available data
         */
        function mergeRemoteData(items, replace) {
            const source = Array.isArray(items) ? items : [];
            if (replace) {
                config.data = [];
            }
            const map = {};
            config.data.forEach(function(item) {
                map[item.value] = item;
            });
            source.forEach(function(item) {
                if (item && item.value !== undefined && item.value !== null) {
                    map[item.value] = item;
                }
            });
            config.data = Object.keys(map).map(function(key) {
                return map[key];
            });
        }

        /**
         * Remotely loads the drop-down option.
         *
         * @param {number} pageNum CurrentPage
         * @param {string} keyword ,
         * @param {boolean} replace YesNoReplaceAvailable data
         */
        function loadRemoteOptions(pageNum, keyword, replace) {
            if (!config.remoteSearch || typeof config.loadOptions !== 'function' || loadingRemote) {
                return;
            }
            loadingRemote = true;
            if (replace) {
                remotePageNum = 1;
                remoteHasMore = false;
            }
            renderOptions($searchInput.val());
            config.loadOptions({
                pageNum: pageNum,
                pageSize: config.pageSize,
                searchKey: keyword || ''
            }, function(result) {
                loadingRemote = false;
                const payload = result || {};
                const total = Number(payload.total || 0);
                const page = Number(payload.pageNum || pageNum || 1);
                const size = Number(payload.pageSize || config.pageSize || 50);
                const rows = Array.isArray(payload.data) ? payload.data : [];
                remotePageNum = page;
                remoteHasMore = page * size < total;
                mergeRemoteData(rows, replace);
                renderOptions($searchInput.val());
            }, function() {
                loadingRemote = false;
                renderOptions($searchInput.val());
            });
        }

        // Deal with options changes
        function handleOptionChange($option, value, label) {
            if (config.type === 'single') {
                selectedValues = [value];
                $options.find('label').removeClass('selected');
                $options.find('input[type="radio"]').prop('checked', false);
                $option.addClass('selected');
                $option.find('input[type="radio"]').prop('checked', true);
                // Auto on singleCloseDropdown Menu
                closeDropdown();
            } else {
                const index = selectedValues.indexOf(value);
                const $checkbox = $option.find('input[type="checkbox"]');
                if (index > -1) {
                    selectedValues.splice(index, 1);
                    $option.removeClass('selected');
                    $checkbox.prop('checked', false);
                } else {
                    selectedValues.push(value);
                    $option.addClass('selected');
                    $checkbox.prop('checked', true);
                }
            }

            updateDisplay();
            triggerSelectEvent();
        }

        // Sync Originalselect Yes.options
        function syncSelectOptions() {
            if (!$select.is('select')) return;
            
            // Get Currentoptions（Keep InitialHTML options，Avoidance of duplicationAdd）
            const existingOptions = {};
            $select.find('option').each(function() {
                existingOptions[$(this).val()] = true;
            });
            
            // AddMissingoptions
            config.data.forEach(function(item) {
                if (!existingOptions[item.value]) {
                    const $option = $('<option></option>')
                        .attr('value', item.value)
                        .text(item.label);
                    if (item.disabled) {
                        $option.prop('disabled', true);
                    }
                    $select.append($option);
                }
            });
        }

        // Update Display
        function updateDisplay() {
            // updateselect
            if ($select.is('select')) {
                // Make sure it's raw.select Yes.options and config.data Sync
                syncSelectOptions();
                $select.val(config.type === 'single' ? (selectedValues[0] || '') : selectedValues);
            }

            // Update tab display
            $tags.empty();

            if (selectedValues.length > 0) {
                const selectedItems = selectedValues.map(function(value) {
                    const item = config.data.find(d => d.value === value);
                    return {
                        value: value,
                        label: item ? item.label : value
                    };
                });

                if (config.type === 'single') {
                    $text.text(selectedItems[0].label).show();
                } else {
                    // Multiple selection: Before display3Tags+ Count
                    $text.hide();
                    const displayCount = 3;
                    if (selectedItems.length <= displayCount) {
                        selectedItems.forEach(function(selectedItem) {
                            const $tag = $(`<span class="dbsyncer-select-tag">${escapeHtml(selectedItem.label)}<i class="fa fa-times dbsyncer-select-tag-remove"></i></span>`);
                            $tag.find('.dbsyncer-select-tag-remove').on('click.dbSelect-' + selectId, function(e) {
                                e.stopPropagation();
                                const idx = selectedValues.indexOf(selectedItem.value);
                                if (idx > -1) {
                                    selectedValues.splice(idx, 1);
                                    updateDisplay();
                                    triggerSelectEvent();
                                    renderOptions($searchInput.val());
                                }
                            });
                            $tags.append($tag);
                        });
                    } else {
                        for (let i = 0; i < displayCount; i++) {
                            const selectedItem = selectedItems[i];
                            const $tag = $(`<span class="dbsyncer-select-tag">${escapeHtml(selectedItem.label)}<i class="fa fa-times dbsyncer-select-tag-remove"></i></span>`);
                            $tag.find('.dbsyncer-select-tag-remove').on('click.dbSelect-' + selectId, function(e) {
                                e.stopPropagation();
                                const idx = selectedValues.indexOf(selectedItem.value);
                                if (idx > -1) {
                                    selectedValues.splice(idx, 1);
                                    updateDisplay();
                                    triggerSelectEvent();
                                    renderOptions($searchInput.val());
                                }
                            });
                            $tags.append($tag);
                        }
                        const count = selectedItems.length - displayCount;
                        $tags.append(`<span class="dbsyncer-select-count">+${count}</span>`);
                    }
                }
            } else {
                // No selectionValueShow timeNoticeText
                $text.text('Please select ').show();
            }
        }

        // Trigger SelectionEvent
        function triggerSelectEvent() {
            // RewindFunctions
            if (typeof config.onSelect === 'function') {
                config.onSelect(selectedValues, config.type);
            }
            // TriggerCustomEvent
            $component.trigger('dbselect:change', [selectedValues]);
        }

        // Open Dropdown Menu
        function openDropdown() {
            if (!config.disabled) {
                // FirstCloseOtherOpendbSelect Examples
                if (window.dbSelectCurrentOpenInstance && window.dbSelectCurrentOpenInstance !== api) {
                    window.dbSelectCurrentOpenInstance.close();
                }
                
                $component.addClass('open');
                $dropdown.removeClass('hidden');
                
                // Smart positioning: testingYesNoNeed to expand upwards
                adjustDropdownPosition();
                
                $searchInput.focus();
                if (config.remoteSearch) {
                    // Remote mode: first open load; if currentNoneData is also allowed to be loaded again (e.g. the last precision query is empty, resulting in drop-down blanks)
                    if (!hasRenderedOnce || !config.data || config.data.length === 0) {
                        loadRemoteOptions(1, $searchInput.val() || '', true);
                        hasRenderedOnce = true;
                    }
                } else if (!hasRenderedOnce) {
                    renderOptions('');
                    hasRenderedOnce = true;
                }
                
                // Update the current UNJSPF case
                window.dbSelectCurrentOpenInstance = api;
            }
        }

        function isComponentAttached() {
            const el = $component[0];
            return !!(el && document.body && document.body.contains(el));
        }

        function unbindWindowEvents() {
            $(window).off('scroll.dbSelect-' + selectId);
            $(window).off('resize.dbSelect-' + selectId);
        }

        // Adjust the dropdown menu position (smart up/ Units UNCLOS oppose)
        function adjustDropdownPosition() {
            if (!isComponentAttached()) {
                return;
            }
            const $trigger = $component.find('.dbsyncer-select-trigger');
            const triggerEl = $trigger[0];
            if (!triggerEl) {
                return;
            }
            const triggerRect = triggerEl.getBoundingClientRect();
            const dropdownHeight = $dropdown.outerHeight() || 300; // Default Max height
            const viewportHeight = window.innerHeight;
            const spaceBelow = viewportHeight - triggerRect.bottom;
            const spaceAbove = triggerRect.top;
            
            // Remove Previous Positioning Situations
            $dropdown.removeClass('dbsyncer-select-dropdown-up');
            
            // If the lower space is insufficient and the upper space is sufficient, spread upward
            if (spaceBelow < dropdownHeight && spaceAbove > spaceBelow) {
                $dropdown.addClass('dbsyncer-select-dropdown-up');
            }
        }

        // CloseDropdown Menu
        function closeDropdown() {
            $component.removeClass('open');
            $dropdown.addClass('hidden');
            $searchInput.val('');
            $searchClear.removeClass('active');
            
            // If this is the case,YesAgricultural technology is not an option.ClearGlobal variables
            if (window.dbSelectCurrentOpenInstance === api) {
                window.dbSelectCurrentOpenInstance = null;
            }
        }

        // Scroll and size changes in the listening window, repositioning the dropdown menu
        function handleWindowEvents() {
            if (!isComponentAttached()) {
                closeDropdown();
                unbindWindowEvents();
                return;
            }
            if (!$dropdown.hasClass('hidden')) {
                adjustDropdownPosition();
            }
        }

        // Tie Specific WindowEvent（Frente 15 Apartments
        $(window).on('scroll.dbSelect-' + selectId, handleWindowEvents);
        $(window).on('resize.dbSelect-' + selectId, handleWindowEvents);
        
        // Remove component when destroyedEventListen
        $select.on('remove', function() {
            unbindWindowEvents();
        });

        // Select All
        function selectAll() {
            if (config.type === 'multiple') {
                const filterText = $searchInput.val().toLowerCase();
                // If there is.SearchFilter Conditions，Select only filter visible options
                if (filterText) {
                    config.data.forEach(function(item) {
                        const matches = item.label.toLowerCase().indexOf(filterText) > -1;
                        if (matches && !item.disabled && selectedValues.indexOf(item.value) === -1) {
                            selectedValues.push(item.value);
                        }
                    });
                } else {
                    // Auditors Growth
                    config.data.forEach(function(item) {
                        if (!item.disabled && selectedValues.indexOf(item.value) === -1) {
                            selectedValues.push(item.value);
                        }
                    });
                }
                updateDisplay();
                triggerSelectEvent();
                renderOptions($searchInput.val());
            }
        }

        // CancelSelect All
        function deselectAll() {
            selectedValues = [];
            updateDisplay();
            triggerSelectEvent();
            renderOptions($searchInput.val());
        }

        // TieEvent
        $trigger.on('click.dbSelect-' + selectId, function(e) {
            e.stopPropagation();
            if ($dropdown.hasClass('hidden')) {
                openDropdown();
            } else {
                closeDropdown();
            }
        });

        // Search
        $searchInput.on('input.dbSelect-' + selectId, function() {
            const text = $(this).val();
            $searchClear.toggleClass('active', text.length > 0);
            if (config.remoteSearch) {
                remoteKeyword = text;
                if (keywordTimer) {
                    clearTimeout(keywordTimer);
                }
                keywordTimer = setTimeout(function() {
                    loadRemoteOptions(1, remoteKeyword, true);
                }, config.keywordDebounceMs);
            } else {
                renderOptions(text);
            }
        });

        // ClearSearch
        $searchClear.on('click.dbSelect-' + selectId, function(e) {
            e.stopPropagation();
            $searchInput.val('').focus();
            $searchClear.removeClass('active');
            if (config.remoteSearch) {
                loadRemoteOptions(1, '', true);
            } else {
                renderOptions('');
            }
        });

        // Scroll-down touch bottom loadNext
        $options.on('scroll.dbSelect-' + selectId, function() {
            if (!config.remoteSearch || loadingRemote || !remoteHasMore) {
                return;
            }
            const el = this;
            const nearBottom = el.scrollTop + el.clientHeight >= el.scrollHeight - 20;
            if (nearBottom) {
                loadRemoteOptions(remotePageNum + 1, remoteKeyword, false);
            }
        });

        // Click NextClose
        $(document).on('click.dbselect_' + selectId, function(e) {
            if (!$component.is(e.target) && $component.has(e.target).length === 0) {
                closeDropdown();
            }
        });

        // Actionsbutton
        if (config.type === 'multiple') {
            // AddDefault button
            const $selectAllBtn = $(`<button class="dbsyncer-select-action-btn">Select All</button>`);
            const $deselectAllBtn = $(`<button class="dbsyncer-select-action-btn">CancelSelect All</button>`);

            $selectAllBtn.on('click.dbSelect-' + selectId, function(e) {
                e.preventDefault();
                e.stopPropagation();
                selectAll();
            });

            $deselectAllBtn.on('click.dbSelect-' + selectId, function(e) {
                e.preventDefault();
                e.stopPropagation();
                deselectAll();
            });

            $actions.append($selectAllBtn).append($deselectAllBtn);
        }

        // AddCustombutton
        config.customButtons.forEach(function(btn, index) {
            const $btn = $(`<button class="dbsyncer-select-action-btn">${escapeHtml(btn.text)}</button>`);
            $btn.on('click.dbSelect-' + selectId, function(e) {
                e.preventDefault();
                e.stopPropagation();
                if (typeof btn.callback === 'function') {
                    btn.callback(selectedValues);
                }
                // TriggerCustomEvent
                $component.trigger('dbselect:button', [index, selectedValues]);
                // Call Global Back
                if (typeof config.onCustomButton === 'function') {
                    config.onCustomButton(index, selectedValues, btn.text);
                }
            });
            $actions.append($btn);
        });

        // Initialise Display
        updateDisplay();

        // If notSettingsDefault value，InspectionYesNoYes Notably.selected Includes:
        if (selectedValues.length === 0 && $select.is('select')) {
            // InspectionYesNoYes Notably.selected Properties Options
            const $selectedOptions = $select.find('option:selected');
            if ($selectedOptions.length > 0) {
                selectedValues = [];
                $selectedOptions.each(function() {
                    const val = $(this).val();
                    if (val && val !== '') {
                        selectedValues.push(val);
                    }
                });
            } else {
                // Nothing.selected Properties, select the first non-empty option by default in single mode
                if (config.type === 'single') {
                    const $firstOption = $select.find('option').filter(function() {
                        return $(this).val() && $(this).val() !== '';
                    }).first();
                    if ($firstOption.length) {
                        selectedValues = [$firstOption.val()];
                    }
                }
            }
            // Update Display
            if (selectedValues.length > 0) {
                updateDisplay();
                renderOptions('');
                triggerSelectEvent();
            }
        }

        // TermsAPI Object
        const api = {
            $element: $select,
            $component: $component,
            getValues: function() { return selectedValues; },
            setValues: function(values, silent) {
                const inputValues = Array.isArray(values) ? values : [values];
                if (config.remoteSearch) {
                    // Allow first selection in remote modeValue，AvoidObjective CombinedUnmounted/47%ValueLost
                    selectedValues = inputValues.filter(function(value) {
                        return value !== undefined && value !== null && value !== '';
                    });
                } else {
                    // Filter OutNoneEffectValue（Only inconfig.data ExistenceValue）
                    selectedValues = inputValues.filter(function(value) {
                        return config.data.some(function(item) {
                            return item.value === value;
                        });
                    });
                }
                updateDisplay();
                // NoneDraw MenuYesNoOpen, Update Options SIStatus
                renderOptions($searchInput.val());
                if (!silent) {
                    triggerSelectEvent();
                }
                return this;
            },
            setData: function(newData) {
                // Update Options Advancement Units
                config.data = newData || [];
                const oldSelectedValues = (selectedValues || []).slice();
                
                // IfYesOriginalselect Elements,Clearand refilloptions
                if ($select.is('select')) {
                    $select.empty();
                    config.data.forEach(function(item) {
                        const $option = $('<option></option>')
                            .attr('value', item.value)
                            .text(item.label);
                        if (item.disabled) {
                            $option.prop('disabled', true);
                        }
                        $select.append($option);
                    });
                }
                
                // ClearSearchExpenses Extended
                $searchInput.val('');
                remoteKeyword = '';
                remotePageNum = 1;
                remoteHasMore = false;
                // Remote Mode is selected to avoid restartingSearchThen lost RESOLUTION
                if (config.remoteSearch) {
                    selectedValues = oldSelectedValues.filter(function(value) {
                        return value !== undefined && value !== null && value !== '';
                    });
                } else {
                    // Rebuild selected non-remote mode by local dataValue
                    selectedValues = oldSelectedValues.filter(function(value) {
                        return config.data.some(function(item) {
                            return item && item.value === value;
                        });
                    });
                    // ReinitiateDefault value
                    if (selectedValues.length === 0 && config.defaultValue) {
                        selectedValues = Array.isArray(config.defaultValue) ? config.defaultValue : [config.defaultValue];
                    }
                    if (selectedValues.length === 0 && config.data.length > 0 && config.type === 'single') {
                        const firstItem = config.data.find(function(d) {
                            return d && d.value && d.value !== '';
                        });
                        if (firstItem) {
                            selectedValues = [firstItem.value];
                        }
                    }
                }
                // Render
                updateDisplay();
                renderOptions('');
                triggerSelectEvent();
                return this;
            },
            getData: function() {
                return config.data;
            },
            // Get the full selectionInfo（Includingdata Properties)
            getSelectedItems: function() {
                return selectedValues.map(function(value) {
                    const item = config.data.find(d => d.value === value);
                    if (item) {
                        return {
                            value: item.value,
                            label: item.label,
                            data: item.data || {}
                        };
                    }
                    return { value: value, label: value, data: {} };
                });
            },
            // Fetch specified attributes for the selected itemValue
            getAttribute: function(attributeName, value) {
                // If it's specified,value，Get thatValueproperties;NoFetches the properties of the first selected item
                const targetValue = value !== undefined ? value : (selectedValues[0] || null);
                if (!targetValue) return null;
                
                const item = config.data.find(d => d.value === targetValue);
                if (item && item.data) {
                    return item.data[attributeName] || null;
                }
                return null;
            },
            clear: function() {
                selectedValues = [];
                updateDisplay();
                renderOptions('');
                triggerSelectEvent();
                return this;
            },
            close: function() {
                closeDropdown();
                return this;
            },
            /**
             * Manually in remote modeRefreshDatadoubt (for loved ones)Cancel Filter/Reload.
             *
             * @param {string} keyword SearchKeyword，Empty string meansClearSearch
             * @returns {*}
             */
            reloadRemote: function(keyword) {
                if (!config.remoteSearch) {
                    return this;
                }
                const nextKeyword = keyword === undefined || keyword === null ? '' : String(keyword);
                remoteKeyword = nextKeyword;
                $searchInput.val(nextKeyword);
                $searchClear.toggleClass('active', nextKeyword.length > 0);
                loadRemoteOptions(1, nextKeyword, true);
                return this;
            },
            destroy: function() {
                // FirstCloseDropdown Menu
                closeDropdown();
                
                // Remove Document LevelEventListen
                $(document).off('click.dbselect_' + selectId);
                
                // Remove WindowEventListen
                unbindWindowEvents();
                
                // Remove all named spaces in the componentEventListening (including all sub-elements)
                if ($component && $component.length) {
                    // Clear the components and all their sub-elements.Event
                    // Use namespace to clean up only current casesEvent
                    const namespace = '.dbSelect-' + selectId;
                    $component.off(namespace);
                    $component.find('*').off(namespace);
                }
                
                // Remove ComponentDOM（It's gonna clean up everything.Event）
                if ($component && $component.length) {
                    $component.remove();
                }
                
                // Show Originalselect Elements
                if ($select && $select.length) {
                    $select.show();
                }
                
                // Hey 95! Clear Data
                $select.removeData('dbSelectInitialized');
                $select.removeData('dbSelect');
                // If it's destroyedYesThe current example is open.ClearGlobal variables
                if (window.dbSelectCurrentOpenInstance === this) {
                    window.dbSelectCurrentOpenInstance = null;
                }
                return this;
            }
        };

        // Save API To Component and Original Elements
        $component.data('dbSelect', api);
        $select.data('dbSelect', api);
        // Trigger InitializationFinishEvent
        config.onReady(selectedValues);

        return api;
    };
    
})(window, jQuery);