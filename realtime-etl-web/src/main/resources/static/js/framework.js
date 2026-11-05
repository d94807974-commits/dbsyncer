// ******************* Initialize*****************************
const $location = (window.location + '').split('/');
const $path = document.location.pathname;
const $basePath = $location[0] + '//' + $location[2] + $path.substr(0, $path.substr(1).indexOf("/") + 1);
const $mainContent = $('#mainContent');

// ToolsFunctions
function showLoading() {
    $mainContent.html('<div class="loading"><div class="loading-spinner"></div>Loading...</div>');
}
function hideLoading() {
    $mainContent.find('.loading').remove();
}

// HTMLConversion
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(time) {
    let timestamp = Number(time);
    const date = new Date(timestamp);
    const YY = date.getFullYear() + '-';
    const MM = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '-';
    const DD = (date.getDate() < 10 ? '0' + (date.getDate()) : date.getDate());
    const hh = (date.getHours() < 10 ? '0' + date.getHours() : date.getHours()) + ':';
    const mm = (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes()) + ':';
    const ss = (date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds());
    return YY + MM + DD + " " + hh + mm + ss;
}

/**
 * Calculate relativeTimeText
 * @param {string|number} time - TimeString orTimePoke
 * @returns {string} RelativeTimeText such as:30Second、1A minute ago.、2Hour ago、Yesterday.、2Day before、3Months ago Peoples、NMonths ago Peoples
 */
function formatRelativeTime(time) {
    if (!time) {
        return '';
    }
    
    const now = new Date();
    const targetTime = new Date(time);
    
    // IfTimeNoneEffectBackEmpty String
    if (isNaN(targetTime.getTime())) {
        return '';
    }
    
    const diffMs = now.getTime() - targetTime.getTime();
    const diffSeconds = Math.floor(diffMs / 1000);
    const diffMinutes = Math.floor(diffSeconds / 60);
    const diffHours = Math.floor(diffMinutes / 60);
    const diffDays = Math.floor(diffHours / 24);
    
    // less than1minutes：ShowsecondsNumber
    if (diffSeconds < 60) {
        return diffSeconds <= 0 ? 'Just Excise.' : diffSeconds + 'Second';
    }
    
    // less than1hours：ShowminutesNumber
    if (diffMinutes < 60) {
        return diffMinutes + 'A minute ago.';
    }
    
    // less than24hours：ShowhoursNumber
    if (diffHours < 24) {
        return diffHours + 'Hour ago';
    }
    
    // Judgement ConfirmationYesNoYesYesterday.（The same.days）
    const nowDate = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const targetDate = new Date(targetTime.getFullYear(), targetTime.getMonth(), targetTime.getDate());
    const daysDiff = Math.floor((nowDate.getTime() - targetDate.getTime()) / (1000 * 60 * 60 * 24));
    
    // Yesterday.
    if (daysDiff === 1) {
        return 'Yesterday.';
    }
    
    // less than30days：ShowdaysNumber
    if (diffDays < 30) {
        return diffDays + 'Day before';
    }
    
    // Calculate month differential
    const monthsDiff = (now.getFullYear() - targetTime.getFullYear()) * 12 + (now.getMonth() - targetTime.getMonth());
    
    // less than12Months: Number of months shown
    if (monthsDiff < 12) {
        return monthsDiff + 'Months ago Peoples';
    }
    
    // greater than or equal to12Months: years shown
    const yearsDiff = Math.floor(monthsDiff / 12);
    return yearsDiff + 'Years ago';
}

/**
 * CriterionYesNoEmpty string
 * @eg undefined true
 * @eg null true
 * @eg '' true
 * @eg ' ' true
 * @eg '1' false
 * @return Boolean
 */
function isBlank(str) {
    return str === undefined || str === null || str === false || str.length === 0;
}

// ******************* Watermark ***************************
window.onresize = function () {
    watermark();
}
window.onscroll = function () {
    watermark();
}
//WatermarkStyle DefaultSettings
const settings = {
    watermark_txt: "",
    watermark_x: 50,//WatermarkOrganisation retrievexAxis Coordinates
    watermark_y: 55,//WatermarkOrganisation retrieveYAxis Coordinates
    watermark_rows: 2000,//WatermarkLines
    watermark_cols: 2000,//WatermarkColumns
    watermark_x_space: 70,//WatermarkxAxis interval
    watermark_y_space: 30,//WatermarkyAxis interval
    watermark_color: '#aaaaaa',//WatermarkFont Colour
    watermark_alpha: 0.2,//WatermarkTransparency
    watermark_fontsize: '15px',//WatermarkFont Size
    watermark_font: '. .',//WatermarkFonts
    watermark_width: 210,//WatermarkWidth
    watermark_height: 80,//WatermarkLength
    watermark_angle: 15//WatermarkInclination
};
let timestampWatermark;

function watermark() {
    const now = Date.now();
    if (timestampWatermark != null && now - timestampWatermark < 200) {
        return;
    }
    if (isBlank(settings.watermark_txt)) {
        return;
    }
    timestampWatermark = now;
    $(".realtime_etl_mask").remove();

    const water = document.body;
    //AccessPageMaximum width of face
    const page_width = Math.max(water.scrollWidth, water.clientWidth);
    //AccessPageMaximum face height
    const page_height = Math.max(water.scrollHeight, water.clientHeight);
    //WatermarkQuantities from the range of adaptation elements
    settings.watermark_cols = Math.ceil(page_width / (settings.watermark_x_space + settings.watermark_width));
    settings.watermark_rows = Math.ceil(page_height / (settings.watermark_y_space + settings.watermark_height));
    let x;
    let y;
    for (let i = 0; i < settings.watermark_rows; i++) {
        y = settings.watermark_y + (settings.watermark_y_space + settings.watermark_height) * i;
        for (let j = 0; j < settings.watermark_cols; j++) {
            x = settings.watermark_x + (settings.watermark_width + settings.watermark_x_space) * j;
            let mask_div = document.createElement('div');
            mask_div.className = 'realtime_etl_mask';
            mask_div.innerHTML = (settings.watermark_txt);
            mask_div.style.webkitTransform = "rotate(-" + settings.watermark_angle + "deg)";
            mask_div.style.MozTransform = "rotate(-" + settings.watermark_angle + "deg)";
            mask_div.style.msTransform = "rotate(-" + settings.watermark_angle + "deg)";
            mask_div.style.OTransform = "rotate(-" + settings.watermark_angle + "deg)";
            mask_div.style.transform = "rotate(-" + settings.watermark_angle + "deg)";
            mask_div.style.position = "absolute";
            mask_div.style.left = x + 'px';
            mask_div.style.top = y + 'px';
            mask_div.style.overflow = "hidden";
            mask_div.style.pointerEvents = 'none';
            mask_div.style.opacity = settings.watermark_alpha;
            mask_div.style.fontSize = settings.watermark_fontsize;
            mask_div.style.fontFamily = settings.watermark_font;
            mask_div.style.color = settings.watermark_color;
            mask_div.style.textAlign = "center";
            mask_div.style.width = settings.watermark_width + 'px';
            mask_div.style.height = settings.watermark_height + 'px';
            mask_div.style.display = "block";
            water.appendChild(mask_div);
        }
    }
}

