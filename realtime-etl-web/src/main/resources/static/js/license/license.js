$(function () {
    // Initialise Tab Input Box
    initMultipleInputTags();

// Remove Activated Code
    $("#removeBtn").on('click', function(){
        const $btn = $(this);
        showConfirm({
            title: 'When the active code is removed, the product functionality will not be available, and is it confirmed that it is deleted?',
            icon: 'warning',
            size: 'large',
            confirmType: 'danger',
            onConfirm: function () {
                // Disablebutton to prevent repeat clicks
                const originalText = $btn.html();
                $btn.html('<i class="fa fa-spinner fa-spin"></i> Deleting...').prop('disabled', true)
                doPoster("/license/remove", {}, function (response) {
                    $btn.prop('disabled', false);
                    $btn.html(originalText);
                    if (response.success === true) {
                        bootGrowl("Remove active code successfully!", "success");
                        refreshLicense();
                        doLoader("/license");
                    } else {
                        bootGrowl(response.message || "Delete failed", "danger");
                    }
                });
            }
        });
    });

    // Online Activation
    $("#activateBtn").on('click', function(){
        const $form = $("#license-form");
        const $btn = $(this);
        // Prevention of duplicationSubmit
        if ($btn.prop('disabled')) {
            return;
        }
        if (!validateForm($form)) {
            return;
        }

        const originalText = $btn.html();
        $btn.html('<i class="fa fa-spinner fa-spin"></i>Activating...').prop('disabled', true);
        doPoster("/license/activate", $form.serializeJson(), function (response) {
            $btn.html(originalText).prop('disabled', false);

            if (response.success === true) {
                bootGrowl("Online activation!", "success");
                refreshLicense();
                doLoader("/license");
            } else {
                bootGrowl(response.message || "Activate Failed", "danger");
            }
        });
    });

    // Copy machine code
    $("#copyBtn").on('click', function(){
        const licenseKey = document.getElementById("licenseKey");
        const $btn = $(this);
        copyToClipboard(licenseKey.value, {
            button: $btn,
            successMessage: "Copy the machine code!",
            originalText: "Copy",
            originalIcon: "fa-copy"
        });
    });

    // InitializeDocumentationUploadComponent
    initFileUpload('#licenseUploader', {
        uploadUrl: $basePath + '/license/upload',
        maxFiles: 1,
        maxSize: 10 * 1024 * 1024, // 10MB
        autoUpload: true,
        onSuccess: function(file, response) {
            if (response.success) {
                bootGrowl("Activ Reflect Code Upload Success!", "success");
                refreshLicense();
                doLoader("/license");
            } else {
                bootGrowl(response.message || "Upload failed", "danger");
            }
        },
        onError: function(file, error) {
            bootGrowl(error || "Upload failed", "danger");
        }
    });
});