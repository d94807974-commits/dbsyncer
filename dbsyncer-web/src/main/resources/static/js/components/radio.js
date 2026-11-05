/**
 * Radio Group Beautiful.Round Single Component
 * Keep the traditional circle.radio Appearance, but more elegant.
 * Usage:$('.radio-group').radioGroup(options)
 * 
 * @param {Object} options - ConfigureOptions
 *   - theme: Theme Colour('primary', 'success', 'warning', 'danger', 'info')，Default'primary'
 *   - size: Dimensions('sm', 'md', 'lg')，Default'md'
 *   - layout: Layout('horizontal', 'vertical', 'inline')，Default'horizontal'
 *   - bordered: YesNoShow border style, defaultfalse
 *   - onChange: Echo on selected changesFunctions，ParameterYes(value, label)
 * 
 * @example
 * // Basic use
 * const radioGroup = $('#myRadioGroup').radioGroup({
 *     theme: 'success',
 *     size: 'lg',
 *     bordered: true,
 *     onChange: function(value, label) {
 *         console.log('Organisation', value, label);
 *     }
 * });
 * // Get CurrentValue
 * const value = radioGroup.getValue();
 * // SettingsValue
 * radioGroup.setValue('full');
 * 
 * @example
 * // HTML Structure
 * <div class="radio-group" id="myRadioGroup">
 *     <input type="radio" name="mode" value="full" checked />
 *     <label>Full</label>
 *     <input type="radio" name="mode" value="increment" />
 *     <label>Incremental</label>
 *     <input type="radio" name="mode" value="fullToIncrement" disabled />
 *     <label>Full + incremental</label>
 * </div>
 */