// ******************* ExtensionJSForm Method***************************
$.fn.serializeJson = function () {
    var o = {};
    var a = this.serializeArray();
    $.each(a, function () {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};

function validateForm($form) {
    if (!$form || !$form.length) { return true; }

    var isValid = true;
    $form.find('.form-error-msg').remove();
    $form.find('.is-invalid').removeClass('is-invalid');

    // Find All withrequired Properties and Not HiddenStatusYes.Field
    $form.find('[required]:not([type="hidden"])').each(function(){
        var $field = $(this);

        // Extra checkYesNoAuthorization(Like throughhidden Properties orCSS display)
        if ($field.is(':hidden')) {
            return; // Skip this cycle and continue to verify the next element
        }

        if ($field.is(':disabled')) {
            return;
        }

        var value = $.trim($field.val());
        var invalid = false;

        if ($field.is(':checkbox') || $field.is(':radio')) {
            // Check for check boxes and single buttonsYesNoSelected
            invalid = !$field.is(':checked');
        } else if ($field.is('select')) {
            // For drop-down selection box, checkValueYesNoEmpty
            invalid = value === '' || value === null;
        } else {
            // Yeah.OtherInput box (text, numbers, etc.), checkValueLength
            invalid = value.length === 0;
        }

        if (invalid) {
            isValid = false;
            var $container = $field.closest('.form-control-area');
            if ($container.length === 0) {
                $container = $field.parent();
            }

            // 获取字段的标签文本用于错误提示
            var labelText = '';
            var $label = $field.closest('.form-item').find('.form-label').first();
            if ($label.length) {
                labelText = $label.text().replace('*', '').trim();
            } else if ($field.attr('placeholder')) {
                labelText = $field.attr('placeholder');
            } else {
                labelText = 'Field';
            }

            // Tag           FieldYesNoneEffect, andAddErrorInfo
            $field.addClass('is-invalid');
            if ($container.length) {
                $('<div class="form-error-msg"><i class="fa fa-exclamation-circle"></i> ' + labelText + 'Can\'t be empty.</div>').appendTo($container);
            }
        }
    });

    return isValid;
}

// ******************* CopyTo Clipboard***************************
/**
 * CopyText to Clipboard (public)Totalmethodology)
 * @param {string} text - Yes.Copytext
 * @param {Object} options - ConfigureOptions
 * @param {string|jQuery|HTMLElement} options.button - But (17%)jQueryObject, Selector orDOMElement) for displayCopyStatusFeedback
 * @param {string} options.successMessage - Copied successfullyNoticeMessage, default:"Copy Teachers!"
 * @param {string} options.errorMessage - Copy FailedNoticeMessage, default:"Copy failed, please copy manually"
 * @param {string} options.successIcon - Copied successfully, by default:"fa-check"
 * @param {string} options.originalIcon - button, default:"fa-copy"
 * @param {string} options.originalText - button, default:"Copy"
 * @param {number} options.resetDelay - buttonStatusResetDelayTime（ms），Default:2000
 * @param {string} options.copiedClass - Copied successfullyTimeAddTo the buttonCSSClass, default:"copied"
 */
function copyToClipboard(text, options) {
    if (!text) {
        const errorMsg = (options && options.errorMessage) || 'Copy failed, please copy manually';
        if (typeof bootGrowl === 'function') {
            bootGrowl(errorMsg, "danger");
        }
        return Promise.reject(new Error('Text content is empty'));
    }

    // DefaultConfigure
    const config = {
        successMessage: 'Copy Teachers!',
        errorMessage: 'Copy failed, please copy manually',
        successIcon: 'fa-check',
        originalIcon: 'fa-copy',
        originalText: 'Copy',
        resetDelay: 2000,
        copiedClass: 'copied',
        ...options
    };

    // Get button elements (support)jQueryObject, Selector String orDOMElements)
    let $button = null;
    if (config.button) {
        if (typeof config.button === 'string') {
            $button = $(config.button);
        } else if (config.button instanceof jQuery) {
            $button = config.button;
        } else if (config.button instanceof HTMLElement) {
            $button = $(config.button);
        }
    }

    // If buttons exist, checkYesNoOngoingCopyMedium (prevent rapid multiple hits)
    if ($button && $button.length) {
        const isCopying = $button.data('copying');
        if (isCopying) {
            // If it isCopy, ignore this request
            return Promise.resolve();
        }
        // ClearBefore.ResetScheduledInstrument (if any)
        const resetTimerId = $button.data('resetTimerId');
        if (resetTimerId) {
            clearTimeout(resetTimerId);
            $button.removeData('resetTimerId');
        }
    }

    // Savethe original content of the button
    let originalButtonHtml = '';
    if ($button && $button.length) {
        originalButtonHtml = $button.html();
        // Mark as ongoingCopyMedium
        $button.data('copying', true);
    }

    // Update buttonStatusYes"Copyed"
    function updateButtonToCopied() {
        if ($button && $button.length) {
            const successText = config.originalText === 'Copy' ? 'Copyed' : config.originalText;
            $button.html(`<i class="fa ${config.successIcon}"></i> ${successText}`);
            $button.addClass(config.copiedClass);
        }
    }

    // Restore button originalStatus
    function resetButton() {
        if ($button && $button.length) {
            $button.html(originalButtonHtml || `<i class="fa ${config.originalIcon}"></i> ${config.originalText}`);
            $button.removeClass(config.copiedClass);
            // ClearCopyto mark andScheduledDeviceID
            $button.removeData('copying');
            $button.removeData('resetTimerId');
        }
    }

    // Using Modern BrowserClipboard API
    let copyPromise;
    if (navigator.clipboard && window.isSecureContext) {
        copyPromise = navigator.clipboard.writeText(text).then(function() {
            // Copied successfully
            if (typeof bootGrowl === 'function') {
                bootGrowl(config.successMessage, "success");
            }
            updateButtonToCopied();
            // SaveScheduledDeviceID，For follow-upClear
            const resetTimerId = setTimeout(function() {
                resetButton();
            }, config.resetDelay);
            if ($button && $button.length) {
                $button.data('resetTimerId', resetTimerId);
            }
            return Promise.resolve();
        }).catch(function(err) {
            console.error('Copy Failed', err);
            // Copy FailedI'll have to.ClearCopyMiddle Tag
            if ($button && $button.length) {
                $button.removeData('copying');
            }
            // Downgrade to traditional methods
            return fallbackCopyText(text, config, updateButtonToCopied, resetButton, $button);
        });
    } else {
        // Downgrade to traditional methods
        copyPromise = fallbackCopyText(text, config, updateButtonToCopied, resetButton, $button);
    }
    copyPromise.catch(function(err) {
        // ErrorImplemented internally (adopted)bootGrowl Notice），It's only for disposal.Promise rejection Warning
        console.error('Copy operation failed:', err);
        // Copy FailedI'll have to.ClearCopyMiddle Tag
        if ($button && $button.length) {
            $button.removeData('copying');
        }
    });
}

/**
 * DowngradeCopyScheme (old browser compatible)
 * @private
 */
function fallbackCopyText(text, config, updateButtonToCopied, resetButton, $button) {
    return new Promise(function(resolve, reject) {
        const textArea = document.createElement("textarea");
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.top = '0';
        textArea.style.left = '0';
        textArea.style.opacity = '0';
        textArea.style.pointerEvents = 'none';
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();

        try {
            const successful = document.execCommand('copy');
            document.body.removeChild(textArea);
            
            if (successful) {
                if (typeof bootGrowl === 'function') {
                    bootGrowl(config.successMessage, "success");
                }
                if (updateButtonToCopied) {
                    updateButtonToCopied();
                    // SaveScheduledDeviceID，For follow-upClear
                    const resetTimerId = setTimeout(function() {
                        resetButton();
                    }, config.resetDelay);
                    if ($button && $button.length) {
                        $button.data('resetTimerId', resetTimerId);
                    }
                }
                resolve();
            } else {
                throw new Error('execCommand copy failed');
            }
        } catch (err) {
            document.body.removeChild(textArea);
            console.error('Copy Failed', err);
            if (typeof bootGrowl === 'function') {
                bootGrowl(config.errorMessage, "danger");
            }
            // Copy FailedI'll have to.ClearCopyMiddle Tag
            if ($button && $button.length) {
                $button.removeData('copying');
            }
            reject(err);
        }
    });
}

// ******************* GlobalScheduledRefreshManager***************************
/**
 * GlobalScheduledRefreshManager
 * Global onlyStartOne.ScheduledTasks，Fixed interval5secondsRun once
 * EachPageNoodles can register themselves.RefreshFunctions，OnlyRefreshCurrent DisplayPageNoodle content
 */
const PageRefreshManager = (function() {
    let refreshTimer = null;
    let currentRefreshFn = null;
    const REFRESH_INTERVAL = 5000; // 5seconds

    // StartScheduledDevice
    function startTimer() {
        if (refreshTimer === null) {
            refreshTimer = setInterval(function() {
                if (currentRefreshFn && typeof currentRefreshFn === 'function') {
                    try {
                        currentRefreshFn();
                    } catch (e) {
                        console.error('Page refreshing function executed error:', e);
                    }
                }
            }, REFRESH_INTERVAL);
        }
    }

    // StopScheduledDevice
    function stopTimer() {
        if (refreshTimer !== null) {
            clearInterval(refreshTimer);
            refreshTimer = null;
        }
    }

    return {
        /**
         * Register CurrentPageNoodles.RefreshFunctions
         * @param {Function} refreshFn - RefreshFunctions
         */
        register: function(refreshFn) {
            currentRefreshFn = refreshFn;
            startTimer();
        },
        
        /**
         * CancelRegister CurrentPageNoodles.RefreshFunctions
         */
        unregister: function() {
            currentRefreshFn = null;
        },
        
        /**
         * StopScheduledDevice (normally no manual call required)
         */
        stop: function() {
            stopTimer();
            currentRefreshFn = null;
        }
    };
})();

// Global LoadPageNoodles.
function doLoader(url) {
    // Use unified content area
    if ($mainContent.length) {
        // ClearBeforePageNoodles.RefreshFunctionsRegistration
        PageRefreshManager.unregister();

        // Destroy Current Content AreadbSelect，AvoidPageScroll access removed after face switchingDOM
        if (typeof window.destroyDbSelectsIn === 'function') {
            window.destroyDbSelectsIn($mainContent);
        }

        if (typeof window.syncSidebarActive === 'function') {
            window.syncSidebarActive(url);
        }

        // Show LoadingStatus
        showLoading();

        // LoadPageFace
        $mainContent.load($basePath + url, function (response, status, xhr) {
            hideLoading();
            if (status !== 'success') {
                bootGrowl('Page loading failed, please try again later', "danger");
            } else if (window.DBSPermission && DBSPermission.isLoaded()) {
                DBSPermission.apply($mainContent);
            }
        });
    }
}

// ErrorRequest
function doRequest(action, data) {
    hideLoading();
    // ErrorRequests:302
    if (!(data instanceof Object)) {
        bootGrowl("Session expires, access login home page in 3 seconds...", "danger");
        setTimeout(function () {
            location.href = $basePath;
        }, 3000);
    } else {
        action(data);
    }
}

// ErrorResponse
function doErrorResponse(xhr, status, info) {
    hideLoading();
    bootGrowl("Access abnormal, please refresh or try again.", "danger");
}

// GlobalAjax post
function doPoster(url, params, action) {
    $.post($basePath + url, params, function (data) {
        doRequest(action, data);
    }).error(function (xhr, status, info) {
        doErrorResponse(xhr, status, info);
    });
}

// GlobalAjax get
function doGetter(url, params, action) {
    $.get($basePath + url, params, function (data) {
        doRequest(action, data);
    }).error(function (xhr, status, info) {
        doErrorResponse(xhr, status, info);
    });
}

function initSearch(searchWrapperId, callback) {
    const wrapper = document.getElementById(searchWrapperId);
    const searchInput = wrapper.querySelector('.search-input');
    const searchClear = wrapper.querySelector('.search-clear');

    if (!searchInput || !searchClear || searchInput.dataset.selectBound === 'true') {
        return null;
    }

    // UpdateSearchStatus（Show / HideClearbutton)
    function updateSearchState(keyword) {
        if (keyword && keyword.trim() !== '') {
            searchClear.classList.add('active');
        } else {
            searchClear.classList.remove('active');
        }
    }

    // ClearSearchInternal realization
    function clearSearch() {
        searchInput.value = '';
        searchInput.focus();
        updateSearchState('');
        if (callback) {
            callback(searchInput.value);
        }
    }

    // TieEvent
    searchInput.addEventListener('input', function(e) {
        const value = e.target.value;
        // Update immediatelyClearShow administer buttonsStatus
        updateSearchState(value);
    });

    // ClearSearch
    searchClear.addEventListener('click', clearSearch);

    // Back to car keysSearch
    searchInput.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            updateSearchState(e.target.value);
            if (callback) {
                callback(e.target.value);
            }
        }
    });

    // Avoid duplication of bindings
    searchInput.dataset.selectBound = 'true';

    // InitializeStatus
    updateSearchState(searchInput.value || '');

    // BackOrganisationinput Subject of example and methodology
    return {
        // input Example of element
        input: searchInput,
        // Simulate ClickClearbutton
        clear: function() {
            clearSearch();
        },
        // SettingsSearchValue
        setValue: function(value) {
            searchInput.value = value || '';
            updateSearchState(searchInput.value);
            if (callback) {
                callback(searchInput.value);
            }
        },
        // AccessSearchValue
        getValue: function() {
            return searchInput.value;
        },
        // TriggerSearch（Simulation back)
        search: function() {
            updateSearchState(searchInput.value);
            if (callback) {
                callback(searchInput.value);
            }
        }
    };
}

