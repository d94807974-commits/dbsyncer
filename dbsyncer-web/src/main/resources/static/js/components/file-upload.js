/**
 * DocumentationUploadInitiation of components
 * @param {string} selector - UploadContainer Selector
 * @param {object} options - ConfigureOptions
 */
(function(window) {
    'use strict';
    
    // Reliance on checking
    if (typeof window.notify !== 'function') {
        console.error('[init WhilstUpload] relies notify function not found');
        return;
    }
    
    function initFileUpload(selector, options) {
        var container = document.querySelector(selector);
        if (!container) {
            console.error('File upload container not found:' + selector);
            return;
        }

        var uploadArea = container.querySelector('[data-upload-area]');
        var uploadList = container.querySelector('[data-upload-list]');
        var fileInput = container.querySelector('.upload-input');
        
        if (!uploadArea || !uploadList || !fileInput) {
            console.error('File upload component elements incomplete');
            return;
        }

        // DefaultConfigure
        var config = {
            uploadUrl: options.uploadUrl || '/upload',
            accept: options.accept || [],
            maxFiles: options.maxFiles || 10,
            maxSize: options.maxSize || 50 * 1024 * 1024, // Default50MB
            autoUpload: options.autoUpload !== false, // Default AutoUpload
            onSuccess: options.onSuccess || function() {},
            onError: options.onError || function() {},
            onProgress: options.onProgress || function() {}
        };

        var fileList = [];
        var uploadingCount = 0;

        // ClickUploadArea TriggerDocumentationSelection
        uploadArea.addEventListener('click', function(e) {
            if (!uploadArea.classList.contains('disabled')) {
                fileInput.click();
            }
        });

        // DocumentationSelection
        fileInput.addEventListener('change', function(e) {
            handleFiles(e.target.files);
            fileInput.value = ''; // Clearinput，Allow repeated selection of the sameDocumentation
        });

        // DragUpload
        uploadArea.addEventListener('dragover', function(e) {
            e.preventDefault();
            e.stopPropagation();
            uploadArea.classList.add('dragover');
        });

        uploadArea.addEventListener('dragleave', function(e) {
            e.preventDefault();
            e.stopPropagation();
            uploadArea.classList.remove('dragover');
        });

        uploadArea.addEventListener('drop', function(e) {
            e.preventDefault();
            e.stopPropagation();
            uploadArea.classList.remove('dragover');
            handleFiles(e.dataTransfer.files);
        });

        // ProcessingDocumentation
        function handleFiles(files) {
            if (!files || files.length === 0) return;

            // InspectionDocumentationQuantity limit
            if (fileList.length + files.length > config.maxFiles) {
                window.notify({ 
                    message: 'Only upload at most. ' + config.maxFiles + ' File', 
                    type: 'warning' 
                });
                return;
            }

            Array.from(files).forEach(function(file) {
                // AuthenticationDocumentation
                if (!validateFile(file)) return;

                // AddPresent.DocumentationList
                const fileObj = {
                    id: Date.now() + '_' + Math.random().toString(36).substr(2, 9),
                    file: file,
                    name: file.name,
                    size: file.size,
                    status: 'pending', // pending, uploading, success, error
                    progress: 0,
                    error: null
                };

                fileList.push(fileObj);
                renderFileItem(fileObj);

                // AutoUpload
                if (config.autoUpload) {
                    uploadFile(fileObj);
                }
            });
        }

        // AuthenticationDocumentation
        function validateFile(file) {
            // InspectionDocumentationExtension
            if (config.accept.length > 0) {
                const ext = '.' + file.name.split('.').pop().toLowerCase();
                if (config.accept.indexOf(ext) === -1) {
                    window.notify({ 
                        message: 'Unsupported file type:' + ext, 
                        type: 'warning' 
                    });
                    return false;
                }
            }

            // InspectionDocumentation/5/
            if (file.size > config.maxSize) {
                window.notify({ 
                    message: 'Documentation "' + file.name + '" Beyond the size limit (' + formatFileSize(config.maxSize) + '）', 
                    type: 'warning' 
                });
                return false;
            }

            return true;
        }

        // RenderDocumentationItem
        function renderFileItem(fileObj) {
            const item = document.createElement('div');
            item.className = 'upload-item ' + fileObj.status;
            item.setAttribute('data-file-id', fileObj.id);
            item.innerHTML = 
                '<div class="upload-item-icon">' +
                    '<i class="fa fa-file-o"></i>' +
                '</div>' +
                '<div class="upload-item-info">' +
                    '<div class="upload-item-name" title="' + fileObj.name + '">' + fileObj.name + '</div>' +
                    '<div class="upload-item-size">' + formatFileSize(fileObj.size) + '</div>' +
                    '<div class="upload-item-progress" style="display:none;">' +
                        '<div class="upload-item-progress-bar" style="width:0%"></div>' +
                    '</div>' +
                '</div>' +
                '<div class="upload-item-status"></div>' +
                '<button type="button" class="upload-item-remove" data-action="remove">' +
                    '<i class="fa fa-times"></i>' +
                '</button>';

            // TieDeleteEvent
            item.querySelector('[data-action="remove"]').addEventListener('click', function() {
                removeFile(fileObj.id);
            });

            uploadList.appendChild(item);
        }

        // UpdateDocumentationItemStatus
        function updateFileItem(fileObj) {
            const item = uploadList.querySelector('[data-file-id="' + fileObj.id + '"]');
            if (!item) return;

            item.className = 'upload-item ' + fileObj.status;

            const statusEl = item.querySelector('.upload-item-status');
            const progressEl = item.querySelector('.upload-item-progress');
            const progressBar = item.querySelector('.upload-item-progress-bar');

            if (fileObj.status === 'uploading') {
                statusEl.innerHTML = '<div class="upload-status-icon loading"><i class="fa fa-spinner fa-spin"></i></div>';
                progressEl.style.display = 'block';
                progressBar.style.width = fileObj.progress + '%';
            } else if (fileObj.status === 'success') {
                statusEl.innerHTML = '<div class="upload-status-icon success"><i class="fa fa-check-circle"></i></div>';
                progressEl.style.display = 'none';
            } else if (fileObj.status === 'error') {
                statusEl.innerHTML = '<div class="upload-status-icon error"><i class="fa fa-exclamation-circle"></i></div>';
                progressEl.style.display = 'none';
                item.title = fileObj.error || 'Upload failed';
            }
        }

        // UploadDocumentation
        function uploadFile(fileObj) {
            if (uploadingCount >= 3) { // Restriction simultaneous distributionUploadNumber
                setTimeout(function() {
                    uploadFile(fileObj);
                }, 1000);
                return;
            }

            fileObj.status = 'uploading';
            fileObj.progress = 0;
            updateFileItem(fileObj);
            uploadingCount++;

            const formData = new FormData();
            formData.append('files', fileObj.file);

            const xhr = new XMLHttpRequest();

            // UploadProgress
            xhr.upload.addEventListener('progress', function(e) {
                if (e.lengthComputable) {
                    fileObj.progress = Math.round((e.loaded / e.total) * 100);
                    updateFileItem(fileObj);
                    config.onProgress(fileObj, e);
                }
            });

            // UploadFinish
            xhr.addEventListener('load', function() {
                uploadingCount--;
                try {
                    const response = JSON.parse(xhr.responseText);
                    if (xhr.status === 200 && response.success) {
                        fileObj.status = 'success';
                        updateFileItem(fileObj);
                        config.onSuccess(fileObj, response);
                    } else {
                        fileObj.status = 'error';
                        fileObj.error = response.message || 'Upload failed';
                        updateFileItem(fileObj);
                        config.onError(fileObj, fileObj.error);
                    }
                } catch (e) {
                    fileObj.status = 'error';
                    fileObj.error = 'Server response format error';
                    updateFileItem(fileObj);
                    config.onError(fileObj, fileObj.error);
                }
            });

            // UploadError
            xhr.addEventListener('error', function() {
                uploadingCount--;
                fileObj.status = 'error';
                fileObj.error = 'Network error';
                updateFileItem(fileObj);
                config.onError(fileObj, fileObj.error);
            });

            xhr.open('POST', config.uploadUrl, true);
            xhr.send(formData);
        }

        // DeleteDocumentation
        function removeFile(fileId) {
            var index = fileList.findIndex(function(f) { return f.id === fileId; });
            if (index === -1) return;

            var fileObj = fileList[index];
            
            // If it isUpload，Yeah.CancelUpload（Yes.SavexhrObject)
            if (fileObj.status === 'uploading') {
                window.notify({ message: 'File is being uploaded and cannot be deleted', type: 'warning' });
                return;
            }

            // RemoveDOM
            var item = uploadList.querySelector('[data-file-id="' + fileId + '"]');
            if (item) {
                item.style.animation = 'fadeOut 0.3s ease-out';
                setTimeout(function() {
                    item.parentNode.removeChild(item);
                }, 300);
            }

            // Remove Data
            fileList.splice(index, 1);
        }

        // FormattingDocumentation/5/
        function formatFileSize(bytes) {
            if (bytes === 0) return '0 B';
            var k = 1024;
            var sizes = ['B', 'KB', 'MB', 'GB'];
            var i = Math.floor(Math.log(bytes) / Math.log(k));
            return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i];
        }

        // Open methods
        return {
            getFiles: function() { return fileList; },
            clearFiles: function() {
                fileList = [];
                uploadList.innerHTML = '';
            },
            uploadAll: function() {
                fileList.filter(function(f) { 
                    return f.status === 'pending'; 
                }).forEach(function(f) {
                    uploadFile(f);
                });
            }
        };
    }
    
    // ExportTo Global Text
    window.initFileUpload = initFileUpload;
    
})(window);

