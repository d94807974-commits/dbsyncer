/**
 * Checkbox Group Beautiful.Square checkbox component
 * Keep the traditional square.checkbox Appearance, but more elegant.
 * 
 * @param {Object} options - ConfigureOptions
 *   - theme: Theme Colour('primary', 'success', 'warning', 'danger', 'info')，Default'primary'
 *   - size: Dimensions('sm', 'md', 'lg')，Default'md'
 *   - layout: Layout('horizontal', 'vertical', 'inline')，Default'horizontal'
 *   - bordered: YesNoShow border style, defaultfalse
 *   - showSelectAll: YesNoShowSelect All/CancelSelect Allbutton (checkbox group), defaultfalse
 *   - onChange: Echo on selected changesFunctions，ParameterYes(values, labels)
 *
 * Usage:
 *   - $('input[type="checkbox"]').checkboxGroup(options)  // Direct Selectioninput
 * @example
 * $('.tableGroupCheckbox').checkboxGroup({
 *     theme: 'info',
 *     onChange: function(values) {
 *         console.log('Organisation', values);
 *     }
 * });
 */
(function($) {
    'use strict';
    
    // HTMLConversionFunctions
    function escapeHtml(text) {
        if (typeof window.escapeHtml === 'function') {
            return window.escapeHtml(text);
        }
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    // Create Singlecheckbox Packaging
    function wrapCheckbox($checkbox, config) {
        const $label = $checkbox.next('label');
        
        // Create packing elements
        const $item = $('<div class="checkbox-group-item"></div>');
        
        // AddDisableStatus
        if ($checkbox.prop('disabled')) {
            $item.addClass('disabled');
        }
        
        // AddSelectStatus
        const isChecked = $checkbox.prop('checked');
        if (isChecked) {
            $item.addClass('active');
        }
        
        // Create label content
        let labelText = '';
        if ($label.length) {
            labelText = $label.text();
        } else if ($checkbox.attr('id')) {
            const $labelFor = $('label[for="' + $checkbox.attr('id') + '"]');
            if ($labelFor.length) {
                labelText = $labelFor.text();
            } else {
                labelText = $checkbox.val() || '';
            }
        } else {
            labelText = $checkbox.val() || '';
        }
        
        const $itemLabel = $('<span class="checkbox-group-item-label">' + escapeHtml(labelText) + '</span>');
        
        // First.checkbox Organisationitem
        $checkbox.before($item);
        
        // ♪ And then will ♪checkbox and labels into packaging elements
        $item.append($checkbox);
        $item.append($itemLabel);
        
        // Remove Originallabel
        if ($label.length) {
            $label.remove();
        }
        
        return $item;
    }
    
    // Bind checkbox change events
    function bindCheckboxEvents($container, $item, $checkbox, config, isSingle) {
        // Switch when clicking itemscheckbox Status
        $item.on('click', function(e) {
            if ($item.hasClass('disabled')) {
                e.preventDefault();
                return;
            }
            
            // Toggle SelectionStatus
            const isChecked = $checkbox.prop('checked');
            if (isChecked) {
                $item.removeClass('active');
                $checkbox.prop('checked', false);
            } else {
                $item.addClass('active');
                $checkbox.prop('checked', true);
            }
            
            $checkbox.trigger('change');
            
            // Trigger echo
            if (config.onChange && typeof config.onChange === 'function') {
                if (isSingle) {
                    // Singlecheckbox，Direct PassValue
                    config.onChange($checkbox.prop('checked') ? [$checkbox.val()] : [], 
                                   $checkbox.prop('checked') ? [$item.find('.checkbox-group-item-label').text()] : []);
                } else {
                    // Multiplecheckbox，We need to collect everything.Value
                    triggerChangeEvent($container, config);
                }
            }
        });
        
        // Listencheckbox ChangeEvent
        $checkbox.on('change', function() {
            if ($checkbox.prop('checked')) {
                $item.addClass('active');
            } else {
                $item.removeClass('active');
            }
            
            if (!isSingle && config.onChange && typeof config.onChange === 'function') {
                triggerChangeEvent($container, config);
            }
        });
    }
    
    // Trigger ChangeEvent（(Container mode)
    function triggerChangeEvent($container, config) {
        const selectedValues = [];
        const selectedLabels = [];
        
        $container.find('input[type="checkbox"]:checked').each(function() {
            const $checkbox = $(this);
            selectedValues.push($checkbox.val());
            const $item = $checkbox.closest('.checkbox-group-item');
            selectedLabels.push($item.find('.checkbox-group-item-label').text());
        });
        
        if (config.onChange && typeof config.onChange === 'function') {
            config.onChange(selectedValues, selectedLabels);
        }
        
        $container.trigger('checkboxgroup:change', [selectedValues, selectedLabels]);
    }
    
    $.fn.checkboxGroup = function(options) {
        // DefaultConfigure
        const config = $.extend({
            theme: 'primary',
            size: 'md',
            layout: 'horizontal',
            bordered: false,
            showSelectAll: false,
            onChange: null
        }, options);
        
        return this.each(function() {
            const $element = $(this);
            
            // If it's a direct choice,Yes input[type="checkbox"]，Simplified processing
            if ($element.is('input[type="checkbox"]')) {
                // Prevention of duplicate initialization
                if ($element.data('checkboxGroupInitialized')) {
                    return;
                }
                $element.data('checkboxGroupInitialized', true);
                
                // Create a container (if Selected)
                let $container = $element.closest('.checkbox-group');
                if (!$container.length) {
                    $container = $('<div class="checkbox-group"></div>');
                    $element.before($container);
                    $container.append($element);
                }
                
                // AddTheme and Size Category
                if (config.theme && config.theme !== 'primary') {
                    $container.addClass('checkbox-group-' + config.theme);
                }
                if (config.size && config.size !== 'md') {
                    $container.addClass('checkbox-group-' + config.size);
                }
                
                // Create Packaging
                const $item = wrapCheckbox($element, config);
                
                // TieEvent
                bindCheckboxEvents($container, $item, $element, config, true);
                
                // Create SimpleAPI
                const api = {
                    getValue: function() {
                        return $element.prop('checked') ? $element.val() : null;
                    },
                    setValue: function(checked) {
                        $element.prop('checked', !!checked);
                        if (checked) {
                            $item.addClass('active');
                        } else {
                            $item.removeClass('active');
                        }
                        $element.trigger('change');
                        return this;
                    },
                    disable: function() {
                        $item.addClass('disabled');
                        $element.prop('disabled', true);
                        return this;
                    },
                    enable: function() {
                        $item.removeClass('disabled');
                        $element.prop('disabled', false);
                        return this;
                    }
                };
                
                $container.data('checkboxGroup', api);
                $element.data('checkboxGroup', api);
                
                return;
            }
            
            // Original packaging treatment
            const $container = $element;
            
            // Prevention of duplicate initialization
            if ($container.data('checkboxGroupInitialized')) {
                return;
            }
            $container.data('checkboxGroupInitialized', true);
            
            // AddTheme and Size Category
            if (config.theme && config.theme !== 'primary') {
                $container.addClass('checkbox-group-' + config.theme);
            }
            if (config.size && config.size !== 'md') {
                $container.addClass('checkbox-group-' + config.size);
            }
            
            // AddLayout Class
            if (config.layout === 'vertical') {
                $container.addClass('checkbox-group-vertical');
            } else if (config.layout === 'inline') {
                $container.addClass('checkbox-group-inline');
            }
            
            // AddBorder Styles
            if (config.bordered) {
                $container.addClass('checkbox-group-bordered');
            }
            
            // Get Allcheckbox
            const $checkboxes = $container.find('input[type="checkbox"]');
            
            if ($checkboxes.length === 0) {
                console.warn('[checkboxGroup] No checkbox elements found');
                return;
            }
            
            // CreateActions1) Button Area (if required
            let $actions = null;
            if (config.showSelectAll && $checkboxes.length > 1) {
                $actions = $('<div class="checkbox-group-actions"></div>');
                const $selectAllBtn = $('<button type="button" class="checkbox-group-action-btn">Select All</button>');
                const $deselectAllBtn = $('<button type="button" class="checkbox-group-action-btn">Unselect All</button>');
                
                $selectAllBtn.on('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    selectAll();
                });
                
                $deselectAllBtn.on('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    deselectAll();
                });
                
                $actions.append($selectAllBtn).append($deselectAllBtn);
                $container.prepend($actions);
            }
            
            // Pack them all.checkbox
            $checkboxes.each(function() {
                const $checkbox = $(this);
                const $item = wrapCheckbox($checkbox, config);
                bindCheckboxEvents($container, $item, $checkbox, config, false);
            });
            
            // Select AllFunctions
            function selectAll() {
                $container.find('.checkbox-group-item:not(.disabled)').each(function() {
                    const $item = $(this);
                    const $checkbox = $item.find('input[type="checkbox"]');
                    if (!$checkbox.prop('checked')) {
                        $item.addClass('active');
                        $checkbox.prop('checked', true).trigger('change');
                    }
                });
                triggerChangeEvent($container, config);
            }
            
            // CancelSelect AllFunctions
            function deselectAll() {
                $container.find('.checkbox-group-item').each(function() {
                    const $item = $(this);
                    const $checkbox = $item.find('input[type="checkbox"]');
                    $item.removeClass('active');
                    $checkbox.prop('checked', false).trigger('change');
                });
                triggerChangeEvent($container, config);
            }
            
            // API Methodology
            const api = {
                getValues: function() {
                    const values = [];
                    $container.find('input[type="checkbox"]:checked').each(function() {
                        values.push($(this).val());
                    });
                    return values;
                },
                getLabels: function() {
                    const labels = [];
                    $container.find('input[type="checkbox"]:checked').each(function() {
                        const $item = $(this).closest('.checkbox-group-item');
                        labels.push($item.find('.checkbox-group-item-label').text());
                    });
                    return labels;
                },
                setValues: function(values) {
                    values = Array.isArray(values) ? values : [values];
                    $container.find('input[type="checkbox"]').each(function() {
                        const $checkbox = $(this);
                        const value = $checkbox.val();
                        const shouldCheck = values.indexOf(value) > -1;
                        const $item = $checkbox.closest('.checkbox-group-item');
                        
                        if (shouldCheck && !$checkbox.prop('disabled')) {
                            $item.addClass('active');
                            $checkbox.prop('checked', true);
                        } else {
                            $item.removeClass('active');
                            $checkbox.prop('checked', false);
                        }
                    });
                    triggerChangeEvent($container, config);
                    return this;
                },
                selectAll: function() {
                    selectAll();
                    return this;
                },
                deselectAll: function() {
                    deselectAll();
                    return this;
                },
                disable: function(value) {
                    if (value === undefined) {
                        $container.find('.checkbox-group-item').addClass('disabled');
                        $container.find('input[type="checkbox"]').prop('disabled', true);
                    } else {
                        const $checkbox = $container.find('input[type="checkbox"][value="' + value + '"]');
                        $checkbox.prop('disabled', true);
                        $checkbox.closest('.checkbox-group-item').addClass('disabled');
                    }
                    return this;
                },
                enable: function(value) {
                    if (value === undefined) {
                        $container.find('.checkbox-group-item').removeClass('disabled');
                        $container.find('input[type="checkbox"]').prop('disabled', false);
                    } else {
                        const $checkbox = $container.find('input[type="checkbox"][value="' + value + '"]');
                        $checkbox.prop('disabled', false);
                        $checkbox.closest('.checkbox-group-item').removeClass('disabled');
                    }
                    return this;
                },
                destroy: function() {
                    $container.off('click change keydown');
                    if ($actions) {
                        $actions.remove();
                    }
                    $container.find('.checkbox-group-item').each(function() {
                        const $item = $(this);
                        const $checkbox = $item.find('input[type="checkbox"]');
                        const labelText = $item.find('.checkbox-group-item-label').text();
                        const $label = $('<label>' + labelText + '</label>');
                        
                        if ($checkbox.attr('id')) {
                            $label.attr('for', $checkbox.attr('id'));
                        }
                        
                        $checkbox.insertBefore($item);
                        $label.insertAfter($checkbox);
                        $item.remove();
                    });
                    $container.removeData('checkboxGroupInitialized');
                    $container.removeClass(function(index, className) {
                        return (className.match(/\bcheckbox-group-\S+/g) || []).join(' ');
                    });
                    return this;
                }
            };
            
            $container.data('checkboxGroup', api);
        });
    };
    
})(jQuery);