function initSelect(selector){
    return selector.dbSelect({
        type: 'single'
    });
}

function initSwitch(dbSwitch, callback) {
    const $input = dbSwitch;
    $input.attr('role', 'switch');
    $input.attr('aria-checked', $input.is(':checked'));
    $input.on('change', function () {
        $input.attr('aria-checked', this.checked);
        callback(this.checked);
    });

    return {
        isChecked: function () {
            return $input.is(':checked');
        }
    }
}

// Logout
function logout() {
    // Basic use
    showConfirm({
        title: 'Are you sure you want to write it off?',
        message: 'Go to login page after confirmation Noodles.',
        icon: 'info',
        size: 'large',
        confirmType: 'info',
        onConfirm: function () {
            doPoster("/logout", null, function (data) {
                location.href = $basePath;
            });
        }
    });
}

// RefreshLoginPeople commented:Info（HamFunctional PermissionsI don't know.DriverMenu/Button Visible)
function refreshLoginUser() {
    doGetter("/user/getUserInfo.json", {}, function (response) {
        if (response.success) {
            $("#currentUser").text(response.data.nickname);
            if (window.DBSPermission) {
                DBSPermission.load(response.data || {});
                DBSPermission.apply(document);
            }
            if (typeof window.syncSidebarActive === 'function') {
                const $active = $('.sidebar-item[url].active').first();
                window.syncSidebarActive($active.length ? $active.attr('url') : '/index');
            }
        }
    });
}

