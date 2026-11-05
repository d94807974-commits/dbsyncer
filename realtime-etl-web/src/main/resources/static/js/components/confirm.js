/**
 * ConfirmDialogue Component
 * @param {Object} options - ConfigureOptions
 *   - title: dialogue box title(s)and titleHtml Two or one, or both.title as plain text title)
 *   - titleHtml: CustomTitleHTML（No conversion, replace defaulth3 Title)
 *   - titleExtra: Expand Below HeaderHTML（No conversion, e.g. statistical abstract)
 *   - message: dialogue box contentsInfo
 *   - body: dialogue box mainHTMLContent (optional)
 *   - icon: IconType（info/warning/error/success，Defaultinfo）
 *   - confirmText: ConfirmButton Text（Default'OK'）
 *   - cancelText: CancelButton Text（Default'Cancel'）
 *   - confirmType: ConfirmbuttonType（primary/success/warning/danger/info，Defaultprimary）
 *   - size: Dialog Size (normal/large/xlarge/max，Defaultnormal）
 *   - position: Position of the bullet windowtop/center，Defaultcenter）
 *   - showConfirm: YesNoShowConfirmButton (default)true）
 *   - showCancel: YesNoShowCancelButton (default)true）
 *   - closeOnConfirm: ClickConfirmBackYesNoClose（Defaulttrue）
 *   - closeOnCancel: ClickCancelBackYesNoClose（Defaulttrue）
 *   - onConfirm: ConfirmRewindFunctions
 *   - onCancel: CancelRewindFunctions（Optional)
 */