(function($) {
    'use strict';
    
    // HTMLConversionFunctions（If not defined globally, use local definition)
    function escapeHtml(text) {
        if (typeof window.escapeHtml === 'function') {
            return window.escapeHtml(text);
        }
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    $.fn.radioGroup = function(options) {
        options = options || {};
        
        // DefaultConfigure
        const config = $.extend({
            theme: 'primary',
            size: 'md',
            layout: 'horizontal',
            bordered: false,
            onChange: null
        }, options);
        
        // If there's only one element, direct.Back API Examples
        if (this.length === 1) {
            const $container = $(this[0]);
            
            // Prevention of duplicate initialization, directBackExistingAPI Examples
            if ($container.data('radioGroupInitialized')) {
                return $container.data('radioGroup');
            }
            $container.data('radioGroupInitialized', true);
            
            return initRadioGroup($container, config);
        }
        
        // Multiple elements,Back jQuery Object (reverse compatibility)
        return this.each(function() {
            const $container = $(this);
            
            // Prevention of duplicate initialization
            if ($container.data('radioGroupInitialized')) {
                return;
            }
            $container.data('radioGroupInitialized', true);
            
            initRadioGroup($container, config);
        });
    };
    
    // InitializeradioGroup InternalFunctions
    function initRadioGroup($container, config) {
        // AddTheme and Size Category
        if (config.theme && config.theme !== 'primary') {
            $container.addClass('radio-group-' + config.theme);
        }
        if (config.size && config.size !== 'md') {
            $container.addClass('radio-group-' + config.size);
        }
        
        // AddLayout Class
        if (config.layout === 'vertical') {
            $container.addClass('radio-group-vertical');
        } else if (config.layout === 'inline') {
            $container.addClass('radio-group-inline');
        }
        
        // AddBorder Styles
        if (config.bordered) {
            $container.addClass('radio-group-bordered');
        }
        
        // Get Allradio And the corresponding.label
        const $radios = $container.find('input[type="radio"]');
        if ($radios.length === 0) {
            return null;
        }
        
        // Pack them all.radio andlabel
        $radios.each(function() {
            const $radio = $(this);
            const $label = $radio.next('label');
            
            // Create packing elements
            const $item = $('<div class="radio-group-item"></div>');
            
            // AddDisableStatus
            if ($radio.prop('disabled')) {
                $item.addClass('disabled');
            }
            
            // AddSelectStatus
            const isChecked = $radio.prop('checked');
            if (isChecked) {
                $item.addClass('active');
            }
            
            // Create label content
            const labelText = $label.length ? $label.text() : $radio.val();
            const $itemLabel = $('<span class="radio-group-item-label">' + escapeHtml(labelText) + '</span>');
            
            // Willradio and labels into packaging elements
            $item.append($radio);
            $item.append($itemLabel);
            
            // Remove Originallabel（Other Organiser
            if ($label.length) {
                $label.remove();
            }
            
            // Insert packaging elements into the container
            $container.append($item);
        });
        
        // Ensure Initial SelectionStatus54
        setTimeout(function() {
            const $checked = $container.find('input[type="radio"]:checked');
            if ($checked.length) {
                $container.find('.radio-group-item').removeClass('active');
                $checked.closest('.radio-group-item').addClass('active');
            }
        }, 0);
        
        // EventOther Organiserradio
        $container.on('click', '.radio-group-item:not(.disabled)', function() {
            const $item = $(this);
            const $radio = $item.find('input[type="radio"]');
            // CancelAll SelectedStatus
            $container.find('.radio-group-item').removeClass('active');
            // SettingsCurrent item is selected
            $item.addClass('active');
            $radio.prop('checked', true).trigger('change');
        });
        
        // Listenradio ChangeEvent（Support external processes directlyActions radio）
        $container.on('change', 'input[type="radio"]', function() {
            const $radio = $(this);
            const $item = $radio.closest('.radio-group-item');
            
            // Update selection of all itemsStatus
            $container.find('.radio-group-item').removeClass('active');
            $item.addClass('active');
            
            // Trigger echo
            if (config.onChange && typeof config.onChange === 'function') {
                const value = $radio.val();
                const label = $item.find('.radio-group-item-label').text();
                config.onChange(value, label);
            }
        });
        
        // Keyboard Navigation Support
        $container.on('keydown', 'input[type="radio"]', function(e) {
            const $current = $(this).closest('.radio-group-item');
            let $target = null;
            
            if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
                // Next
                $target = $current.nextAll('.radio-group-item:not(.disabled)').first();
                if (!$target.length) {
                    $target = $container.find('.radio-group-item:not(.disabled)').first();
                }
                e.preventDefault();
            } else if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') {
                // Previous Contact
                $target = $current.prevAll('.radio-group-item:not(.disabled)').first();
                if (!$target.length) {
                    $target = $container.find('.radio-group-item:not(.disabled)').last();
                }
                e.preventDefault();
            }
            
            if ($target && $target.length) {
                $target.find('input[type="radio"]').focus().click();
            }
        });
        
        // API Methodology
        const api = {
            getValue: function() {
                return $container.find('input[type="radio"]:checked').val();
            },
            setValue: function(value) {
                const $radio = $container.find('input[type="radio"][value="' + value + '"]');
                if ($radio.length && !$radio.prop('disabled')) {
                    $radio.trigger('click');
                }
                return this;
            },
            disable: function(value) {
                if (value === undefined) {
                    // DisableAll
                    $container.find('.radio-group-item').addClass('disabled');
                    $container.find('input[type="radio"]').prop('disabled', true);
                } else {
                    // DisableAssignValue
                    const $radio = $container.find('input[type="radio"][value="' + value + '"]');
                    $radio.prop('disabled', true);
                    $radio.closest('.radio-group-item').addClass('disabled');
                }
                return this;
            },
            enable: function(value) {
                if (value === undefined) {
                    // EnableAll
                    $container.find('.radio-group-item').removeClass('disabled');
                    $container.find('input[type="radio"]').prop('disabled', false);
                } else {
                    // EnableAssignValue
                    const $radio = $container.find('input[type="radio"][value="' + value + '"]');
                    $radio.prop('disabled', false);
                    $radio.closest('.radio-group-item').removeClass('disabled');
                }
                return this;
            },
            destroy: function() {
                $container.off('click change keydown');
                $container.find('.radio-group-item').each(function() {
                    const $item = $(this);
                    const $radio = $item.find('input[type="radio"]');
                    const $label = $('<label>' + $item.find('.radio-group-item-label').text() + '</label>');
                    $radio.insertBefore($item);
                    $label.insertAfter($radio);
                    $item.remove();
                });
                $container.removeData('radioGroupInitialized');
                $container.removeClass(function(index, className) {
                    return (className.match(/\bradio-group-\S+/g) || []).join(' ');
                });
                return this;
            }
        };
        
        // Save API Present.data
        $container.data('radioGroup', api);
        
        return api;
    }
    
})(jQuery);