// RefreshAuthorizationInfo（Topheader AllowStatus）
function refreshLicense() {
    doGetter("/license/query.json", {}, function (response) {
        if (response.success !== true) {
            return;
        }
        const licenseInfo = response.data;
        const $licenseInfo = $("#licenseInfo");
        const $editionName = $("#editionName");
        const $content = $("#effectiveContent");
        const $attention = $("#licenseAttention");
        const $arrow = $("#licenseArrow");
        const $dropdownMenu = $("#licenseDropdownMenu");
        const $productDivider = $("#licenseProductDivider");

        $("#licenseCheck, #licenseRemind, #licenseWarning").addClass("hidden");
        $content.addClass("hidden").text("").removeClass("text-warning");
        $attention.addClass("hidden").text("").removeClass("license-attention-warning license-attention-error");
        $arrow.addClass("hidden");
        $dropdownMenu.addClass("hidden");
        $productDivider.addClass("hidden");
        $dropdownMenu.find(".license-product-item").remove();

        const edition = licenseInfo.edition;
        $licenseInfo.show();
        if (edition === "community") {
            $editionName.text("Community");
            return;
        }

        $editionName.text(licenseInfo.editionName || "Professional");
        const editionStatus = licenseInfo.editionStatus;
        const products = licenseInfo.products || [];

        // Not activated：ProfessionalDeployment 1965aNoneAny productAuthorization
        if (editionStatus === "inactive" || (!editionStatus && products.length === 0)) {
            $("#licenseWarning").removeClass("hidden");
            $attention.removeClass("hidden").addClass("license-attention-error").text("Not activated");
            $arrow.removeClass("hidden");
            return;
        }

        if (editionStatus === "normal" || editionStatus === "partial") {
            $("#licenseCheck").removeClass("hidden");
        } else if (editionStatus === "expiring") {
            $("#licenseRemind").removeClass("hidden");
            if (licenseInfo.effectiveContent) {
                $content.removeClass("hidden").text(licenseInfo.effectiveContent);
            }
        } else if (editionStatus === "expired") {
            $("#licenseWarning").removeClass("hidden");
            $attention.removeClass("hidden").addClass("license-attention-error").text("All functions expired");
        }

        const attentionCount = Number(licenseInfo.attentionCount) || 0;
        if (editionStatus !== "expired" && attentionCount > 0 && licenseInfo.attentionSummary) {
            $attention.removeClass("hidden");
            $attention.addClass(licenseInfo.attentionSummary.indexOf("Expired") >= 0
                ? "license-attention-error"
                : "license-attention-warning");
            $attention.text(licenseInfo.attentionSummary);
        }

        if (products.length > 0) {
            $arrow.removeClass("hidden");
            renderLicenseProductList($dropdownMenu, $productDivider, products);
        } else {
            $arrow.removeClass("hidden");
        }
    });
}

