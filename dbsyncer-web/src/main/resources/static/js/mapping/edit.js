// EditDriverName
function mappingModifyName() {
    const $name = $("#mapping_name_modify");
    const tmp = $name.text();
    $name.text("");
    $name.append("<input type='text' class='form-control text-md'/>");
    const $input = $name.find("input");
    $input.focus().val(tmp);
    $input.blur(function () {
        $name.text($(this).val());
        $("#mappingModifyForm input[name='name']").val($(this).val());
        $input.unbind();
    });
}

// TieFull + incrementalSwitchEvent
function bindMappingModelChange() {
    const mappingModel = $('#mapping_model').radioGroup({
        theme: 'info',
        size: 'md',
        onChange: function(value, label) {
            showSuperConfig(value);
        }
    });
    // Render Selection ratifyradioConfigure
    showSuperConfig(mappingModel.getValue());
}
function showSuperConfig(model){
    const full = $("#mapping_full");
    const increment = $("#mapping_increment");
    if ('full' === model) {
        increment.addClass("hidden");
    } else {
        increment.removeClass("hidden");
    }
}

// TieLog+ScheduledSwitchEvent
function bindIncrementConfigChange(){
    const incrementStrategy = $('#increment_strategy').radioGroup({
        theme: 'info',
        size: 'md',
        onChange: function(value, label) {
            showIncrementConfig(value);
        }
    });
    // Render Selection ratifyradioConfigure
    showIncrementConfig(incrementStrategy.getValue());
}
function showIncrementConfig(model){
    const incrementQuartz = $("#increment_quartz");
    if ('log' === model) {
        incrementQuartz.addClass("hidden");
    } else {
        incrementQuartz.removeClass("hidden");
    }
}

// EditIncrementalPoints 71Configure
function bindSnapshotModifyClick() {
    $(".metaSnapshotModify").click(function(){
        const $cell = $(this).closest('tr').find("td:eq(2)");
        const oldValue = $cell.text();
        const $input = $('<input type="text" class="form-control"/>').val(oldValue);

        $cell.html($input);
        $input.focus().select();

        $input.one('blur', function () {
            const newValue = $(this).val();
            $cell.text(newValue);
            if (oldValue !== newValue) {
                createMetaSnapshotParams();
            }
        });
    })
}
// GenerateIncrementalPoints 71ConfigureParameter
function createMetaSnapshotParams() {
    const snapshot = {};
    $("#mappingMetaSnapshotConfig tr").each(function () {
        snapshot[$(this).find("td:eq(1)").text()] = $(this).find("td:eq(2)").text();
    });
    $("#metaSnapshot").val(JSON.stringify(snapshot));
}


function resolveConnectorType($connector, connectorId) {
    const fromSelect = typeof resolveConnectorTypeFromSelect === 'function'
        ? resolveConnectorTypeFromSelect($connector, connectorId) : '';
    if (fromSelect) {
        return fromSelect;
    }
    if (!$connector || !connectorId) {
        return '';
    }
    const $option = $connector.find('option[value="' + connectorId + '"]');
    if (!$option.length) {
        return '';
    }
    const text = $option.text() || '';
    const match = text.match(/\(([^()]+)\)\s*$/);
    return match && match[1] ? match[1].trim() : '';
}

function isOracleConnector($connector, connectorId) {
    const type = resolveConnectorType($connector, connectorId);
    // Dream /OceanBaseOracle and Oracle Same asSchema(User) To name space,NoneIndependenceLibraryList
    return type && ['oracle', 'dameng', 'oceanbaseoracle'].indexOf(String(type).toLowerCase()) >= 0;
}

function toggleDatabaseFieldVisibility($database, hide) {
    $database.closest('.form-item').toggleClass('hidden', !!hide);
}

function onDBChange(connectorId, schemaSelect, dbName, defaultSchema, oracleMode) {
    schemaSelect.setData([]);
    schemaSelect.setValues([], true);
    if (!connectorId) {
        return;
    }
    if (!oracleMode && !dbName) {
        return;
    }
    doGetter('/connector/getSchema', {id: connectorId, database: dbName || ''}, function (response) {
        if (response.success) {
            const schemas = response.data || [];
            const array = schemas.map(function (schema) {
                return {label: schema, value: schema, disabled: false};
            });
            schemaSelect.setData(array);
            if (defaultSchema && array.some(function (item) { return item.value === defaultSchema; })) {
                schemaSelect.setValues([defaultSchema], true);
            }
        } else {
            bootGrowl("Could not close temporary folder: %s " + response.message, "danger");
        }
    });
}

