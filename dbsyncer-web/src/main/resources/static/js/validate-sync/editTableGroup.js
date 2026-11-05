$(function() {
    // Cache Common Selector
    const $fieldMappingList = () => $("#fieldMappingList");
    const $fieldCheckboxes = () => $('.fieldCheckbox');
    const $fieldCheckboxAll = () => $('.fieldCheckboxAll');
    const $fieldDelBtn = () => $('#fieldDelBtn');
    const $fieldMapping = () => $("#fieldMapping");

    // InitializeMappingParameter
    function initFieldMappingParams(){
        // GenerateJSONParameter
        let row = [];
        const $list = $fieldMappingList();
        $list.find("tr").each(function(k,v){
            row.push({
                "source":$(this).find("td:eq(1)").text(),
                "target":$(this).find("td:eq(2)").text(),
                "pk": $(this).find("td:eq(3) i").length > 0
            });
        });

        // Based onYesNoTables maintains data 165tbodyFather elementtable）
        const $table = $list.closest('table');
        if (row.length === 0) {
            $table.addClass("hidden");
        } else {
            $table.removeClass("hidden");
        }
        $fieldMapping().val(JSON.stringify(row));
    }

    function bindFieldSelect(selector, onChange){
        return selector.dbSelect({
            type: 'single',
            onSelect: function (data) {
                if (onChange) {
                    onChange(data);
                }
            },
            defaultValue: [],// Default Selection
            customButtons: [ // Up to 2 custom buttons
                {
                    text: 'Refresh Fields',
                    callback: function(values) {
                        bindRefreshTableFieldsClick();
                    }
                }
            ]
        });
    }

    // Tie Form DragEvent（Use originalHTML5 DragAPI）
    function bindFieldMappingDrop() {
        const $list = $fieldMappingList();
        const rows = $list.find("tr");

        // Remove the old one first.EventListening devices.
        rows.off('dragstart dragend dragover dragleave drop');
        rows.each(function() {
            const row = this;
            row.setAttribute('draggable', 'true');
        });

        // UseEventCommissioning, avoiding duplication.
        $list.on('dragstart', 'tr', function(e) {
            e.originalEvent.dataTransfer.effectAllowed = 'move';
            e.originalEvent.dataTransfer.setData('text/html', this.innerHTML);
            $(this).addClass('dragging');
        });

        $list.on('dragend', 'tr', function(e) {
            $(this).removeClass('dragging');
            $list.find('tr').removeClass('drag-over');
            // Update Line Number
            updateRowNumbers();
            initFieldMappingParams();
        });

        $list.on('dragover', 'tr', function(e) {
            e.preventDefault();
            e.originalEvent.dataTransfer.dropEffect = 'move';
            $(this).addClass('drag-over');
            return false;
        });

        $list.on('dragleave', 'tr', function(e) {
            $(this).removeClass('drag-over');
        });

        $list.on('drop', 'tr', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const $dragging = $list.find('.dragging');
            if ($dragging.length && $dragging[0] !== this) {
                const draggingIndex = $dragging.index();
                const targetIndex = $(this).index();

                if (draggingIndex < targetIndex) {
                    $(this).after($dragging);
                } else {
                    $(this).before($dragging);
                }
            }

            $(this).removeClass('drag-over');
            return false;
        });
    }

    // Update Table Line Numbers
    function updateRowNumbers() {
        $fieldMappingList().find("tr").each(function(index) {
            $(this).find("td:eq(0)").text(index + 1);
        });
    }

    // Multicheck or single-check binding check boxesEvent
    function bindFieldMappingCheckbox() {
        const $checkboxAll = $fieldCheckboxAll();
        const $delBtn = $fieldDelBtn();
        let isUpdatingSelectAll = false;

        // UpdateSelect AllCheckboxStatus
        function updateSelectAllState() {
            if (isUpdatingSelectAll) return;
            isUpdatingSelectAll = true;
            const $checkboxes = $fieldCheckboxes();
            const $checkedCheckboxes = $checkboxes.filter(':checked');
            const allChecked = $checkboxes.length > 0 && $checkboxes.length === $checkedCheckboxes.length;
            const checkboxAllApi = $checkboxAll.data('checkboxGroup');
            if (checkboxAllApi) {
                checkboxAllApi.setValue(allChecked);
            } else {
                $checkboxAll.prop('checked', allChecked);
            }
            setTimeout(function() {
                isUpdatingSelectAll = false;
            }, 0);
        }

        // UpdateDeletebuttonStatus
        function updateDeleteButtonState() {
            $delBtn.prop('disabled', $fieldCheckboxes().filter(':checked').length === 0);
        }

        // Remove the old one first.EventTie it up.
        $checkboxAll.off('change');
        $fieldCheckboxes().off('change');

        // InitializeSelect AllCheckbox
        $checkboxAll.checkboxGroup({
            theme: 'danger',
            size: 'md',
            onChange: function(values) {
                // Select All/CancelSelect All, sync all single check boxes
                const isChecked = values.length > 0;
                $fieldCheckboxes().each(function() {
                    const api = $(this).data('checkboxGroup');
                    if (api) {
                        api.setValue(isChecked);
                    }
                });
                updateDeleteButtonState();
            }
        });

        // Initialize an existing check box
        $fieldCheckboxes().each(function() {
            if (!$(this).data('checkboxGroup')) {
                $(this).checkboxGroup({
                    theme: 'danger',
                    size: 'md',
                    onChange: function(values) {
                        updateSelectAllState();
                        updateDeleteButtonState();
                    }
                });
            }
        });
    }

    // TieRefresh tablesFieldEvent
    function bindRefreshTableFieldsClick() {
        let id = $("#tableGroupId").val();
        doPoster("/validate-sync/refreshFields", {'id': id}, function (data) {
            if (data.success === true) {
                bootGrowl("Refresh Corporation succeeded!", "success");
                doLoader('/validate-sync/page/editTableGroup?id=' + id);
            } else {
                bootGrowl(data.message, "danger");
            }
        });
    }

    // TieField mappingTable ClickEvent
    function bindFieldMappingListClick(){
        // UseEventCommissioning, supporting dynamicsAddLine
        const $list = $fieldMappingList();
        $list.off("dblclick", "tr").on('dblclick', 'tr', function () {
            const $pk = $(this).find("td:eq(3)");
            // Update to New Icon Style
            $pk.html($pk.find("i").length > 0 ? '' : '<i title="Primary key" class="fa fa-key text-warning"></i>');
            initFieldMappingParams();
        });
    }

    // TieAddField mappingClickEvent
    function bindFieldMappingAddClick(sourceSelector, targetSelector){
        $("#fieldAddBtn").unbind("click").bind('click', function(){
            let sValues = sourceSelector.getValues();
            let tValues = targetSelector.getValues();
            // Non 159
            if(sValues.length < 1 && tValues.length < 1){
                bootGrowl("At least one field.", "danger");
                return;
            }

            // Check RepeatField
            let sField = sValues[0];
            let tField = tValues[0];
            sField = sField == null ? "" : sField;
            tField = tField == null ? "" : tField;
            let repeated = false;
            const $list = $fieldMappingList();
            const $tr = $list.find("tr");
            $tr.each(function (k, v) {
                let sf = $(this).find("td:eq(1)").text();
                let tf = $(this).find("td:eq(2)").text();
                if (sField === sf && tField === tf) {
                    repeated = true;
                    return false; // Jump out of cycle
                }
            });
            if (repeated) {
                bootGrowl("Field map already exists.", "danger");
                return;
            }

            // ConversionHTMLPreventionXSSAttack!
            const escapedSField = escapeHtml(sField);
            const escapedTField = escapeHtml(tField);
            const rowIndex = $tr.length + 1;

            $list.append(`<tr title='Double-click to set/unset primary key | Drag to reorder'>
                    <td>${rowIndex}</td>
                    <td>${escapedSField}</td>
                    <td>${escapedTField}</td>
                    <td></td>
                    <td onclick="event.stopPropagation();">
                        <input type="checkbox" class="fieldCheckbox" onclick="event.stopPropagation();" />
                    </td>
                </tr>`);

            initFieldMappingParams();
            bindFieldMappingDrop();
            bindFieldMappingListClick();
            bindFieldMappingCheckbox();
        });
    }
    // TieDeleteField mappingClickEvent
    function bindFieldMappingDelClick(){
        $fieldDelBtn().unbind("click").bind('click', function () {
            const checkedRows = [];
            $fieldCheckboxes().filter(':checked').each(function () {
                checkedRows.push($(this).closest('tr'));
            });
            if (checkedRows.length > 0) {
                // DeleteSelected rows
                checkedRows.forEach(function($row) {
                    $row.remove();
                });

                // Update Line Number
                updateRowNumbers();
                // Update MapParameter（Tables are automatically shown/hidden)
                initFieldMappingParams();
                // UpdateDeletebuttonStatus
                $fieldDelBtn().prop('disabled', $fieldCheckboxes().filter(':checked').length === 0);
            }
        });
    }

    // DefinitionsBackFunctions，SonPageNoodles.Back
    let taskId = $("#taskId").val();
    window.backIndexPage = function () {
        doLoader("/validate-sync/page/edit?id=" + taskId);
    };

    // Tie down the pull selectionEventAuto-match IllicitFieldEvent
    let targetTableSelect = bindFieldSelect($('#target_table_field'));
    let sourceTableSelect = bindFieldSelect($('#source_table_field'), function(data) {
        targetTableSelect.setValues(data);
    });

    // Tie TableFieldRelationship ClickEvent
    initFieldMappingParams();
    // Tie Form DragEvent
    bindFieldMappingDrop();
    // TieDeleteTableField mappingEvent
    bindFieldMappingCheckbox();
    // TieAddFieldMappingEvent
    bindFieldMappingAddClick(sourceTableSelect, targetTableSelect);
    // TieDeleteFieldMappingEvent
    bindFieldMappingDelClick();
    // TieFieldRelationship ClickEvent
    bindFieldMappingListClick();

    //Save
    $("#tableGroupSubmitBtn").click(function () {
        let $form = $("#table_group_modify_form");
        if (validateForm($form)) {
            //SaveDriverConfigure
            doPoster("/validate-sync/editTableGroup", $form.serializeJson(), function (data) {
                if (data.success === true) {
                    bootGrowl("Change table field map successfully!", "success");
                    backIndexPage();
                } else {
                    bootGrowl(data.message, "danger");
                }
            });
        }
    });
});