function renderLicenseProductList($dropdownMenu, $productDivider, products) {
    const statusConfig = {
        normal: { icon: "fa-check-circle", badge: "badge-success", label: "Normal" },
        expiring: { icon: "fa-exclamation-triangle", badge: "badge-warning", label: "Expiring soon" },
        expired: { icon: "fa-times-circle", badge: "badge-error", label: "Expired" }
    };

    products.forEach(function (product) {
        const config = statusConfig[product.status] || statusConfig.normal;
        const effectiveTime = Number(product.effectiveTime) || 0;
        const timeText = effectiveTime > 0 ? formatDate(effectiveTime) : "Not activated";
        $productDivider.before(
            '<li class="license-product-item">'
            + '<div class="license-product-main">'
            + '<span class="license-product-name">' + escapeHtml(product.name || "") + '</span>'
            + '<span class="badge ' + config.badge + '"><i class="fa ' + config.icon + '"></i> ' + config.label + '</span>'
            + '</div>'
            + '<div class="license-product-time">' + escapeHtml(timeText) + '</div>'
            + '</li>'
        );
    });
    $productDivider.removeClass("hidden");
}

// ******************* Sync taskTable Presentation***************************

function getStateConfig(mappingId) {
    return {
        0: { // Not running
            icon: 'fa-play',
            title: 'Start',
            onclick: `changeMappingState('${mappingId}', '/mapping/start', 'Start')`,
            disabled: false,
            class: 'badge-info',
            text: 'Not running'
        },
        1: { // Running
            icon: 'fa-pause',
            title: 'Stop',
            onclick: `changeMappingState('${mappingId}', '/mapping/stop', 'Stop')`,
            disabled: false,
            class: 'badge-success',
            text: 'Running'
        },
        2: { // Stopping
            icon: 'fa-spinner fa-spin',
            title: 'Stopping',
            text: ' Stopping',
            disabled: true,
            class: 'badge-warning'
        }
    };
}

// Format SeemsminRatio (reserved)2decimal places)
function formatPercent(value, maxFractionDigits) {
    if (value == null || isNaN(value)) return '0%';
    const percent = value * 100;
    return percent.toFixed(maxFractionDigits || 2) + '%';
}

// Format the integer number (thousands)minBits)
function formatCount(value) {
    const num = Number(value) || 0;
    return num.toLocaleString('en-US');
}

// Additional time-consumingInfo
function formatElapsedDuration(startTime, endTime) {
    const begin = Number(startTime);
    const end = Number(endTime);
    if (!begin || !end || end <= begin) {
        return '';
    }
    const seconds = Math.floor((end - begin) / 1000);
    if (seconds < 60) {
        return seconds + 'seconds';
    }
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    if (minutes < 60) {
        return minutes + 'min' + remainingSeconds + 'seconds';
    }
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    return hours + 'hours' + remainingMinutes + 'min';
}

function appendElapsedTime(content, meta) {
    // Running/Stopping updateTime Often not counted.Refresh，Use front-end currentTimeReal time.
    const RUNNING = 1;
    const STOPPING = 2;
    const endTime = (meta.state === RUNNING || meta.state === STOPPING)
        ? Date.now()
        : meta.updateTime;
    const text = formatElapsedDuration(meta.startTime, endTime);
    if (!text) {
        return;
    }
    content.push(`<div class="text-tertiary">Time-consuming: ${text}</div>`);
}

// Mini progress bar
function renderMiniProgressBar(current, total) {
    const safeTotal = Number(total) || 0;
    const safeCurrent = Number(current) || 0;
    const percent = safeTotal > 0 ? Math.min(100, Math.round((safeCurrent / safeTotal) * 100)) : 0;
    return `<div class="progress-bar" style="height:6px;margin:4px 0;">
        <div class="progress-fill progress-fill-success" style="width:${percent}%"></div>
    </div>`;
}

// ParsingFull + incrementalCurrent Involvement:full / increment / idle
function resolveFullIncrementPhase(meta) {
    const RUNNING = 1;
    const STOPPING = 2;
    const snapshot = meta.snapshot || {};
    const phaseCode = snapshot.fullIncrementPhase;
    const isActive = meta.state === RUNNING || meta.state === STOPPING;

    if (!isActive) {
        // StopRetain the semantics of the period and avoid them.CompletedFullIt's a show.FullProgress Bar Style
        if (phaseCode === 'increment') {
            return 'increment';
        }
        return 'idle';
    }

    // Backendsnapshot Phase to be determinedIncrementalPhasesuccess Often less thanFull total，I can't use it anymore.processed < total Infer)
    if (phaseCode === 'increment') {
        return 'increment';
    }

    if (meta.counting) {
        return 'full';
    }

    if (phaseCode === 'full') {
        if (meta.counting || isFullPhaseInProgress(meta, snapshot)) {
            return 'full';
        }
        // FullMarked asfull But not yet countedtotal，Press stillFullPhase Presentation
        if ((Number(meta.total) || 0) === 0) {
            return 'full';
        }
        return 'increment';
    }

    // Compatibility of old data: Socio-economicNone fullIncrementPhase Back to progress inspiration
    return isFullPhaseInProgress(meta, snapshot) ? 'full' : 'increment';
}

