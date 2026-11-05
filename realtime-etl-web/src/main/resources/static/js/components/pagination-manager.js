/**
 * Shared pagination manager
 * @param {Object} options ConfigureOptions
 *   - requestUrl: Address requested (required)
 *   - tableBodySelector: Table Body Selector (required)
 *   - paginationSelector: minPageArea Selector (optional, automatically in case not provided)table Create later)
 *   - countSelector: TotalNumber of elements selected (optional, default:.totalCount）
 *   - currentPageSelector: CurrentPageDisplays the Continuous Selector of Elements (optional, default:.currentPage）
 *   - totalPagesSelector: TotalPageNumber of elements selected (optional, default:.totalPages）
 *   - renderRow: CustomLine RenderFunctions(item, index)（77)
 *   - emptyHtml: NoneData-timeHTML（Optional)
 *   - storageKey: localStorage Cache key,Enduring pageNum、pageSize（Optional)
 */
(function(window, $) {
    'use strict';

    const PAGINATION_STORAGE_PREFIX = 'realtime-etl.pagination.';

    function loadPaginationState(storageKey) {
        if (!storageKey) {
            return null;
        }
        try {
            const raw = localStorage.getItem(PAGINATION_STORAGE_PREFIX + storageKey);
            if (raw) {
                return JSON.parse(raw);
            }
        } catch (e) {
            console.warn('[PaginationManager] Reading page cache failed', e);
        }
        return null;
    }

    function savePaginationState(storageKey, pageNum, pageSize) {
        if (!storageKey) {
            return;
        }
        try {
            localStorage.setItem(PAGINATION_STORAGE_PREFIX + storageKey, JSON.stringify({
                pageNum: pageNum,
                pageSize: pageSize
            }));
        } catch (e) {
            console.warn('[PaginationManager] Save page cache failed', e);
        }
    }
    
    // Reliance on checking
    if (typeof window.doPoster !== 'function') {
        console.error('[PaginationManager] relies on the doPoster function not found');
        return;
    }
    if (typeof window.bootGrowl !== 'function') {
        console.error('[PaginationManager] relies on the BootGrowl function not found');
        return;
    }
    
    function createPaginationNavBtn(label, iconClass, disabled, onClick) {
        const btn = $(`<button type="button" class="pagination-btn pagination-btn-nav" title="${label}" ${disabled ? 'disabled' : ''}>
            <i class="fa ${iconClass}"></i>
        </button>`);
        if (!disabled && typeof onClick === 'function') {
            btn.on('click', onClick);
        }
        return btn;
    }

    function applyProgressFillWidths(container) {
        $(container).find('.progress-fill[data-progress-width]').each(function () {
            const width = this.getAttribute('data-progress-width');
            if (width !== null && width !== '') {
                this.style.setProperty('--progress-width', width + '%');
            }
        });
    }

    function getPageWindow(currentPage, totalPages, windowSize) {
        if (totalPages <= windowSize) {
            return { start: 1, end: totalPages };
        }
        let start = currentPage - Math.floor(windowSize / 2);
        let end = start + windowSize - 1;
        if (start < 1) {
            start = 1;
            end = windowSize;
        }
        if (end > totalPages) {
            end = totalPages;
            start = totalPages - windowSize + 1;
        }
        return { start: start, end: end };
    }

    function PaginationManager(options) {
        const storageKey = options.storageKey || '';
        const stored = loadPaginationState(storageKey);
        const defaultPageSize = options.pageSize || 5;
        const defaultPageIndex = options.pageIndex || 1;
        const config = {
            requestUrl: options.requestUrl,
            tableBodySelector: options.tableBodySelector,
            paginationSelector: options.paginationSelector,
            countSelector: options.countSelector,
            currentPageSelector: options.currentPageSelector,
            totalPagesSelector: options.totalPagesSelector,
            renderRow: options.renderRow,
            emptyHtml: options.emptyHtml || '',
            params: options.params || {},
            pageIndex: (stored && stored.pageNum) ? stored.pageNum : defaultPageIndex,
            pageSize: (stored && stored.pageSize) ? stored.pageSize : defaultPageSize,
            storageKey: storageKey,
            customPageSize: options.customPageSize || false,
            customPageSizeItems: options.customPageSizeItems ||  [5, 10, 50, 100, 200],
            showBoundaryButtons: options.showBoundaryButtons !== false,
            refreshCompleted: options.refreshCompleted || function() {}
        };

        const persistState = function(pageNum) {
            savePaginationState(config.storageKey, pageNum, config.pageSize);
        };

        // AutocreateminPageContainers and structures
        this.initPaginationStructure = function() {
            let $pagination = config.paginationSelector ? $(config.paginationSelector) : $();
            let paginationId = '';
            
            // IfminPageThe Supervision System does not exist.table Create later
            if ($pagination.length === 0) {
                const $table = $(config.tableBodySelector).closest('table');
                if ($table.length === 0) {
                    console.error('[PaginationManager] Could not find table elements:', config.tableBodySelector);
                    return;
                }
                
                // CreateminPageContainer and insert totable Back
                paginationId = config.paginationSelector 
                    ? config.paginationSelector.replace('#', '') 
                    : 'pagination_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
                $pagination = $('<div id="' + paginationId + '"></div>');
                $table.after($pagination);
                
                // UpdateConfigureSelector in
                config.paginationSelector = '#' + paginationId;
            } else {
                // If the container already exists, get itID，If not,GenerateOne.
                paginationId = $pagination.attr('id');
                if (!paginationId) {
                    paginationId = 'pagination_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
                    $pagination.attr('id', paginationId);
                    config.paginationSelector = '#' + paginationId;
                }
            }

            // GenerateOnly Selector Prefix (based onminPagePlansID）
            const selectorPrefix = '#' + paginationId + ' ';
            
            //              If not specifiedCustomSelector, use based onminPageThe only choice for the container.
            config.countSelector = config.countSelector || selectorPrefix + '.totalCount';
            config.currentPageSelector = config.currentPageSelector || selectorPrefix + '.currentPage';
            config.totalPagesSelector = config.totalPagesSelector || selectorPrefix + '.totalPages';

            // InspectionYesNoExistingminPageButton container, if not, automatically create the complete structure
            const $paginationBar = $pagination.find('.pagination-bar');
            if ($paginationBar.length === 0) {
                // No style class,AddDefault Styles
                if (!$pagination.hasClass('p-5')) {
                    $pagination.addClass('p-5 border-t border-gray-100 flex items-center justify-between');
                }
                
                // InspectionYesNoExistingminPageInfoText
                const $paginationInfo = $pagination.find('.pagination-info');
                if ($paginationInfo.length === 0) {
                    // CreateminPageInfoText
                    $pagination.prepend(`
                        <p class="text-sm text-gray-500 pagination-info">
                            Total <span class="totalCount">0</span> , page  <span class="currentPage">1</span> / <span class="totalPages">1</span> Page
                        </p>
                    `);
                }
                
                // CreateminPageButton Container
                $pagination.append('<div class="pagination-bar flex items-center"></div>');
            }
        };

        this.doSearch = function(params, pageNum) {
            const searchParams = $.extend({}, this.lastSearchParams || {}, params || {});
            this.lastSearchParams = $.extend({}, searchParams);
            const targetPage = pageNum || this.currentPage || config.pageIndex;
            searchParams.pageNum = targetPage;
            searchParams.pageSize = config.pageSize;
            const pagination = this;
            window.doPoster(config.requestUrl, searchParams, function(data) {
                if (data.success === true) {
                    pagination.refreshPagination(data, searchParams);
                } else {
                    window.bootGrowl('Search abnormal. Try again.', 'danger');
                }
            });
        };

        this.refreshPagination = function(response, params) {
            const result = response.data || {};
            const items = result.data || [];
            const total = result.total || 0;
            const pageNum = result.pageNum || config.pageIndex;
            const totalPages = Math.ceil(total / config.pageSize) || 1;

            // CurrentPageWhen beyond the scope of validity2 PageDelete Data) Autoback to LastPage
            if (pageNum > totalPages) {
                this.doSearch(params, Math.max(1, totalPages));
                return;
            }

            // UpdateminPageManagerStatus
            this.currentPage = pageNum;
            config.pageIndex = pageNum;
            this.total = total;
            persistState(pageNum);
            // Render Table
            this.renderTable(items);
            // UpdateminPageInfo
            this.updateInfo(total, pageNum);

            // RenderminPagebutton
            this.renderPagination(pageNum, totalPages, (nextPage) => {
                this.doSearch(params, nextPage);
            });

            // Show / HideminPageRegional
            this.togglePagination(items.length > 0);

            // RefreshFinish
            config.refreshCompleted();
        };

        // Render Table Data
        this.renderTable = function(data) {
            const tbody = $(config.tableBodySelector);
            tbody.empty();
            if (!data || data.length === 0) {
                tbody.append(config.emptyHtml);
                return;
            }
            data.forEach((item, index) => {
                const i = (this.currentPage - 1) * config.pageSize + index + 1;
                const html = config.renderRow(item, i);
                tbody.append(html);
            });
            applyProgressFillWidths(tbody);
        };

        // RenderminPagebutton
        this.renderPagination = function(currentPage, totalPages, onPageChange) {
            const pagination = $(config.paginationSelector);
            const paginationBar = $(config.paginationSelector).find(".pagination-bar");
            const paginationBtns = pagination.find('.pagination-btn, .pagination-page-btn');
            paginationBtns.remove();

            let $this = this;
            if (config.customPageSize) {
                // Check the frameYesNoExists, update selection if availableStatus，Create without
                let $sizeSelect = paginationBar.find('.page-size-select');
                if ($sizeSelect.length === 0) {
                    // Defines each optional optionPageNumber of bars
                    const currentSize = config.pageSize;
                    // Create Down FrameHTML
                    let selectHtml = '<select class="page-size-select ml-2 mr-2 form-control-md">';
                    config.customPageSizeItems.forEach(size => {
                        const selected = size === currentSize ? 'selected' : '';
                        selectHtml += `<option value="${size}" ${selected}>${size}Article/Page</option>`;
                    });
                    selectHtml += '</select>';
                    const $sizeSelect = $(selectHtml);
                    paginationBar.append($sizeSelect);
                    $sizeSelect.dbSelect({
                        type: 'single',
                        onSelect: function (values) {
                            const selectedValue = values && values.length > 0 ? values[0] : '';
                            if (!isBlank(selectedValue)) {
                                const newSize = parseInt(selectedValue);
                                // UpdateConfigureMediumpageSize
                                config.pageSize = newSize;
                                persistState(1);
                                // ResetTo number one.Page♪ And again ♪Search
                                $this.doSearch({}, 1);
                            }
                        }
                    });
                } else {
                    // ScheduledRefreshShow only sync, not triggeronSelect（NoThen I will.doSearch Dead cycle andResetUntil the end.1 Page）
                    const pageSizeApi = $sizeSelect.data('dbSelect');
                    const nextVal = String(config.pageSize);
                    const currentVal = pageSizeApi && typeof pageSizeApi.getValues === 'function'
                        ? String((pageSizeApi.getValues()[0] || ''))
                        : String($sizeSelect.val() || '');
                    if (currentVal !== nextVal) {
                        if (pageSizeApi && typeof pageSizeApi.setValues === 'function') {
                            pageSizeApi.setValues(nextVal, true);
                        } else {
                            $sizeSelect.val(config.pageSize);
                        }
                    }
                }
            }

            if (config.showBoundaryButtons) {
                // First pagebutton
                paginationBar.append(createPaginationNavBtn(
                    'Home',
                    'fa-angle-double-left',
                    currentPage === 1,
                    () => onPageChange(1)
                ));
            }

            // Previousbutton
            paginationBar.append(createPaginationNavBtn(
                'Previous',
                'fa-angle-left',
                currentPage === 1,
                () => onPageChange(currentPage - 1)
            ));

            // PageCode button: TotalPageNumber> 3 Fixed presentation3 Yeah.NoShowAll
            const pageWindow = getPageWindow(currentPage, totalPages, 3);
            for (let i = pageWindow.start; i <= pageWindow.end; i++) {
                const pageBtn = $(`<button type="button" class="pagination-page-btn ${i === currentPage ? 'active' : ''}">${i}</button>`);
                pageBtn.on('click', () => {
                    if (i !== currentPage) {
                        onPageChange(i);
                    }
                });
                paginationBar.append(pageBtn);
            }

            // Nextbutton
            paginationBar.append(createPaginationNavBtn(
                'Next',
                'fa-angle-right',
                currentPage === totalPages,
                () => onPageChange(currentPage + 1)
            ));

            if (config.showBoundaryButtons) {
                // Last pagebutton
                paginationBar.append(createPaginationNavBtn(
                    'Last page',
                    'fa-angle-double-right',
                    currentPage === totalPages,
                    () => onPageChange(totalPages)
                ));
            }
        };

        // UpdateminPageInfo
        this.updateInfo = function(total, pageNo) {
            const totalPages = Math.ceil(total / config.pageSize) || 1;
            $(config.countSelector).text(total);
            $(config.currentPageSelector).text(pageNo);
            $(config.totalPagesSelector).text(totalPages);
            return totalPages;
        };

        // Show / HideminPageRegional
        this.togglePagination = function(show) {
            $(config.paginationSelector).toggle(show);
        };

        // InitializeStatus
        this.currentPage = config.pageIndex;
        this.total = 0;
        this.lastSearchParams = $.extend({}, config.params || {});
        
        // InitializeminPageStructure
        this.initPaginationStructure();
        
        // Start loading data
        this.doSearch(config.params, this.currentPage);
    }
    
    // ExportTo Global Text
    window.PaginationManager = PaginationManager;
    
})(window, jQuery);