(function(window) {
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
    
    function showConfirm(options) {
        options = options || {};
        
        // ParameterAuthentication
        if (!options.title && !options.titleHtml) {
            console.warn('[showConfilm] Missing necessary parameters: type or typeHtml');
            return;
        }

        // DefaultConfigure
        const config = {
            title: options.title || '',
            titleHtml: options.titleHtml || '',
            titleExtra: options.titleExtra || '',
            message: options.message || '',
            body: options.body || '',
            icon: options.icon || 'info',
            confirmText: options.confirmText || 'OK',
            cancelText: options.cancelText || 'Cancel',
            confirmType: options.confirmType || 'primary',
            size: options.size || 'normal',
            position: options.position || 'center',
            showConfirm: options.showConfirm !== false,
            showCancel: options.showCancel !== false,
            closeOnConfirm: options.closeOnConfirm !== false,
            closeOnCancel: options.closeOnCancel !== false,
            onConfirm: options.onConfirm || function() {},
            onCancel: options.onCancel || function() {}
        };

        // AuthenticationiconType
        const validIcons = ['info', 'warning', 'error', 'success'];
        if (validIcons.indexOf(config.icon) === -1) {
            config.icon = 'info';
        }

        // Authenticationsize
        const validSizes = ['normal', 'large', 'xlarge', 'max'];
        if (validSizes.indexOf(config.size) === -1) {
            config.size = 'normal';
        }

        const validPositions = ['top', 'center'];
        if (validPositions.indexOf(config.position) === -1) {
            config.position = 'center';
        }

        // AuthenticationConfirmbuttonType
        const validTypes = ['primary', 'success', 'warning', 'danger', 'info'];
        if (validTypes.indexOf(config.confirmType) === -1) {
            config.confirmType = 'primary';
        }

        // Accessicon HTML
        function getIconHTML() {
            const iconMap = {
                'info': '<i class="fa fa-info-circle"></i>',
                'warning': '<i class="fa fa-exclamation-triangle"></i>',
                'error': '<i class="fa fa-times-circle"></i>',
                'success': '<i class="fa fa-check-circle"></i>'
            };
            return iconMap[config.icon] || iconMap['info'];
        }

        // CreateHTML
        const overlayId = 'confirm-overlay-' + Date.now();
        const dialogId = 'confirm-dialog-' + Date.now();
        
        const overlay = document.createElement('div');
        overlay.id = overlayId;
        overlay.className = 'confirm-overlay confirm-overlay-position-' + config.position;

        const dialog = document.createElement('div');
        dialog.id = dialogId;
        dialog.className = 'confirm-dialog size-' + config.size;

        // Build headHTML
        let headerHTML = '';
        if (config.title || config.titleHtml || config.message || config.titleExtra) {
            let titleHTML = '';
            if (config.titleHtml) {
                titleHTML = `<div class="confirm-title-html">${config.titleHtml}</div>`;
            } else if (config.title) {
                titleHTML = `<h3 class="confirm-title">${escapeHtml(config.title)}</h3>`;
            }
            headerHTML = `
                <div class="confirm-header">
                    <div class="confirm-icon icon-${config.icon}">
                        ${getIconHTML()}
                    </div>
                    <div class="confirm-content-wrapper">
                        ${titleHTML}
                        ${config.titleExtra ? `<div class="confirm-title-extra">${config.titleExtra}</div>` : ''}
                        ${config.message ? `<p class="confirm-message">${escapeHtml(config.message)}</p>` : ''}
                    </div>
                </div>
            `;
        }

        // Build bodyHTML
        let bodyHTML = '';
        if (config.body) {
            bodyHTML = `<div class="confirm-body">${config.body}</div>`;
        }

        let footerHTML = '';
        if (config.showConfirm || config.showCancel) {
            let footerButtons = '';
            if (config.showConfirm) {
                footerButtons += `<button class="btn confirm-btn confirm-btn-confirm confirm-btn-type-${config.confirmType}">${escapeHtml(config.confirmText)}</button>`;
            }
            if (config.showCancel) {
                footerButtons += `<button class="btn confirm-btn confirm-btn-cancel">${escapeHtml(config.cancelText)}</button>`;
            }
            footerHTML = `<div class="confirm-footer">${footerButtons}</div>`;
        }

        dialog.innerHTML = `
            ${headerHTML}
            ${bodyHTML}
            ${footerHTML}
        `;

        overlay.appendChild(dialog);
        document.body.appendChild(overlay);

        // Signature button
        const confirmBtn = dialog.querySelector('.confirm-btn-confirm');
        const cancelBtn = dialog.querySelector('.confirm-btn-cancel');

        let closed = false;

        function removeKeydownListener() {
            document.removeEventListener('keydown', handleKeydown, true);
        }

        // Closedialogue box
        function closeConfirm() {
            if (closed) {
                return;
            }
            closed = true;
            removeKeydownListener();
            dialog.classList.add('hidden');
            overlay.classList.add('hidden');
            setTimeout(function() {
                if (overlay.parentNode) {
                    overlay.parentNode.removeChild(overlay);
                }
            }, 200);
        }

        function triggerConfirm() {
            if (typeof config.onConfirm === 'function') {
                config.onConfirm();
            }
            if (config.closeOnConfirm) {
                closeConfirm();
            }
        }

        function triggerCancel() {
            if (typeof config.onCancel === 'function') {
                config.onCancel();
            }
            if (config.closeOnCancel) {
                closeConfirm();
            }
        }

        // ConfirmbuttonEvent
        if (confirmBtn) {
            confirmBtn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                triggerConfirm();
            });
        }

        // CancelbuttonEvent
        if (cancelBtn) {
            cancelBtn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                triggerCancel();
            });
        }

        // Click non-dialog areaClose
        overlay.addEventListener('click', function(e) {
            if (e.target === overlay) {
                e.preventDefault();
                triggerCancel();
            }
        });

        // Enter Confirm、Esc Cancel（capture phase, avoid default behaviour Entry of bottom buttons)
        function handleKeydown(e) {
            if (e.key === 'Enter') {
                if (e.isComposing) {
                    return;
                }
                e.preventDefault();
                e.stopPropagation();
                if (cancelBtn && document.activeElement === cancelBtn) {
                    triggerCancel();
                } else if (confirmBtn) {
                    triggerConfirm();
                } else if (cancelBtn) {
                    triggerCancel();
                }
            } else if (e.key === 'Escape') {
                e.preventDefault();
                e.stopPropagation();
                triggerCancel();
            }
        }
        document.addEventListener('keydown', handleKeydown, true);

        // Avoid returning the button that opens the bullet window again (e.g.Delete）
        if (document.activeElement && typeof document.activeElement.blur === 'function') {
            document.activeElement.blur();
        }
        if (confirmBtn) {
            confirmBtn.focus();
        } else if (cancelBtn) {
            cancelBtn.focus();
        }

        return {
            close: closeConfirm,
            getDialog: function() { return dialog; },
            getOverlay: function() { return overlay; }
        };
    }
    
    // ExportTo Global Text
    window.showConfirm = showConfirm;
    
})(window);