function isFullPhaseInProgress(meta, snapshot) {
    const total = Number(meta.total) || 0;
    const processed = (Number(meta.success) || 0) + (Number(meta.fail) || 0);
    const tableGroupIndex = parseInt(snapshot.tableGroupIndex || '0', 10);
    const pageIndex = parseInt(snapshot.pageIndex || '1', 10);
    const fullCursorActive = tableGroupIndex > 0 || pageIndex > 1 || !!snapshot.cursor;
    if (total > 0 && processed < total) {
        return true;
    }
    return fullCursorActive && (total === 0 || processed <= total);
}

// Full syncPresentation of progress (Full / Full + incrementalFullPhaseTotalUsed
function renderFullPhaseSyncResult(meta) {
    const total = Number(meta.total) || 0;
    const success = Number(meta.success) || 0;
    const processed = success + (Number(meta.fail) || 0);
    // Yes.TotalShows progress barFinishBack100%），Onlytotal=0 TimeNoticeWaitStart
    const showProgress = total > 0;
    const content = [];

    content.push('<div class="sync-result text-xs">');
    if (meta.counting) {
        content.push(`<div class="text-tertiary">Statistics in progressTotal...</div>`);
    } else {
        content.push(`<div>Total: ${formatCount(total)}</div>`);
        if (showProgress) {
            content.push(renderMiniProgressBar(processed, total));
            content.push(`<div>FullProgress: ${formatCount(processed)} / ${formatCount(total)} (${formatPercent(processed / total, 1)})</div>`);
        } else {
            content.push(`<div class="text-tertiary">WaitFullTasksStart...</div>`);
        }
    }
    appendElapsedTime(content, meta);
    content.push(`<div>Success: ${formatCount(success)}</div>`);
    content.push('</div>');
    return content.join('');
}

// Full + incremental：ResultColumn Presentation
function renderFullIncrementSyncResult(meta) {
    const phase = resolveFullIncrementPhase(meta);
    const total = Number(meta.total) || 0;
    const success = Number(meta.success) || 0;
    const content = [];

    if (phase === 'full') {
        return renderFullPhaseSyncResult(meta);
    }
    content.push('<div class="sync-result text-xs">');
    if (phase === 'increment') {
        content.push(`<div>Success: ${formatCount(success)}</div>`);
    } else {
        if (total > 0) {
            content.push(`<div>FullTotal: ${formatCount(total)}</div>`);
        }
        content.push(`<div>Success: ${formatCount(success)}</div>`);
    }

    content.push('</div>');
    return content.join('');
}

// FullMode:ResultColumn Presentation
function renderFullSyncResult(meta) {
    return renderFullPhaseSyncResult(meta);
}

// Sync failedOkay.
function renderMappingFailLine(meta, mappingId) {
    if (!meta.fail || meta.fail <= 0) {
        return '';
    }
    const id = mappingId || '';
    return `<div class="text-xs mt-1">Failed: <span class="text-error">${formatCount(meta.fail)}</span> <span class="hover-underline cursor-pointer text-error" title='View failed details' onclick="doLoader('/mapping/page/detail?id=${id}&detailStatus=fail')">View details</span></div>`;
}

// By SyncResultGenerateContents
function renderSyncResult(mapping) {
    const meta = mapping.meta;
    if (!meta) return '';

    if (mapping.model === 'fullIncrement') {
        return renderFullIncrementSyncResult(meta) + renderMappingFailLine(meta, mapping.id);
    }

    const content = [];
    if (mapping.model === 'full') {
        content.push(renderFullSyncResult(meta));
    } else {
        content.push(`<div class="text-xs">Success: ${formatCount(meta.success || 0)}</div>`);
    }
    content.push(renderMappingFailLine(meta, mapping.id));
    return content.join('');
}

// Based onTask TypeGenerateContents
function renderModelText(model) {
    const modelState = {
        'full': { // Full
            class: 'badge-primary',
            text: 'Full sync',
        },
        'increment': { // Incremental
            class: 'badge-info',
            text: 'Incremental sync',
        },
        'fullIncrement': {
            class: 'badge-info',
            text: 'Full + incremental',
        }
    };
    const config = modelState[model] || { class: 'badge-secondary', text: model || 'Unknown' };
    return `<span class="badge ${config.class}">${config.text}</span>`;
}

// Based onStatusGenerateContents
function renderStateText(state) {
    const stateConfig = getStateConfig();
    const config = stateConfig[state] || stateConfig[0];
    return `<span class="badge ${config.class}">${config.text}</span>`;
}

// Based onStatusGenerateActionsbutton
function renderStateButton(state, mappingId) {
    const stateConfig = getStateConfig(mappingId);
    const config = stateConfig[state] || stateConfig[0];
    const disabledAttr = config.disabled ? ' disabled' : '';
    const onclickAttr = config.onclick ? ` onclick="${config.onclick}"` : '';
    const canOperate = !window.DBSPermission || DBSPermission.has('mapping:operate');
    const canEdit = !window.DBSPermission || DBSPermission.has('mapping:edit');
    const stateBtn = [];
    if (canOperate) {
        stateBtn.push(`
        <button class="table-action-btn play" data-id="${mappingId}" title="${config.title}"${onclickAttr}${disabledAttr}>
            <i class="fa ${config.icon}"></i>
        </button>
    `);
    }
    // Not runningShow onlyDeletebutton
    if (state === 0 && canEdit) {
        stateBtn.push(`
            <button class="table-action-btn delete" title="Delete" onclick="deleteMapping('${mappingId}')">
                <i class="fa fa-trash"></i>
            </button>
        `);
    }
    return stateBtn.join('');
}

/**
 * Sidebar:GroupExpand, shrink, pressurl Highlight
 */
