/**
 * Initialize 2D suspendedNotice
 * @param {Object} options - ConfigureOptions
 * @param {string} options.url - TwoD link address
 * @param {string} options.selector - Objective CombinedElement Selector (multiple commas)minamend
 * @param {number} options.size - 2D size, default150
 * @param {string} options.position - Position, Default'bottom'（bottom/top/left/right）
 */
(function(window) {
    'use strict';
    
    function initQRCodePopover(options) {
        const config = {
            url: options.url || '',
            selector: options.selector || '',
            size: options.size || 150,
            position: options.position || 'bottom'
        };

        if (!config.url || !config.selector) {
            console.warn('initQRCodePopover: url and selector are required');
            return;
        }

        // Seems to create hidden 2-D containers
        let qrcodeContainer = document.getElementById('qrcode-temp-container');
        if (!qrcodeContainer) {
            qrcodeContainer = document.createElement('div');
            qrcodeContainer.id = 'qrcode-temp-container';
            qrcodeContainer.style.cssText = 'position: absolute; left: -9999px; top: -9999px;';
            document.body.appendChild(qrcodeContainer);
        }

        // GenerateTwo-dimensional code.
        qrcodeContainer.innerHTML = ''; // Clear
        if (typeof QRCode !== 'undefined') {
            new QRCode(qrcodeContainer, {
                text: config.url,
                width: config.size,
                height: config.size,
                colorDark: '#000000',
                colorLight: '#ffffff',
                correctLevel: QRCode.CorrectLevel.H
            });
        } else {
            console.error('QRCode library not found');
            return;
        }

        // Wait for 2D.Generate
        setTimeout(function() {
            const qrcodeImg = qrcodeContainer.querySelector('img');
            if (!qrcodeImg) {
                console.error('QRCode image not generated');
                return;
            }

            const qrcodeDataUrl = qrcodeImg.src;

            // For all matching elementsAddSuspended effects
            const elements = document.querySelectorAll(config.selector);
            elements.forEach(function(element) {
                // Create SuspendedNotice
                const popover = document.createElement('div');
                popover.className = 'qrcode-popover';
                popover.innerHTML = `
                    <div class="qrcode-arrow"></div>
                    <div class="qrcode-inner">
                        <img src="${qrcodeDataUrl}" alt="WeChat scan" />
                        <div class="qrcode-text text-center text-success">
                            <i class="fa fa-wechat"></i> WeChat scanContact
                        </div>
                    </div>
                `;
                document.body.appendChild(popover);

                let showTimer = null;
                let hideTimer = null;

                // Show 2D code
                function showQRCode() {
                    clearTimeout(hideTimer);
                    showTimer = setTimeout(function() {
                        const rect = element.getBoundingClientRect();
                        const popoverRect = popover.getBoundingClientRect();
                        
                        let top, left;
                        popover.className = 'qrcode-popover qrcode-' + config.position;

                        switch (config.position) {
                            case 'top':
                                top = rect.top - popoverRect.height - 10;
                                left = rect.left + (rect.width - popoverRect.width) / 2;
                                break;
                            case 'left':
                                top = rect.top + (rect.height - popoverRect.height) / 2;
                                left = rect.left - popoverRect.width - 10;
                                break;
                            case 'right':
                                top = rect.top + (rect.height - popoverRect.height) / 2;
                                left = rect.right + 10;
                                break;
                            case 'bottom':
                            default:
                                top = rect.bottom + 10;
                                left = rect.left + (rect.width - popoverRect.width) / 2;
                                break;
                        }

                        popover.style.top = top + window.scrollY + 'px';
                        popover.style.left = left + window.scrollX + 'px';
                        popover.classList.add('show');
                    }, 300); // Delay300ms Show
                }

                // Hide 2D Code
                function hideQRCode() {
                    clearTimeout(showTimer);
                    hideTimer = setTimeout(function() {
                        popover.classList.remove('show');
                    }, 200); // Delay200ms Hide
                }

                // TieEvent
                element.addEventListener('mouseenter', showQRCode);
                element.addEventListener('mouseleave', hideQRCode);
                popover.addEventListener('mouseenter', function() {
                    clearTimeout(hideTimer);
                });
                popover.addEventListener('mouseleave', hideQRCode);
            });
        }, 500);
    }
    
    // ExportTo Global Text
    window.initQRCodePopover = initQRCodePopover;
    
})(window);