function onConnectorChange(connectorId, dbSelect, schemaSelect, $connector, $database, defaultDatabase, defaultSchema) {
    const oracleMode = isOracleConnector($connector, connectorId);
    toggleDatabaseFieldVisibility($database, oracleMode);

    dbSelect.setData([]);
    dbSelect.setValues([], true);
    schemaSelect.setData([]);
    schemaSelect.setValues([], true);
    if (!connectorId) {
        return;
    }
    if (oracleMode) {
        onDBChange(connectorId, schemaSelect, '', defaultSchema, true);
        return;
    }
    doGetter('/connector/getDatabase', {id: connectorId}, function (response) {
        if (response.success) {
            const databases = response.data || [];
            const db = databases.map(function (dbName) {
                return {label: dbName, value: dbName, disabled: false};
            });
            dbSelect.setData(db);
            if (defaultDatabase && db.some(function (item) { return item.value === defaultDatabase; })) {
                dbSelect.setValues([defaultDatabase], true);
                onDBChange(connectorId, schemaSelect, defaultDatabase, defaultSchema, false);
            }
        } else {
            bootGrowl("Could not close temporary folder: %s " + response.message, "danger");
        }
    });
}

function initDBSelect($connector, $database, $schema, role) {
    // For eachselect Group maintains independenceConnectorID，Avoid context string
    let currentConnectorId = null;
    const defaultDatabase = $database.data("database") || '';
    const defaultSchema = $schema.data("schema") || '';

    const schemaSelect = $schema.dbSelect({
        type: 'single',
        defaultValue: defaultSchema ? [defaultSchema] : null
    });
    const dbSelect = $database.dbSelect({
        type: 'single',
        defaultValue: defaultDatabase ? [defaultDatabase] : null,
        onSelect: function (selected) {
            if (currentConnectorId) {
                onDBChange(currentConnectorId, schemaSelect, selected.length >= 1 ? selected[0] : '', '', false);
            }
        }
    });
    const connectorSelect = $connector.dbSelect(buildRemoteConnectorSelectOptions({
        role: role,
        defaultValue: $connector.data('defaultId') || '',
        defaultLabel: $connector.data('defaultLabel') || '',
        defaultType: $connector.data('defaultType') || '',
        preferredType: $connector.data('defaultType') || '',
        onSelect: function (connectorId) {
            currentConnectorId = connectorId.length >= 1 ? connectorId[0] : '';
            onConnectorChange(currentConnectorId, dbSelect, schemaSelect, $connector, $database);
        }
    }));

    // Initialization: if selectedHAConnector，LoadLibraryList
    const selected = connectorSelect.getValues();
    if (selected.length >= 1) {
        currentConnectorId = selected[0];
        onConnectorChange(currentConnectorId, dbSelect, schemaSelect, $connector, $database, defaultDatabase, defaultSchema);
    }
}

$(function () {
    // DefinitionsBackFunctions，SonPageNoodles.Back
    window.backIndexPage = function () {
        doLoader('/mapping/list');
    };

    initDBSelect($('#sourceConnectorId'), $('#sourceDatabase'), $('#sourceSchema'), 'source');
    initDBSelect($('#targetConnectorId'), $('#targetDatabase'), $('#targetSchema'), 'target');
    // TieFull + incrementalSwitchEvent
    bindMappingModelChange();
    // TieLog+ScheduledSwitchEvent
    bindIncrementConfigChange();
    // TieIncrementalPoints 71ConfigureEditEvent
    bindSnapshotModifyClick();

    // Save
    $("#mappingSubmitBtn").click(function () {
        let $form = $("#mappingModifyForm");
        if (validateForm($form)) {
            const $btn = $(this);
            const originalText = $btn.html();
            $btn.html('<i class="fa fa-spinner fa-spin"></i> Saving...').prop('disabled', true);
            doPoster("/mapping/edit", $form.serializeJson(), function (response) {
                $btn.html(originalText).prop('disabled', false);
                if (response.success === true) {
                    bootGrowl("Saved successfully!", "success");
                    const rawStr = localStorage.getItem("dbsyncer.pagination.mapping-list");
                    const raw = JSON.parse(rawStr);
                    raw.pageNum = 1;
                    localStorage.setItem('dbsyncer.pagination.mapping-list', JSON.stringify(raw));
                    // BackTo Default MasterPage
                    backIndexPage();
                } else {
                    bootGrowl(response.message, "danger");
                }
            });
        }
    });

})