(function (window, $) {
    'use strict';

    const STORAGE_COLLAPSED = 'realtime-etl.sidebar.collapsed';
    const STORAGE_GROUPS = 'realtime-etl.sidebar.groups';

    function isCollapsed() {
        return document.body.classList.contains('sidebar-collapsed');
    }

    function setCollapsed(collapsed) {
        document.body.classList.toggle('sidebar-collapsed', !!collapsed);
        try {
            localStorage.setItem(STORAGE_COLLAPSED, collapsed ? '1' : '0');
        } catch (e) {
            // ignore
        }
        closeAllFlyouts();
        const $btn = $('#sidebarToggle');
        if ($btn.length) {
            $btn.attr('title', collapsed ? 'Expand Sidebar' : 'Collapse sidebar');
        }
    }

    function readCollapsed() {
        try {
            return localStorage.getItem(STORAGE_COLLAPSED) === '1';
        } catch (e) {
            return false;
        }
    }

    function readOpenGroups() {
        try {
            const raw = sessionStorage.getItem(STORAGE_GROUPS);
            if (!raw) {
                return null;
            }
            const arr = JSON.parse(raw);
            return Array.isArray(arr) ? arr : null;
        } catch (e) {
            return null;
        }
    }

    function saveOpenGroups() {
        const keys = [];
        $('.sidebar-group.open').each(function () {
            const key = $(this).attr('data-group');
            if (key) {
                keys.push(key);
            }
        });
        try {
            sessionStorage.setItem(STORAGE_GROUPS, JSON.stringify(keys));
        } catch (e) {
            // ignore
        }
    }

    function closeAllFlyouts() {
        $('.sidebar-group').removeClass('flyout-open');
    }

    function normalizeSidebarPath(url) {
        if (!url) {
            return '';
        }
        let path = String(url).split('?')[0];
        if ($basePath && path.indexOf($basePath) === 0) {
            path = path.substring($basePath.length);
        }
        if (path.charAt(0) !== '/') {
            path = '/' + path;
        }
        return path;
    }

    function menuPrefix(menuUrl) {
        if (!menuUrl) {
            return '';
        }
        if (menuUrl.length > 5 && menuUrl.slice(-5) === '/list') {
            return menuUrl.slice(0, -4);
        }
        return menuUrl.charAt(menuUrl.length - 1) === '/' ? menuUrl : menuUrl + '/';
    }

    function findBestSidebarItem(path) {
        let best = null;
        let bestLen = -1;
        $('.sidebar-item[url]').each(function () {
            const $item = $(this);
            if ($item.hasClass('hidden')) {
                return;
            }
            const u = $item.attr('url');
            if (!u) {
                return;
            }
            let matched = path === u;
            if (!matched && path.indexOf(u + '/') === 0) {
                matched = true;
            }
            if (!matched) {
                const prefix = menuPrefix(u);
                if (prefix && path.indexOf(prefix) === 0) {
                    matched = true;
                }
            }
            if (matched && u.length > bestLen) {
                best = $item;
                bestLen = u.length;
            }
        });
        return best;
    }

    function expandGroupOf($item) {
        if (!$item || !$item.length) {
            return;
        }
        // Expand whole ancestral chain (support Strategies)System management → Log management）
        $item.parents('.sidebar-group').addClass('open').addClass('has-active');
        saveOpenGroups();
    }

    function syncSidebarActive(url) {
        const path = normalizeSidebarPath(url || '/index');
        $('.sidebar-item[url]').removeClass('active');
        $('.sidebar-group').removeClass('has-active');
        closeAllFlyouts();

        const $best = findBestSidebarItem(path);
        if ($best && $best.length) {
            $best.addClass('active');
            expandGroupOf($best);
        }
    }

    function syncSidebarGroupVisibility() {
        // From the inside: handle the nest firstGroup，Reprocess the outer layer.
        const $groups = $('.sidebar-group').toArray().reverse();
        $groups.forEach(function (el) {
            const $group = $(el);
            const $visible = $group.find('.sidebar-item[url]').filter(function () {
                return !$(this).hasClass('hidden');
            });
            if ($visible.length === 0) {
                $group.addClass('hidden');
                $group.removeClass('open flyout-open has-active');
            } else {
                $group.removeClass('hidden');
            }
        });
    }

    function restoreGroups() {
        const saved = readOpenGroups();
        $('.sidebar-group').removeClass('open');
        if (saved && saved.length) {
            saved.forEach(function (key) {
                $('.sidebar-group[data-group="' + key + '"]').addClass('open');
            });
        }
        const $active = $('.sidebar-item[url].active').first();
        if ($active.length) {
            expandGroupOf($active);
        }
    }

    function initSidebar() {
        setCollapsed(readCollapsed());
        restoreGroups();

        $(document).on('click', '.sidebar-item[url]', function (e) {
            e.preventDefault();
            e.stopPropagation();
            const url = $(this).attr('url');
            closeAllFlyouts();
            doLoader(url);
        });

        $(document).on('click', '.sidebar-group-title', function (e) {
            e.preventDefault();
            e.stopPropagation();
            // Shrink by suspending pop-up secondary menu, clickGroupTitle does not switch
            if (isCollapsed()) {
                return;
            }
            $(this).closest('.sidebar-group').toggleClass('open');
            saveOpenGroups();
        });

        // Shrink: SuspendedGroupPopup Level 2 (compatible migration to float)
        $(document).on('mouseenter', '.sidebar-group', function () {
            if (!isCollapsed()) {
                return;
            }
            closeAllFlyouts();
            $(this).addClass('flyout-open');
        });
        $(document).on('mouseleave', '.sidebar-group', function () {
            if (!isCollapsed()) {
                return;
            }
            $(this).removeClass('flyout-open');
        });

        $('#sidebarToggle').on('click', function (e) {
            e.preventDefault();
            setCollapsed(!isCollapsed());
        });
    }

    window.syncSidebarActive = syncSidebarActive;
    window.syncSidebarGroupVisibility = syncSidebarGroupVisibility;
    window.initSidebar = initSidebar;
})(window, jQuery);

