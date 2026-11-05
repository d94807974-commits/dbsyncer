/**
 * Initialise Tab Input Box (Tagsinput）
 * Will havedata-role="tagsinput" input boxConvertEnter box for tab
 * SupportAdd/DeleteLabel,SubmitComma-spelled times
 */
(function(window) {
    'use strict';
    
    // HTML Conversion
    function escapeHtml(text) {
        if (typeof window.escapeHtml === 'function') {
            return window.escapeHtml(text);
        }
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    function initMultipleInputTags() {
        const inputs = document.querySelectorAll('input[data-role="tagsinput"]');
        
        inputs.forEach(function(input) {
            // If it's been initialized, skip.
            if (input.classList.contains('tagsinput-initialized')) {
                return;
            }
            
            // Mark as Initialized
            input.classList.add('tagsinput-initialized');
            
            // Create Tab Container
            const container = document.createElement('div');
            container.className = 'tagsinput';
            
            // Create Individual TabsListContainers
            const tagsList = document.createElement('div');
            tagsList.className = 'tagsinput-tags';
            
            // Create a new tab input box
            const newTagInput = document.createElement('input');
            newTagInput.type = 'text';
            newTagInput.className = 'tagsinput-input';
            newTagInput.placeholder = input.placeholder || 'Press Enter after entering';
            
            // Hide original input box
            input.style.display = 'none';
            
            // Insert container toDOM
            input.parentNode.insertBefore(container, input);
            container.appendChild(tagsList);
            container.appendChild(newTagInput);
            
            // Storage TabsValuearray
            let tags = [];
            
            // Initialize existingValue
            const initialValue = input.value || input.getAttribute('value');
            if (initialValue) {
                const initialTags = initialValue.split(',').map(function(tag) {
                    return tag.trim();
                }).filter(function(tag) {
                    return tag !== '';
                });
                initialTags.forEach(function(tag) {
                    addTag(tag);
                });
            }
            
            // AddLabel
            function addTag(value) {
                value = value.trim();
                if (!value) return;
                
                // InspectionYesNoExists
                if (tags.indexOf(value) !== -1) {
                    return;
                }
                
                tags.push(value);
                
                // Create label element
                const tagElement = document.createElement('span');
                tagElement.className = 'tag';
                tagElement.innerHTML = `
                    <span class="tag-text">${escapeHtml(value)}</span>
                    <i class="tag-remove fa fa-times"></i>
                `;
                
                // TieDeleteEvent
                tagElement.querySelector('.tag-remove').addEventListener('click', function() {
                    removeTag(value, tagElement);
                });
                
                tagsList.appendChild(tagElement);
                updateInputValue();
            }
            
            // DeleteLabel
            function removeTag(value, element) {
                const index = tags.indexOf(value);
                if (index > -1) {
                    tags.splice(index, 1);
                }
                element.remove();
                updateInputValue();
            }
            
            // Update ComptrollerValue
            function updateInputValue() {
                input.value = tags.join(',');
            }
            
            // Tag           YesNoInput method input
            let isComposing = false;
            // Tag           YesNoNeed to process after input methodEnter Keys
            let pendingEnter = false;
            
            // The listening input method begins.Event
            newTagInput.addEventListener('compositionstart', function() {
                isComposing = true;
                pendingEnter = false;
            });
            
            // The listening input method is overEvent
            newTagInput.addEventListener('compositionend', function() {
                isComposing = false;
                // If you press it before,Enter Keys, now processAddLabel
                if (pendingEnter) {
                    pendingEnter = false;
                    setTimeout(function() {
                        const value = newTagInput.value.trim();
                        if (value) {
                            addTag(value);
                            newTagInput.value = '';
                        }
                    }, 0);
                }
            });
            
            // ProcessingAddLogic of labels
            function handleAddTag() {
                const value = newTagInput.value.trim();
                if (value) {
                    addTag(value);
                }
                // NoneCommentYesNoSuccessAdd，BothClearInput Box
                newTagInput.value = '';
            }
            
            // Keyboard to listen to the new tab input boxEvent
            newTagInput.addEventListener('keydown', function(e) {
                // If entering method, pressYes Enter Keys
                if (isComposing && e.key === 'Enter') {
                    // Tags need to be processed after input method
                    pendingEnter = true;
                    e.preventDefault();
                    return;
                }
                
                // Check BrowserYesNoSupportisComposing Properties (Additional Protection)
                if (e.isComposing && e.key === 'Enter') {
                    e.preventDefault();
                    return;
                }
                
                if (e.key === 'Enter' || e.key === ',') {
                    e.preventDefault();
                    handleAddTag();
                } else if (e.key === 'Backspace' && !newTagInput.value && tags.length > 0) {
                    // Activities in this area include:DeleteLast Tab
                    const lastTag = tags[tags.length - 1];
                    const lastTagElement = tagsList.lastElementChild;
                    removeTag(lastTag, lastTagElement);
                }
            });
            
            // We lost focus.Event
            newTagInput.addEventListener('blur', function() {
                const value = newTagInput.value.trim();
                if (value) {
                    addTag(value);
                }
                // When you lose focusYesClearInput Box
                newTagInput.value = '';
            });
            
            // Click container to focus on input box
            container.addEventListener('click', function() {
                newTagInput.focus();
            });
        });
    }
    
    // ExportTo Global Text
    window.initMultipleInputTags = initMultipleInputTags;
    
})(window);