$(function () {
    // DefinitionsBackFunctions，SonPageNoodles.Back
    window.backIndexPage = function () {
        doLoader('/index');
    };
    window.validateForm = validateForm;

    if (typeof window.initSidebar === 'function') {
        window.initSidebar();
    }

    refreshLoginUser();
    refreshLicense();
    // Initialize copyrightInfo
    doGetter("/index/version.json", {}, function (response) {
        if (response.success === true) {
            // Get bottom copyrightsInfo
            $("#copyRight").html(response.data.appCopyRight);
            settings.watermark_txt = response.data.watermark;
            watermark();
        }
    });

    // Dropdown Menu
    const $dropdown = $(".dropdown");
    const $dropdownBtn = $(".dropdown button");
    const $dropdownMenu = $(".dropdown .dropdown-menu");
    $dropdownBtn.on('click', function (){
        event.stopPropagation();
        // Toggle only the current dropdown menu, without prejudiceOtherDropdown Menu
        const $currentDropdown = $(this).closest('.dropdown');
        const $currentMenu = $currentDropdown.find('.dropdown-menu');
        // CloseOtherDropdown Menu
        $dropdown.not($currentDropdown).removeClass("open");
        $dropdownMenu.not($currentMenu).addClass("hidden");
        // Toggle the current dropdown menu
        $currentDropdown.toggleClass("open");
        $currentMenu.toggleClass("hidden");
    });
    // Only if you click the drop-down menu button or menu entryClose
    $(document).on('click', function(event) {
        if (!$(event.target).closest('.dropdown').length) {
            $dropdown.removeClass("open");
            $dropdownMenu.addClass("hidden");
        }
    });
});
/**
 * ConnectorminPage → dbSelect Options
 */
function convertConnectorPagingToSelectData(paging, preferredType) {
    const rows = paging && Array.isArray(paging.data) ? paging.data : [];
    const preferred = preferredType ? String(preferredType).trim() : '';
    const list = [];
    rows.forEach(function (row) {
        if (!row || !row.id) {
            return;
        }
        const type = (row.config && row.config.connectorType) ? String(row.config.connectorType) : '';
        if (preferred && type && preferred.toLowerCase() !== type.toLowerCase()) {
            return;
        }
        const name = row.name || '';
        list.push({
            label: type ? (name + '(' + type + ')') : name,
            value: row.id,
            data: {connectorType: type}
        });
    });
    return list;
}

/**
 * Build RemoteConnector dbSelect Configure（LibraryIt's the other way.minPage）。
 * @param {Object} extra defaultValue/defaultLabel/defaultType/relationOnly/preferredType/role/pageSize/disabled/onSelect
 */
function buildRemoteConnectorSelectOptions(extra) {
    extra = extra || {};
    const initialData = [];
    if (extra.defaultValue) {
        initialData.push({
            label: extra.defaultLabel || extra.defaultValue,
            value: extra.defaultValue,
            data: {connectorType: extra.defaultType || ''}
        });
    }
    return {
        type: 'single',
        remoteSearch: true,
        pageSize: extra.pageSize || 50,
        keywordDebounceMs: 300,
        disabled: !!extra.disabled,
        defaultValue: extra.defaultValue ? [extra.defaultValue] : null,
        data: initialData,
        loadOptions: function (query, onSuccess, onError) {
            const params = {
                pageNum: query.pageNum,
                pageSize: query.pageSize,
                searchKey: query.searchKey || ''
            };
            if (extra.relationOnly) {
                params.relationOnly = '1';
            }
            if (extra.role === 'source' || extra.role === 'target') {
                params.role = extra.role;
            }
            doPoster('/connector/search', params, function (res) {
                if (res.success === true) {
                    const paging = res.data || {};
                    onSuccess({
                        pageNum: paging.pageNum || query.pageNum,
                        pageSize: paging.pageSize || query.pageSize,
                        total: paging.total || 0,
                        data: convertConnectorPagingToSelectData(paging, extra.preferredType)
                    });
                } else {
                    bootGrowl(res.message || 'Failed to load connector', 'danger');
                    if (typeof onError === 'function') {
                        onError();
                    }
                }
            });
        },
        onSelect: extra.onSelect
    };
}

/**
 * Resolve connector type from dbSelect (prefer data.connectorType).
 */
function resolveConnectorTypeFromSelect($connector, connectorId) {
    const api = $connector && $connector.data ? $connector.data('dbSelect') : null;
    if (api && typeof api.getSelectedItems === 'function') {
        const items = api.getSelectedItems() || [];
        if (items.length && items[0] && items[0].data && items[0].data.connectorType) {
            return String(items[0].data.connectorType).trim();
        }
    }
    if (!connectorId || !$connector || !$connector.length) {
        return '';
    }
    const $option = $connector.find('option[value="' + connectorId + '"]');
    if ($option.length) {
        return ($option.attr('data-connector-type') || $option.data('connectorType') || '').toString().trim();
    }
    return '';
}

/**
 * Shared remote page drawer config model (id + name).
 * @param {Object} extra url/defaultValue/defaultLabel/pageSize/onSelect/convertRow/params
 */
function buildRemotePagingSelectOptions(extra) {
    extra = extra || {};
    const initialData = [];
    if (extra.defaultValue) {
        initialData.push({
            label: extra.defaultLabel || extra.defaultValue,
            value: extra.defaultValue
        });
    }
    return {
        type: 'single',
        remoteSearch: true,
        pageSize: extra.pageSize || 50,
        keywordDebounceMs: 300,
        defaultValue: extra.defaultValue ? [extra.defaultValue] : null,
        data: initialData,
        loadOptions: function (query, onSuccess, onError) {
            const params = $.extend({}, extra.params || {}, {
                pageNum: query.pageNum,
                pageSize: query.pageSize,
                searchKey: query.searchKey || ''
            });
            doPoster(extra.url, params, function (res) {
                if (res.success === true) {
                    const paging = res.data || {};
                    const rows = Array.isArray(paging.data) ? paging.data : [];
                    const convert = typeof extra.convertRow === 'function'
                        ? extra.convertRow
                        : function (row) {
                            return row && row.id ? {label: row.name || row.id, value: row.id} : null;
                        };
                    const data = [];
                    rows.forEach(function (row) {
                        const item = convert(row);
                        if (item) {
                            data.push(item);
                        }
                    });
                    onSuccess({
                        pageNum: paging.pageNum || query.pageNum,
                        pageSize: paging.pageSize || query.pageSize,
                        total: paging.total || 0,
                        data: data
                    });
                } else {
                    bootGrowl(res.message || 'Loading failed', 'danger');
                    if (typeof onError === 'function') {
                        onError();
                    }
                }
            });
        },
        onSelect: extra.onSelect
    };
}
