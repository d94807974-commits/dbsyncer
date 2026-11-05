/**
 * Database migration - Create/EditPage
 */
(function () {
    const page = window.DATABASE_SYNC_PAGE || { mode: 'add', readOnly: false, task: null };
    const TABLE_PAGE_SIZE = 100;
    const TABLE_DISPLAY_MAX = 2000;
    /** Right surface mapDetailsEveryPageNumber of bars*/
    const DETAIL_PAGE_SIZE = 10;

    const state = {
        source: {
            connectorId: '',
            database: '',
            schema: '',
            tables: [],
            checked: {},
            tableMeta: {
                total: 0,
                cappedTotal: 0,
                truncated: false,
                hasMore: false,
                loading: false,
                typeCounts: {}
            },
            tableSearchKey: '',
            /** CurrentConnectorin SelectionLibraryBackYesNoProvisionSchema List（BygetSchema InterfaceDecision)*/
            schemaEnabled: false
        },
        target: { connectorId: '' },
        mappings: [],
        activeMappingIndex: -1,
        detailPageNum: 1
    };

    let sourceDbSelect = null;
    let sourceSchemaSelect = null;
    let sourceConnectorSelect = null;
    let targetConnectorSelect = null;
    let pickerTargetConnectorSelect = null;
    /** ConnectorType/NameC ConcreteminPageCumulative)*/
    let connectorTypeCache = {};
    let connectorLabelCache = {};
    /** Target ConnectionRemoteConfigure（preferredType Whatever.Sourcechange)*/
    let targetConnectorRemoteExtra = null;
    let pickerTargetConnectorRemoteExtra = null;
    /** Retention of actionSourceYes.ConnectorType */
    const UNSUPPORTED_SOURCE_CONNECTOR_TYPES = [];
    let tableTreeScrollBound = false;
    let tableCbSeq = 0;
    let initializing = false;
    let lastSourceConnectorId = '';
    let detailPanelBound = false;
    let tablePickerModal = null;
    let tablePickerSearchTimer = null;
    /** add=NewLibraryMaps;append=CurrentLibraryMap Append Table*/
    let tablePickerMode = 'add';
    /** ProgrammaticRefreshConnectorIgnore optionsonSelect，AvoidsetData AutoSelect FirstOverwriteMapData */
    let suppressConnectorSelect = false;

    function isReadOnly() {
        return !!page.readOnly;
    }

    function isEditMode() {
        return page.mode === 'edit';
    }

    function rememberConnectorMeta(id, type, label) {
        if (!id) {
            return;
        }
        if (type) {
            connectorTypeCache[id] = String(type).trim();
        }
        if (label) {
            connectorLabelCache[id] = String(label).trim();
        }
    }

    function formatConnectorOptionLabel(name, type) {
        const n = name || '';
        const t = type || '';
        return t ? (n + '(' + t + ')') : n;
    }

    function resolveConnectorMetaFromTask() {
        const task = page.task || {};
        const mappings = task.databaseMappings || [];
        const first = mappings.length ? mappings[0] : null;
        const sourceConnector = task.sourceConnector || {};
        const targetConnector = task.targetConnector || {};
        const sourceId = (first && first.sourceConnectorId) || sourceConnector.id || '';
        const targetId = (first && first.targetConnectorId) || targetConnector.id || '';
        const sourceType = (sourceConnector.config && sourceConnector.config.connectorType) || '';
        const targetType = (targetConnector.config && targetConnector.config.connectorType) || '';
        const sourceLabel = formatConnectorOptionLabel(sourceConnector.name, sourceType) || sourceId;
        const targetLabel = formatConnectorOptionLabel(targetConnector.name, targetType) || targetId;
        if (sourceId) {
            rememberConnectorMeta(sourceId, sourceType, sourceLabel);
        }
        if (targetId) {
            rememberConnectorMeta(targetId, targetType, targetLabel);
        }
        return {
            sourceId: sourceId,
            sourceType: sourceType,
            sourceLabel: sourceLabel,
            targetId: targetId,
            targetType: targetType,
            targetLabel: targetLabel
        };
    }

    function wrapRemoteConnectorOptions(extra) {
        const base = buildRemoteConnectorSelectOptions(extra);
        const originLoad = base.loadOptions;
        base.loadOptions = function (query, onSuccess, onError) {
            originLoad(query, function (result) {
                const rows = result && Array.isArray(result.data) ? result.data : [];
                rows.forEach(function (item) {
                    if (!item || !item.value) {
                        return;
                    }
                    rememberConnectorMeta(
                        item.value,
                        item.data && item.data.connectorType,
                        item.label
                    );
                });
                onSuccess(result);
            }, onError);
        };
        return base;
    }

    function getConnectorTypeBySelect(selectId, connectorId) {
        if (!connectorId) {
            return '';
        }
        const $el = $('#' + selectId);
        if ($el.length && typeof resolveConnectorTypeFromSelect === 'function') {
            const fromSelect = resolveConnectorTypeFromSelect($el, connectorId);
            if (fromSelect) {
                rememberConnectorMeta(connectorId, fromSelect, '');
                return fromSelect;
            }
        }
        return connectorTypeCache[connectorId] || '';
    }

    function isSameConnectorType(sourceConnectorId, targetConnectorId) {
        const sourceType = normalizeConnectorType(getConnectorTypeById(sourceConnectorId));
        const targetType = normalizeConnectorType(getConnectorTypeById(targetConnectorId));
        return !!(sourceType && targetType && sourceType === targetType);
    }

    function syncTargetSelectPreferredType(select, remoteExtra, sourceConnectorId, currentId, onAssigned) {
        if (!select || !remoteExtra) {
            return '';
        }
        const sourceType = normalizeConnectorType(getConnectorTypeById(sourceConnectorId));
        remoteExtra.preferredType = sourceType || '';
        const curType = normalizeConnectorType(getConnectorTypeById(currentId));
        const nextId = (sourceType && curType && sourceType === curType) ? (currentId || '') : '';
        suppressConnectorSelect = true;
        try {
            if (typeof select.setValues === 'function') {
                select.setValues(nextId ? [nextId] : [], true);
            }
            if (typeof select.reloadRemote === 'function') {
                select.reloadRemote('');
            }
        } finally {
            suppressConnectorSelect = false;
        }
        if (typeof onAssigned === 'function') {
            onAssigned(nextId);
        }
        return nextId;
    }

    /**
     * DetailsSector EntitiesTarget ConnectionPress CurrentLibraryMap source/Target ConnectionFilters;
     * In the window.Target ConnectionPress TopSourceFilter. alleges: The two alleges no interference.
     */
    function refreshTargetConnectorOptions() {
        const block = getActiveMappingBlock();

        if (targetConnectorSelect && hasDetailTargetPanel()) {
            const detailSourceId = block ? getMappingSourceConnectorId(block) : state.source.connectorId;
            const detailTargetId = block ? getMappingTargetConnectorId(block) : state.target.connectorId;
            const nextId = syncTargetSelectPreferredType(
                targetConnectorSelect,
                targetConnectorRemoteExtra,
                detailSourceId,
                detailTargetId,
                function (id) {
                    if (block) {
                        block.targetConnectorId = id;
                    }
                    state.target.connectorId = id;
                }
            );
            if (block) {
                state.target.connectorId = block.targetConnectorId || nextId;
            }
        }

        if (pickerTargetConnectorSelect) {
            const pickerTargetId = getPickerTargetConnectorId() || state.target.connectorId;
            const nextId = syncTargetSelectPreferredType(
                pickerTargetConnectorSelect,
                pickerTargetConnectorRemoteExtra,
                state.source.connectorId,
                pickerTargetId,
                null
            );
            if (isPickerModalOpen()) {
                state.target.connectorId = nextId;
            }
        } else if (!block || !hasDetailTargetPanel()) {
            const nextId = syncTargetSelectPreferredType(
                targetConnectorSelect,
                targetConnectorRemoteExtra,
                state.source.connectorId,
                state.target.connectorId,
                null
            );
            state.target.connectorId = nextId;
        }
    }

    function normalizeConnectorType(connectorType) {
        return (connectorType || '').trim().toLowerCase();
    }

    function isUnsupportedSourceConnectorType(connectorType) {
        return UNSUPPORTED_SOURCE_CONNECTOR_TYPES.indexOf(normalizeConnectorType(connectorType)) >= 0;
    }

    function isUnsupportedSourceConnector(connectorId) {
        return isUnsupportedSourceConnectorType(getConnectorTypeById(connectorId));
    }

    function isOracleConnector(connectorId) {
        const type = normalizeConnectorType(getConnectorTypeById(connectorId));
        // Dream /OceanBaseOracle and Oracle Same asSchema(User) To name space,NoneIndependenceLibraryList
        return type === 'oracle' || type === 'dameng' || type === 'oceanbaseoracle';
    }

    function isOracleSourceConnector() {
        return isOracleConnector(state.source.connectorId);
    }

    function isOracleTargetConnector(connectorId) {
        return isOracleConnector(connectorId != null ? connectorId : getActiveTargetConnectorId());
    }

    function getConnectorTypeById(connectorId) {
        if (!connectorId) {
            return '';
        }
        if (connectorTypeCache[connectorId]) {
            return connectorTypeCache[connectorId];
        }
        let type = getConnectorTypeBySelect('pickerTargetConnectorId', connectorId);
        if (!type) {
            type = getConnectorTypeBySelect('targetConnectorId', connectorId);
        }
        if (!type) {
            type = getConnectorTypeBySelect('sourceConnectorId', connectorId);
        }
        return type;
    }

    function getPickerTargetConnectorId() {
        if (pickerTargetConnectorSelect && typeof pickerTargetConnectorSelect.getValues === 'function') {
            const ids = pickerTargetConnectorSelect.getValues();
            if (ids.length) {
                return ids[0];
            }
        }
        return $('#pickerTargetConnectorId').val() || '';
    }

    function getActiveMappingBlock() {
        if (state.activeMappingIndex < 0 || state.activeMappingIndex >= state.mappings.length) {
            return null;
        }
        return state.mappings[state.activeMappingIndex];
    }

    function getMappingSourceConnectorId(block) {
        if (block && block.sourceConnectorId) {
            return block.sourceConnectorId;
        }
        return state.source.connectorId || '';
    }

    function getMappingTargetConnectorId(block) {
        if (block && block.targetConnectorId) {
            return block.targetConnectorId;
        }
        return state.target.connectorId || '';
    }

    function getDetailPanelTargetConnectorId() {
        return getMappingTargetConnectorId(getActiveMappingBlock());
    }

    function getActiveTargetConnectorId() {
        if (isPickerModalOpen()) {
            const pickerId = getPickerTargetConnectorId();
            if (pickerId) {
                return pickerId;
            }
        }
        if (hasDetailTargetPanel()) {
            const detailId = getDetailPanelTargetConnectorId();
            if (detailId) {
                return detailId;
            }
        }
        return state.target.connectorId || '';
    }

    function isSourceNamespaceReady() {
        const srcDb = (state.source.database || '').trim();
        const srcSchema = (state.source.schema || '').trim();
        if (isOracleSourceConnector()) {
            return !!srcSchema;
        }
        if (!srcDb) {
            return false;
        }
        return !state.source.schemaEnabled || !!srcSchema;
    }

    function isPickerModalOpen() {
        return !!tablePickerModal;
    }

    /** In the window:Source database/Schema OrganisationOverwriteTarget database name、Target schema */
    function syncPickerTargetFromSource() {
        const srcDb = (state.source.database || '').trim();
        const srcSchema = (state.source.schema || '').trim();
        const pickerTargetId = getPickerTargetConnectorId() || state.target.connectorId;
        if ($('#pickerTargetDatabase').length && !isOracleTargetConnector(pickerTargetId)) {
            $('#pickerTargetDatabase').val(srcDb);
        }
        if ($('#pickerTargetSchema').length) {
            $('#pickerTargetSchema').val(srcSchema);
        }
    }

    /** SourceLibrary/Schema Fill after ReadyObjective CombinedName space; forced connection in the window,DetailsDefault fill only when empty*/
    function fillTargetFromSource() {
        if (isPickerModalOpen()) {
            syncPickerTargetFromSource();
            return;
        }
        if (!isSourceNamespaceReady()) {
            return;
        }
        const srcDb = (state.source.database || '').trim();
        const srcSchema = (state.source.schema || '').trim();
        if (hasDetailTargetPanel() && !isOracleTargetConnector(getDetailPanelTargetConnectorId())) {
            const block = getActiveMappingBlock();
            const $detailDb = $('#detailTargetDatabase');
            if (!block || !block.targetDatabase) {
                if (!$detailDb.val().trim()) {
                    $detailDb.val(srcDb);
                }
            }
        }
        if (srcSchema) {
            const $detailSchema = $('#detailTargetSchema');
            if ($detailSchema.length && !$detailSchema.val().trim()) {
                $detailSchema.val(srcSchema);
            }
        }
    }

    function hasDetailTargetPanel() {
        return $('#detailTargetDatabase').length > 0;
    }

    function getConnectorLabel(selectId, connectorId) {
        if (!connectorId) {
            return 'No Selection';
        }
        const text = $('#' + selectId + ' option[value="' + connectorId + '"]').text() || '';
        const name = text.replace(/\([^)]*\)\s*$/, '').trim();
        return name || connectorId;
    }

    /** ProceduralizationValueTime does not triggeronSelect，Avoidand syncDetailPanelFromActiveMapping Form Recursive*/
    function setSelectValues(api, values) {
        if (!api || typeof api.setValues !== 'function') {
            return;
        }
        const list = Array.isArray(values) ? values : [values];
        api.setValues(list, true);
    }

    function refreshMappingCardFlows() {
        if (!hasDetailTargetPanel() || !state.mappings.length) {
            return;
        }
        $('#mappingCardList .list-card').each(function () {
            const idx = Number($(this).data('index'));
            const block = state.mappings[idx];
            if (block) {
                const $src = $(this).find('.list-card-line.is-title');
                const $target = $(this).find('.list-card-line.list-card-target');
                if ($src.length) {
                    $src.attr('title', getMappingSourceLineTitle(block));
                }
                if ($target.length) {
                    $target.html(formatMappingCardTargetLineHtml(block));
                    $target.attr('title', getMappingTargetLineTitle(block));
                }
            }
        });
    }

    function updateMappingCount() {
        $('#mappingCount').text(state.mappings.length);
        updateAddMappingButtonState();
    }

    /**
     * Interim programme: only one permitLibraryMapping。
     */
    function updateAddMappingButtonState() {
        const $btn = $('#btnAddDbMapping');
        if (!$btn.length || isReadOnly()) {
            return;
        }
        const limitReached = state.mappings.length >= 1;
        $btn.prop('disabled', limitReached);
        if (limitReached) {
            $btn.attr('title', 'Currently only single library map relationships are supported');
        } else {
            $btn.removeAttr('title');
        }
    }

    function updateWorkspaceEmptyState() {
        const $empty = $('#mappingWorkspaceEmpty');
        if (!$empty.length) {
            return;
        }
        if (!state.mappings.length) {
            $empty.removeClass('hidden');
        } else {
            $empty.addClass('hidden');
        }
    }

    function updateSourceDatabaseFieldVisibility() {
        const hide = isOracleSourceConnector();
        $('#sourceDatabase').closest('.form-item').toggleClass('hidden', hide);
    }

    function toggleSchemaLabelRequired($wrap, required) {
        const $label = $wrap.find('.form-label').first();
        $label.find('.ds-schema-req').remove();
        if (required) {
            $label.append(' <strong class="text-primary ds-schema-req">*</strong>');
        }
    }

    function updateTargetDatabaseFieldVisibility() {
        const pickerOracle = isOracleTargetConnector(getPickerTargetConnectorId() || state.target.connectorId);
        const detailOracle = isOracleTargetConnector(getDetailPanelTargetConnectorId());
        $('#pickerTargetDatabase').closest('.form-item').toggleClass('hidden', pickerOracle);
        $('#detailTargetDatabase').closest('.ds-target-field').toggleClass('hidden', detailOracle);
        toggleSchemaLabelRequired($('#pickerTargetSchemaWrap'), pickerOracle);
        toggleSchemaLabelRequired($('#detailTargetSchemaWrap'), detailOracle);
    }

    function updateTargetSchemaVisibility() {
        const show = !!state.source.schemaEnabled;
        $('#detailTargetSchemaWrap, #pickerTargetSchemaWrap').toggleClass('hidden', !show);
        updateTargetDatabaseFieldVisibility();
    }

    function formatMappingSourceLabel(block) {
        if (!block) {
            return '';
        }
        let label = block.sourceDatabase || '';
        if (block.sourceSchema) {
            label += (label ? '.' : '') + block.sourceSchema;
        }
        return label || '—';
    }

    function getMappingSourceDbName(block) {
        if (!block) {
            return '';
        }
        if (block.sourceDatabase) {
            return block.sourceDatabase;
        }
        if (isOracleConnector(getMappingSourceConnectorId(block))) {
            return block.sourceSchema || '';
        }
        return block.sourceDatabase || block.sourceSchema || '';
    }

    function getMappingTargetDbName(block) {
        if (!block) {
            return '';
        }
        if (block.targetDatabase) {
            return block.targetDatabase;
        }
        if (isOracleTargetConnector(getMappingTargetConnectorId(block))) {
            return block.targetSchema || '';
        }
        return block.targetDatabase || block.targetSchema || '';
    }

    function formatDbWithConnectorType(dbName, connectorId) {
        const name = escapeHtml(dbName || '—');
        const type = getConnectorTypeById(connectorId);
        if (!type) {
            return name;
        }
        return name + '(' + escapeHtml(type) + ')';
    }

    function getConnectorDisplayName(connectorId) {
        if (!connectorId) {
            return '';
        }
        const cachedLabel = connectorLabelCache[connectorId];
        if (cachedLabel) {
            return cachedLabel.replace(/\([^)]*\)\s*$/, '').trim() || connectorId;
        }
        return getConnectorLabel('targetConnectorId', connectorId)
            || getConnectorLabel('sourceConnectorId', connectorId)
            || connectorId;
    }

    function formatMappingCardSourceLineHtml(block) {
        return 'Source &nbsp; &nbsp; &nbsp; &nbsp; library:' + formatDbWithConnectorType(getMappingSourceDbName(block), getMappingSourceConnectorId(block));
    }

    function formatMappingCardTargetLineHtml(block) {
        const connId = getMappingTargetConnectorId(block);
        const connName = getConnectorDisplayName(connId);
        const dbPart = formatDbWithConnectorType(getMappingTargetDbName(block), connId);
        if (!connName || connName === 'No Selection') {
            return 'Target DB: ' + dbPart;
        }
        return 'Target DB: ' + dbPart + ' · ' + escapeHtml(connName);
    }

    function getMappingSourceLineTitle(block) {
        const db = getMappingSourceDbName(block) || '—';
        const type = getConnectorTypeById(getMappingSourceConnectorId(block));
        return type ? ('Source: ' + db + '(' + type + ')') : ('Source: ' + db);
    }

    function getMappingTargetLineTitle(block) {
        const db = getMappingTargetDbName(block) || '—';
        const connId = getMappingTargetConnectorId(block);
        const type = getConnectorTypeById(connId);
        const connName = getConnectorDisplayName(connId);
        let title = type ? ('Target DB: ' + db + '(' + type + ')') : ('Target DB: ' + db);
        if (connName && connName !== 'No Selection') {
            title += ' · ' + connName;
        }
        return title;
    }

    function formatMappingCardBodyHtml(block) {
        return '<div class="list-card-line is-title" title="' + escapeHtml(getMappingSourceLineTitle(block)) + '">'
            + formatMappingCardSourceLineHtml(block) + '</div>'
            + '<div class="list-card-line list-card-target" title="' + escapeHtml(getMappingTargetLineTitle(block)) + '">'
            + formatMappingCardTargetLineHtml(block) + '</div>';
    }

    function formatTargetNamespaceLabel(block) {
        if (!block) {
            return '—';
        }
        if (block.targetDatabase) {
            return block.targetDatabase;
        }
        if (isOracleTargetConnector(getMappingTargetConnectorId(block)) && block.targetSchema) {
            return block.targetSchema;
        }
        return block.targetDatabase || block.targetSchema || '—';
    }

    function refreshPickerTargetDefaults() {
        refreshTargetConnectorOptions();
        const pickerTargetId = getPickerTargetConnectorId() || state.target.connectorId;
        if (pickerTargetId && pickerTargetConnectorSelect) {
            setSelectValues(pickerTargetConnectorSelect, [pickerTargetId]);
        }
        updateTargetSchemaVisibility();
    }

    function onPickerSourceContextChange() {
        if (!isPickerModalOpen()) {
            return;
        }
        updateTargetSchemaVisibility();
        fillTargetFromSource();
    }

    function syncDetailPanelFromActiveMapping() {
        if (!hasDetailTargetPanel()) {
            return;
        }
        const block = state.mappings[state.activeMappingIndex];
        if (!block) {
            return;
        }
        const sourceConnectorId = getMappingSourceConnectorId(block);
        const targetConnectorId = getMappingTargetConnectorId(block);
        state.source.connectorId = sourceConnectorId;
        state.target.connectorId = targetConnectorId;
        if (sourceConnectorId) {
            setSelectValues(sourceConnectorSelect, [sourceConnectorId]);
        }
        refreshTargetConnectorOptions();
        $('#detailTargetDatabase').val(block.targetDatabase || '');
        $('#detailTargetSchema').val(block.targetSchema || '');
        if (targetConnectorId) {
            setSelectValues(targetConnectorSelect, [targetConnectorId]);
        } else {
            setSelectValues(targetConnectorSelect, []);
        }
        updateTargetDatabaseFieldVisibility();
        const srcLabel = formatMappingSourceLabel(block);
        const tgtNs = formatTargetNamespaceLabel(block);
        $('#mappingDetailTitle').html(
            '<i class="fa fa-table"></i>Map Details'
            + (srcLabel ? ' <span class="text-tertiary text-sm">(' + escapeHtml(srcLabel) + ' ➔ ' + escapeHtml(tgtNs) + ')</span>' : '')
        );
    }

    function syncActiveMappingFromDetailPanel() {
        if (!hasDetailTargetPanel()) {
            return;
        }
        const block = state.mappings[state.activeMappingIndex];
        if (!block) {
            return;
        }
        let targetConnectorId = state.target.connectorId;
        if (targetConnectorSelect && typeof targetConnectorSelect.getValues === 'function') {
            const ids = targetConnectorSelect.getValues();
            if (ids.length) {
                targetConnectorId = ids[0];
            }
        }
        targetConnectorId = targetConnectorId || $('#targetConnectorId').val() || '';
        if (!targetConnectorId && block.targetConnectorId) {
            targetConnectorId = block.targetConnectorId;
        }
        block.targetConnectorId = targetConnectorId;
        state.target.connectorId = targetConnectorId;
        if (!isOracleTargetConnector(targetConnectorId)) {
            block.targetDatabase = ($('#detailTargetDatabase').val() || '').trim();
        } else {
            block.targetDatabase = '';
        }
        block.targetSchema = ($('#detailTargetSchema').val() || '').trim();
        syncMappingsJson();
        const $activeCard = $('#mappingCardList .list-card.is-active');
        if ($activeCard.length) {
            $activeCard.find('.list-card-target').html(formatMappingCardTargetLineHtml(block));
        }
        const srcLabel = formatMappingSourceLabel(block);
        const tgtNs = formatTargetNamespaceLabel(block);
        $('#mappingDetailTitle').html(
            '<i class="fa fa-table"></i>Map Details'
            + (srcLabel ? ' <span class="text-tertiary text-sm">(' + escapeHtml(srcLabel) + ' ➔ ' + escapeHtml(tgtNs) + ')</span>' : '')
        );
    }

    function bindDetailPanelEvents() {
        if (!hasDetailTargetPanel() || detailPanelBound) {
            return;
        }
        detailPanelBound = true;
        $('#detailTargetDatabase').on('input change', function () {
            syncActiveMappingFromDetailPanel();
        });
        $('#detailTargetSchema').on('input change', function () {
            syncActiveMappingFromDetailPanel();
        });
        $('#btnBatchTablePrefix').on('click', batchAddTablePrefix);
        $('#btnAddTablesToMapping').on('click', openAppendTablePickerModal);
    }

    function applyTablePrefixToBlock(block, prefix) {
        (block.tableMappings || []).forEach(function (row) {
            row.targetTable = prefix + (row.targetTable || '');
        });
        syncMappingsJson();
        renderMappingDetail(false);
        bootGrowl('Batch updated target table name', 'success');
    }

    function batchAddTablePrefix() {
        if (isReadOnly()) {
            return;
        }
        const block = state.mappings[state.activeMappingIndex];
        if (!block || !(block.tableMappings || []).length) {
            bootGrowl('There is no editable table for the current map', 'warning');
            return;
        }
        const inputId = 'batchTablePrefixInput';
        showConfirm({
            title: 'Batch add target table prefix',
            message: 'Add Introducing prefixes to all target table names of the current map',
            icon: 'info',
            size: 'normal',
            confirmText: 'OK',
            confirmType: 'primary',
            body: '<div class="form-item mb-0">'
                + '<label class="form-label" for="' + inputId + '"\'The Table Name Prefix</label>'
                + '<div class="form-control-area">'
                + '<input type="text" id="' + inputId + '54% of the world\'s population.'
                + '</div></div>',
            onConfirm: function () {
                const $input = $('#' + inputId);
                const prefix = ($input.length ? $input.val() : '') || '';
                if (!String(prefix).trim()) {
                    bootGrowl('Prefix cannot be empty', 'warning');
                    return;
                }
                applyTablePrefixToBlock(block, prefix);
            }
        });
        setTimeout(function () {
            const el = document.getElementById(inputId);
            if (el) {
                el.focus();
                el.select();
            }
        }, 0);
    }

    function onDBChange(connectorId, schemaSelect, dbName) {
        if (!connectorId) {
            state.source.schemaEnabled = false;
            schemaSelect.setData([]);
            updateTargetSchemaVisibility();
            return;
        }
        doGetter('/connector/getSchema', { id: connectorId, database: dbName }, function (response) {
            if (response.success && response.data) {
                const array = (response.data || []).map(function (schema) {
                    return { label: schema, value: schema, disabled: false };
                });
                state.source.schemaEnabled = array.length > 0;
                schemaSelect.setData(array);
            } else {
                state.source.schemaEnabled = false;
                schemaSelect.setData([]);
                bootGrowl('Can not open message ' + (response.message || ''), 'danger');
            }
            updateTargetSchemaVisibility();
        });
    }

    function resetSourcePickerContext() {
        state.source.database = '';
        state.source.schema = '';
        state.source.schemaEnabled = false;
        state.source.checked = {};
        resetSourceTableList();
        renderSourceTableTreeEmpty('No tables yet. Select connector, database and schema');
        if (sourceDbSelect) {
            sourceDbSelect.setData([]);
        }
        if (sourceSchemaSelect) {
            sourceSchemaSelect.setData([]);
        }
    }

    /** OpenAddWhen mapping the bullet windowResetForm, retain loadedLibraryList，Disable last selection*/
    function resetTablePickerForm() {
        tablePickerMode = 'add';
        state.source.database = '';
        state.source.schema = '';
        state.source.schemaEnabled = false;
        state.source.checked = {};
        state.source.tableSearchKey = '';
        resetSourceTableList();
        renderSourceTableTreeEmpty('No tables yet. Select connector, database and schema');
        $('#sourceTableSearch').val('');
        $('#pickerTargetDatabase').val('');
        $('#pickerTargetSchema').val('');
        if (sourceDbSelect) {
            setSelectValues(sourceDbSelect, []);
        }
        if (sourceSchemaSelect) {
            sourceSchemaSelect.setData([]);
            setSelectValues(sourceSchemaSelect, []);
        }
    }

    function isAppendTablePickerMode() {
        return tablePickerMode === 'append';
    }

    /** CurrentLibraryMap alreadyAddYes.Source Table NamePool (additional mode to re-display)*/
    function getExcludedSourceTableSet() {
        const excluded = {};
        if (!isAppendTablePickerMode()) {
            return excluded;
        }
        const block = getActiveMappingBlock();
        if (!block) {
            return excluded;
        }
        (block.tableMappings || []).forEach(function (row) {
            if (row && row.sourceTable) {
                excluded[row.sourceTable] = true;
            }
        });
        return excluded;
    }

    function getExcludedSourceTableNames() {
        return Object.keys(getExcludedSourceTableSet());
    }

    function hasSelectOptions(selectApi) {
        if (!selectApi || typeof selectApi.getData !== 'function') {
            return false;
        }
        const data = selectApi.getData();
        return Array.isArray(data) && data.length > 0;
    }

    /** EditPageWait for the scene.SourceConnectorGrantedValueBut not gone.onSelect # When you're pulling in #Library/Schema Options*/
    function ensureSourcePickerOptionsLoaded() {
        const connectorId = state.source.connectorId;
        if (!connectorId) {
            return;
        }
        if (isOracleSourceConnector()) {
            if (!hasSelectOptions(sourceSchemaSelect)) {
                onSourceConnectorChange(connectorId);
            }
            return;
        }
        if (!hasSelectOptions(sourceDbSelect)) {
            onSourceConnectorChange(connectorId);
        }
    }

    function onSourceConnectorChange(connectorId) {
        if (!connectorId) {
            resetSourcePickerContext();
            state.source.connectorId = '';
            clearMappings();
            lastSourceConnectorId = '';
            refreshTargetConnectorOptions();
            return;
        }
        if (!initializing && isUnsupportedSourceConnector(connectorId)) {
            const rollbackId = lastSourceConnectorId && !isUnsupportedSourceConnector(lastSourceConnectorId)
                ? lastSourceConnectorId
                : '';
            setSelectValues(sourceConnectorSelect, rollbackId ? [rollbackId] : [], true);
            state.source.connectorId = rollbackId;
            refreshTargetConnectorOptions();
            return;
        }
        const sourceChanged = lastSourceConnectorId && lastSourceConnectorId !== connectorId;
        if (sourceChanged && !initializing) {
            resetSourcePickerContext();
        }
        lastSourceConnectorId = connectorId;
        state.source.connectorId = connectorId;
        refreshTargetConnectorOptions();
        updateSourceDatabaseFieldVisibility();
        if (isOracleSourceConnector()) {
            state.source.database = '';
            if (sourceDbSelect) {
                sourceDbSelect.setData([]);
            }
            if (sourceSchemaSelect) {
                onDBChange(connectorId, sourceSchemaSelect, '');
            }
            return;
        }
        doGetter('/connector/getDatabase', { id: connectorId }, function (response) {
            if (response.success && response.data) {
                const db = (response.data || []).map(function (dbName) {
                    return { label: dbName, value: dbName, disabled: false };
                });
                if (sourceDbSelect) {
                    sourceDbSelect.setData(db);
                }
            } else {
                bootGrowl('Could not close temporary folder: %s ' + (response.message || ''), 'danger');
            }
        });
    }

    function resetSourceTableList() {
        const s = state.source;
        s.tables = [];
        s.tableMeta = {
            total: 0,
            cappedTotal: 0,
            truncated: false,
            hasMore: false,
            loading: false,
            typeCounts: {},
            fetchedCount: 0
        };
    }

    function loadSourceTables(searchKey, reset) {
        const s = state.source;
        if (!s.connectorId) {
            resetSourceTableList();
            renderSourceTableTreeEmpty('No tables yet. Select connector, database and schema');
            return;
        }
        const key = searchKey != null ? searchKey : s.tableSearchKey;
        if (reset || key !== s.tableSearchKey) {
            s.tableSearchKey = key;
            resetSourceTableList();
        }
        if (s.tableMeta.loading) {
            return;
        }
        if (!reset && (!s.tableMeta.hasMore || s.tableMeta.fetchedCount >= TABLE_DISPLAY_MAX)) {
            return;
        }
        fetchSourceTablePage(reset);
    }

    function refreshTableListDisplayMeta(s, serverHasMore) {
        const fetched = s.tableMeta.fetchedCount || 0;
        s.tableMeta.cappedTotal = Math.min(s.tableMeta.total, TABLE_DISPLAY_MAX);
        s.tableMeta.truncated = s.tableMeta.total > TABLE_DISPLAY_MAX;
        s.tableMeta.hasMore = !!serverHasMore && fetched < s.tableMeta.cappedTotal;
    }

    function fetchSourceTablePage(reset, onComplete) {
        const s = state.source;
        const offset = reset ? 0 : (s.tableMeta.fetchedCount || 0);
        if (offset >= TABLE_DISPLAY_MAX) {
            refreshTableListDisplayMeta(s, false);
            updateTableTreeFooter(false);
            if (typeof onComplete === 'function') {
                onComplete();
            }
            return;
        }
        const limit = Math.min(TABLE_PAGE_SIZE, TABLE_DISPLAY_MAX - offset);
        s.tableMeta.loading = true;
        updateTableTreeFooter(true);
        const request = {
            connectorId: s.connectorId,
            database: s.database || '',
            schema: s.schema || '',
            searchKey: s.tableSearchKey || '',
            offset: offset,
            limit: limit
        };
        if (isAppendTablePickerMode()) {
            request.excludeTablesJson = JSON.stringify(getExcludedSourceTableNames());
        }
        doPoster('/database-sync/previewTables', request, function (res) {
            s.tableMeta.loading = false;
            if (!res.success) {
                updateTableTreeFooter(false);
                bootGrowl(res.message || 'Loading table failed', 'danger');
                if (typeof onComplete === 'function') {
                    onComplete();
                }
                return;
            }
            const payload = res.data || {};
            // Service has been pressedexcludeTablesJson ExcludedAdd Table，Do not double filter the frontendPage
            const pageItems = payload.data || [];
            s.tableMeta.fetchedCount = offset + pageItems.length;
            s.tableMeta.total = payload.total || 0;
            s.tableMeta.typeCounts = payload.typeCounts || {};
            if (!s.tableMeta.total) {
                refreshTableListDisplayMeta(s, false);
                renderSourceTableTreeEmpty(
                    isAppendTablePickerMode()
                        ? 'The OVERVIEW OF THE CURRENT COURSE'
                        : 'For the time being, check the library/ Schema or adjust the search conditions'
                );
            } else if (reset) {
                s.tables = pageItems.slice();
                refreshTableListDisplayMeta(s, payload.hasMore);
                renderSourceTableTreeShell();
                appendSourceTableRows(pageItems);
                syncFolderCheckboxState($('#sourceTableTree'));
            } else {
                s.tables = s.tables.concat(pageItems);
                refreshTableListDisplayMeta(s, payload.hasMore);
                if (pageItems.length) {
                    if (!$('#sourceTableTree .picker-tree-body').length) {
                        renderSourceTableTreeShell();
                    }
                    appendSourceTableRows(pageItems);
                    syncFolderCheckboxState($('#sourceTableTree'));
                }
            }
            updateTableTreeFooter(false);
            if (typeof onComplete === 'function') {
                onComplete();
            }
        });
    }

    function ensureAllSourceTablesLoaded(callback) {
        const s = state.source;
        if (!s.tableMeta.hasMore) {
            callback();
            return;
        }
        if (s.tableMeta.loading) {
            bootGrowl('Loading table list, please wait', 'warning');
            return;
        }
        fetchSourceTablePage(false, function () {
            ensureAllSourceTablesLoaded(callback);
        });
    }

    function getLoadedTableNames() {
        return (state.source.tables || []).map(function (t) {
            return t.name || '';
        }).filter(Boolean);
    }

    function setSourceTableChecked(names, checked) {
        names.forEach(function (name) {
            state.source.checked[name] = checked;
        });
    }


    function clearAllSourceTables() {
        state.source.checked = {};
        $('#sourceTableTree .db-sync-table-cb').each(function () {
            setCheckboxGroupChecked($(this), false, true);
        });
        $('#sourceTableTree .db-sync-folder-cb').each(function () {
            setCheckboxGroupChecked($(this), false, true);
            setCheckboxGroupIndeterminate($(this), false);
        });
        updateTableTreeFooter(false);
    }

    function getSelectedTableCount() {
        syncCheckedFromTableTreeDom();
        let count = 0;
        Object.keys(state.source.checked).forEach(function (name) {
            if (state.source.checked[name]) {
                count++;
            }
        });
        return count;
    }

    const TABLE_TREE_CHECKBOX_OPTIONS = {
        size: 'sm',
        layout: 'inline'
    };

    function nextTableCheckboxId(prefix) {
        tableCbSeq += 1;
        return prefix + tableCbSeq;
    }

    function initTableTreeCheckboxStyle($root) {
        ($root || $('#sourceTableTree')).find('.db-sync-table-cb, .db-sync-folder-cb').each(function () {
            const $cb = $(this);
            if ($cb.data('checkboxGroupInitialized')) {
                return;
            }
            const cbId = $cb.attr('id');
            $cb.checkboxGroup(TABLE_TREE_CHECKBOX_OPTIONS);
            // The initialization of the individual will start with the package.checkbox-group，Originallabel It's no longer close.input，Manual removal to avoid duplication of files
            if (cbId) {
                $('label[for="' + cbId + '"]').each(function () {
                    const $label = $(this);
                    if (!$label.closest('.checkbox-group-item').length) {
                        $label.remove();
                    }
                });
            }
        });
    }

    function setCheckboxGroupChecked($cb, checked, silent) {
        $cb.prop('indeterminate', false);
        const $item = $cb.closest('.checkbox-group-item');
        if ($item.length) {
            $item.removeClass('indeterminate');
        }
        const api = $cb.data('checkboxGroup');
        if (!silent && api && typeof api.setValue === 'function') {
            api.setValue(checked);
        } else {
            $cb.prop('checked', checked);
            if ($item.length) {
                $item.toggleClass('active', !!checked);
            }
        }
    }

    function setCheckboxGroupIndeterminate($cb, indeterminate) {
        $cb.prop('indeterminate', !!indeterminate);
        const $item = $cb.closest('.checkbox-group-item');
        if (!$item.length) {
            return;
        }
        if (indeterminate) {
            $item.addClass('indeterminate').removeClass('active');
        } else {
            $item.removeClass('indeterminate');
        }
    }

    function renderSourceTableTreeEmpty(message) {
        $('#sourceTableTree').html('<div class="text-xs text-tertiary p-2">' + escapeHtml(message) + '</div>');
    }

    function getTableFolderDefs() {
        return [
            { key: 'TABLE', label: 'Tables', icon: 'fa-table' },
            { key: 'VIEW', label: 'Views', icon: 'fa-eye' },
            { key: 'OTHER', label: 'Other', icon: 'fa-folder-o' }
        ];
    }

    function normalizeTableType(type) {
        const upper = (type || 'TABLE').toUpperCase();
        if (upper === 'VIEW') {
            return 'VIEW';
        }
        if (upper === 'TABLE') {
            return 'TABLE';
        }
        return 'OTHER';
    }

    function getFolderCount(folderKey) {
        const counts = state.source.tableMeta.typeCounts || {};
        if (folderKey === 'TABLE') {
            return counts.TABLE || counts.table || 0;
        }
        if (folderKey === 'VIEW') {
            return counts.VIEW || counts.view || 0;
        }
        return counts.OTHER || counts.other || 0;
    }

    function renderSourceTableTreeShell() {
        const $box = $('#sourceTableTree');
        const meta = state.source.tableMeta;
        if (!meta.total) {
            renderSourceTableTreeEmpty('For the time being, check the library/ Schema or adjust the search conditions');
            return;
        }
        const folders = getTableFolderDefs();
        tableCbSeq = 0;
        let html = '<div class="picker-tree-body">';
        folders.forEach(function (folder) {
            const count = getFolderCount(folder.key);
            if (!count) {
                return;
            }
            const loadedInFolder = (state.source.tables || []).filter(function (t) {
                return normalizeTableType(t.type) === folder.key;
            });
            const folderChecked = loadedInFolder.length > 0 && loadedInFolder.every(function (t) {
                return !!state.source.checked[t.name];
            });
            const folderIndeterminate = !folderChecked && loadedInFolder.some(function (t) {
                return !!state.source.checked[t.name];
            });
            const folderCbId = nextTableCheckboxId('dbSyncFolder_');
            html += '<div class="picker-tree-node" data-folder-key="' + folder.key + '">'
                + '<div class="picker-tree-folder picker-tree-item picker-tree-folder-label" data-folder="TABLE">'
                + '<input type="checkbox" class="db-sync-folder-cb" id="' + folderCbId + '" data-folder-key="' + folder.key + '"'
                + (folderChecked ? ' checked' : '')
                + (folderIndeterminate ? ' data-indeterminate="1"' : '')
                + '>'
                + '<label for="' + folderCbId + '" class="picker-tree-folder-cb-label"></label>'
                + '<i class="fa fa-caret-down picker-tree-folder-caret"></i>'
                + '<span class="picker-tree-folder-title">' + folder.label + ' (' + count + ')</span>'
                + '</div>'
                + '<div class="picker-tree-children" data-folder-key="' + folder.key + '"></div>'
                + '</div>';
        });
        html += '</div><div class="picker-scroll-footer" id="sourceTableTreeFooter"></div>';
        $box.html(html);
        initTableTreeCheckboxStyle($box);

        $box.find('.db-sync-folder-cb[data-indeterminate="1"]').each(function () {
            setCheckboxGroupIndeterminate($(this), true);
        });

        bindTableTreeScroll();
        updateTableTreeFooter(false);
    }

    function buildTableRowHtml(t) {
        const name = t.name || '';
        const ck = state.source.checked[name] ? ' checked' : '';
        const safeName = String(name).replace(/"/g, '&quot;');
        const cbId = nextTableCheckboxId('dbSyncTable_');
        return '<div class="picker-tree-item">'
            + '<input type="checkbox" class="db-sync-table-cb" id="' + cbId + '" data-name="' + safeName + '" value="' + safeName + '"' + ck + '>'
            + '<label for="' + cbId + '">' + escapeHtml(name) + '</label>'
            + '</div>';
    }

    function appendSourceTableRows(items) {
        if (!items || !items.length) {
            return;
        }
        const grouped = { TABLE: [], VIEW: [], OTHER: [] };
        items.forEach(function (t) {
            grouped[normalizeTableType(t.type)].push(t);
        });
        Object.keys(grouped).forEach(function (folderKey) {
            const list = grouped[folderKey];
            if (!list.length) {
                return;
            }
            const $children = $('#sourceTableTree .picker-tree-children[data-folder-key="' + folderKey + '"]');
            if (!$children.length) {
                return;
            }
            let html = '';
            list.forEach(function (t) {
                html += buildTableRowHtml(t);
            });
            $children.append(html);
            initTableTreeCheckboxStyle($children);
        });
    }

    function updateTableTreeFooter(loading) {
        const $footer = $('#sourceTableTreeFooter');
        if (!$footer.length) {
            return;
        }
        const meta = state.source.tableMeta;
        if (loading) {
            $footer.addClass('is-loading').html('<i class="fa fa-spinner fa-spin"></i>Loading...');
            return;
        }
        $footer.removeClass('is-loading');
        if (!meta.total) {
            $footer.html('');
            return;
        }
        const selected = getSelectedTableCount();
        const total = meta.cappedTotal || meta.total;
        let html = '<span class="picker-scroll-footer-count">Selected ' + selected + ' / Total' + total + ' Watch</span>';
        if (meta.hasMore) {
            html += '<span class="picker-scroll-footer-hint">Scroll More Down</span>';
        } else if (meta.truncated) {
            html += '<span class="picker-scroll-footer-hint">Total ' + meta.total + ' Chang, show up ' + TABLE_DISPLAY_MAX + ' Chang!</span>';
        }
        $footer.html(html);
    }

    function bindTableTreeScroll() {
        if (tableTreeScrollBound) {
            return;
        }
        tableTreeScrollBound = true;
        $('#sourceTableTree').on('scroll.dbSyncTables', function () {
            const el = this;
            const nearBottom = el.scrollTop + el.clientHeight >= el.scrollHeight - 24;
            if (nearBottom) {
                loadSourceTables(null, false);
            }
        });
    }

    function bindTableTreeEvents() {
        const $box = $('#sourceTableTree');

        $box.off('click.pickerFolder').on('click.pickerFolder', '.picker-tree-folder', function (e) {
            if ($(e.target).closest('.checkbox-group-item, .checkbox-group').length) {
                return;
            }
            const $icon = $(this).find('.fa-caret-down, .fa-caret-right').first();
            $(this).next('.picker-tree-children').toggle();
            $icon.toggleClass('fa-caret-down fa-caret-right');
        });

        $box.off('change.tableCb').on('change.tableCb', '.db-sync-table-cb', function () {
            const name = $(this).data('name');
            state.source.checked[name] = this.checked;
            syncFolderCheckboxState($box);
            updateTableTreeFooter(false);
        });

        $box.off('change.folderCb').on('change.folderCb', '.db-sync-folder-cb', function () {
            const folderKey = $(this).data('folder-key');
            const checked = this.checked;
            const list = (state.source.tables || []).filter(function (t) {
                return normalizeTableType(t.type) === folderKey;
            });
            list.forEach(function (t) {
                const name = t.name || '';
                state.source.checked[name] = checked;
            });
            const $node = $(this).closest('.picker-tree-node');
            $node.find('.db-sync-table-cb').each(function () {
                setCheckboxGroupChecked($(this), checked, true);
            });
            updateTableTreeFooter(false);
        });
    }

    function syncFolderCheckboxState($box) {
        $box.find('.picker-tree-node').each(function () {
            const $node = $(this);
            const $folderCb = $node.children('.picker-tree-folder').find('.db-sync-folder-cb').first();
            const $tableCbs = $node.children('.picker-tree-children').find('.db-sync-table-cb');
            if (!$folderCb.length || !$tableCbs.length) {
                return;
            }
            const total = $tableCbs.length;
            const checkedCount = $tableCbs.filter(':checked').length;
            if (checkedCount === total) {
                setCheckboxGroupChecked($folderCb, true, true);
            } else if (checkedCount === 0) {
                setCheckboxGroupChecked($folderCb, false, true);
            } else {
                setCheckboxGroupIndeterminate($folderCb, true);
            }
        });
    }

    /**
     * Put the watch treeDOM Check inStatusWrite backstate（Update only the currently rendered table line, noOverwriteUnrendered).
     * When you open the window againUI Still checked butstate AlreadyClearCause validationFailedThe problem.
     */
    function syncCheckedFromTableTreeDom() {
        $('#sourceTableTree .db-sync-table-cb').each(function () {
            const name = $(this).data('name');
            if (!name) {
                return;
            }
            if (this.checked) {
                state.source.checked[name] = true;
            } else {
                delete state.source.checked[name];
            }
        });
    }

    function getCheckedSourceTableNames() {
        syncCheckedFromTableTreeDom();
        const names = [];
        Object.keys(state.source.checked).forEach(function (name) {
            if (state.source.checked[name]) {
                names.push(name);
            }
        });
        return names;
    }

    /**
     * Press CurrentListOrder isLibraryMap, MapGenerateContinuousOrderNumber (from) 1 I'm not sure what I'm talking about.SaveTimeandThe back end is the same.
     */
    function reindexMappings() {
        state.mappings.forEach(function (block, dbIdx) {
            block.index = dbIdx + 1;
            (block.tableMappings || []).forEach(function (row, tblIdx) {
                row.index = tblIdx + 1;
            });
        });
    }

    function syncMappingsJson() {
        reindexMappings();
        $('#databaseMappingsJson').val(JSON.stringify(state.mappings));
    }

    /**
     * The same.Source databaseYeah.ConfigureMultipleTarget database；Only whenSourceandObjective CombinedMerge the map only when the end name space is fully consistent.
     */
    function findMappingIndex(sourceDatabase, sourceSchema, targetDatabase, targetSchema,
                              sourceConnectorId, targetConnectorId) {
        const srcSchema = sourceSchema || '';
        const tgtDb = targetDatabase || '';
        const tgtSchema = targetSchema || '';
        const srcConn = sourceConnectorId || '';
        const tgtConn = targetConnectorId || '';
        return state.mappings.findIndex(function (m) {
            return m.sourceDatabase === sourceDatabase
                && (m.sourceSchema || '') === srcSchema
                && (m.targetDatabase || '') === tgtDb
                && (m.targetSchema || '') === tgtSchema
                && getMappingSourceConnectorId(m) === srcConn
                && getMappingTargetConnectorId(m) === tgtConn;
        });
    }

    function buildPickerFormField(labelHtml, controlHtml, options) {
        options = options || {};
        let wrapClass = 'form-item';
        if (options.wrapClass) {
            wrapClass += ' ' + options.wrapClass;
        }
        const wrapId = options.wrapId ? ' id="' + options.wrapId + '"' : '';
        const controlBlock = options.controlType === 'select'
            ? controlHtml
            : '<div class="form-control-area">' + controlHtml + '</div>';
        return '<div class="' + wrapClass + '"' + wrapId + '>'
            + '<label class="form-label">' + labelHtml + '</label>'
            + controlBlock
            + '</div>';
    }

    function buildTablePickerBodyHtml() {
        const ro = isReadOnly();
        const appendMode = isAppendTablePickerMode();
        const selectDisabled = (ro || appendMode) ? ' disabled' : '';
        const inputReadonly = (ro || appendMode) ? ' readonly' : '';
        const btnDisabled = ro ? ' disabled' : '';
        return '<div class="dbsyncer-select-panel table-picker-body">'
            + '<div class="mb-4">'
            + '<h5 class="text-sm font-medium mb-3"><span class="text-primary">1.</span>Select Source</h5>'
            + (appendMode
                ? '<p class="text-warning text-sm mb-3"><i class="fa fa-info-circle"></i>The database has been fixed to the current library map and the added table is not shown</p>'
                : '')
            + '<div class="grid grid-cols-2">'
            + buildPickerFormField(
                'Library',
                '<select id="sourceDatabase" class="form-control"' + selectDisabled + '></select>',
                { controlType: 'select' }
            )
            + buildPickerFormField(
                'Schema',
                '<select id="sourceSchema" class="form-control"' + selectDisabled + '></select>',
                { controlType: 'select' }
            )
            + '</div></div>'
            + '<div class="mb-4">'
            + '<h5 class="text-sm font-medium mb-3"><span class="text-primary">2.</span>Specify the target library</h5>'
            + '<p class="text-warning text-sm mb-3">'
            + '<i class="fa fa-info-circle"></i>Isomer type not supported for the time being (under development)'
            + '</p>'
            + '<div class="border border-dashed rounded-lg p-4 bg-secondary">'
            + '<div class="grid grid-cols-2">'
            + buildPickerFormField(
                'Target Connection<strong class="text-primary">*</strong>',
                '<select id="pickerTargetConnectorId" class="form-control"' + selectDisabled + '></select>',
                { controlType: 'select' }
            )
            + buildPickerFormField(
                'Target database name',
                '== sync, corrected by elderman =='
                + inputReadonly + '/>'
            )
            + buildPickerFormField(
                'Target schema',
                '== sync, corrected by elderman =='
                + inputReadonly + '/>',
                { wrapId: 'pickerTargetSchemaWrap' }
            )
            + '</div></div></div>'
            + '<div>'
            + '<h5 class="text-sm font-medium mb-3"><span class="text-primary">3.</span>Check physical tables that require synchronization</h5>'
            + '<div class="flex items-center justify-between gap-2 mb-2">'
            + '<div class="search-wrapper flex-1"><i class="fa fa-search search-icon"></i>'
            + '"Input id" is "sourcetableSearch"'
            + (ro ? ' readonly' : '') + '/></div>'
            + '<div class="flex items-center gap-2 flex-shrink-0">'
            + '<button type="button" id="btnClearAllTables" class="btn btn-outline btn-sm"' + btnDisabled + '> Clear</button>'
            + '</div></div>'
            + '<div id="sourceTableTree" class="picker-scroll picker-tree-checkbox-group"></div>'
            + '</div></div>';
    }

    function initTablePickerControls() {
        tableTreeScrollBound = false;

        sourceSchemaSelect = $('#sourceSchema').dbSelect({
            type: 'single',
            disabled: isReadOnly() || isAppendTablePickerMode(),
            onSelect: function (selected) {
                if (isAppendTablePickerMode()) {
                    return;
                }
                state.source.schema = selected.length ? selected[0] : '';
                loadSourceTables($('#sourceTableSearch').val().trim(), true);
                fillTargetFromSource();
                onPickerSourceContextChange();
            }
        });

        sourceDbSelect = $('#sourceDatabase').dbSelect({
            type: 'single',
            disabled: isReadOnly() || isAppendTablePickerMode(),
            onSelect: function (selected) {
                if (isAppendTablePickerMode()) {
                    return;
                }
                state.source.database = selected.length ? selected[0] : '';
                state.source.schema = '';
                if (sourceSchemaSelect && typeof sourceSchemaSelect.setValues === 'function') {
                    sourceSchemaSelect.setValues([], true);
                }
                if (state.source.connectorId) {
                    onDBChange(state.source.connectorId, sourceSchemaSelect, state.source.database);
                }
                loadSourceTables(null, true);
                fillTargetFromSource();
                onPickerSourceContextChange();
            }
        });

        pickerTargetConnectorRemoteExtra = {
            relationOnly: true,
            role: 'target',
            preferredType: normalizeConnectorType(getConnectorTypeById(state.source.connectorId)),
            disabled: isReadOnly() || isAppendTablePickerMode(),
            defaultValue: state.target.connectorId || '',
            defaultLabel: connectorLabelCache[state.target.connectorId] || state.target.connectorId || '',
            defaultType: getConnectorTypeById(state.target.connectorId),
            onSelect: function (ids) {
                if (suppressConnectorSelect || isAppendTablePickerMode()) {
                    return;
                }
                const id = ids.length ? ids[0] : '';
                if (id) {
                    rememberConnectorMeta(id, getConnectorTypeBySelect('pickerTargetConnectorId', id), '');
                }
                state.target.connectorId = id;
                updateTargetSchemaVisibility();
                updateTargetDatabaseFieldVisibility();
            }
        };
        pickerTargetConnectorSelect = $('#pickerTargetConnectorId').dbSelect(
            wrapRemoteConnectorOptions(pickerTargetConnectorRemoteExtra)
        );

        refreshTargetConnectorOptions();

        $('#sourceTableSearch').off('input.dbSyncPicker').on('input.dbSyncPicker', function () {
            clearTimeout(tablePickerSearchTimer);
            const key = $(this).val().trim();
            tablePickerSearchTimer = setTimeout(function () { loadSourceTables(key, true); }, 300);
        });

        $('#btnClearAllTables').off('click.dbSyncPicker').on('click.dbSyncPicker', clearAllSourceTables);

        bindTableTreeScroll();
        bindTableTreeEvents();
        updateSourceDatabaseFieldVisibility();
        updateTargetSchemaVisibility();
    }

    function destroyTablePickerControls() {
        clearTimeout(tablePickerSearchTimer);
        tablePickerSearchTimer = null;
        tableTreeScrollBound = false;
        sourceDbSelect = null;
        sourceSchemaSelect = null;
        pickerTargetConnectorSelect = null;
    }

    function cleanupTablePickerState() {
        resetTablePickerForm();
        destroyTablePickerControls();
        tablePickerModal = null;
    }

    function openTablePickerModal() {
        if (isReadOnly()) {
            return;
        }
        syncActiveMappingFromDetailPanel();
        if (!state.source.connectorId) {
            bootGrowl('Please select a source connector first', 'warning');
            return;
        }
        closeTablePickerModal();
        tablePickerMode = 'add';

        tablePickerModal = showConfirm({
            title: 'Tools',
            size: 'max',
            position: 'center',
            body: buildTablePickerBodyHtml(),
            confirmText: 'Confirm Add',
            cancelText: 'Cancel',
            closeOnConfirm: false,
            onConfirm: function () {
                if (addDatabaseMapping()) {
                    closeTablePickerModal();
                }
            },
            onCancel: function () {
                cleanupTablePickerState();
            }
        });

        initTablePickerControls();
        ensureSourcePickerOptionsLoaded();
        refreshPickerTargetDefaults();
        renderSourceTableTreeEmpty('No tables yet. Select connector, database and schema');
    }

    /**
     * For the currentLibraryMap ContinueAdd Table：Source/Objective CombinedNamespace fixed, alreadyAddSource tableDon't show.
     */
    function openAppendTablePickerModal() {
        if (isReadOnly()) {
            return;
        }
        syncActiveMappingFromDetailPanel();
        const block = getActiveMappingBlock();
        if (!block) {
            bootGrowl('Please select the left library map first', 'warning');
            return;
        }
        const sourceConnectorId = getMappingSourceConnectorId(block) || state.source.connectorId;
        if (!sourceConnectorId) {
            bootGrowl('Please select a source connector first', 'warning');
            return;
        }
        closeTablePickerModal();
        tablePickerMode = 'append';
        // Write a fixed namespace first, avoidLibraryList setData Automatically select the first trigger trigger error tab
        state.source.connectorId = sourceConnectorId;
        state.source.database = block.sourceDatabase || '';
        state.source.schema = (block.sourceSchema || '').trim();
        state.source.checked = {};
        state.source.tableSearchKey = '';
        resetSourceTableList();

        tablePickerModal = showConfirm({
            title: 'Continue adding table (added table does not appear)',
            size: 'max',
            position: 'center',
            body: buildTablePickerBodyHtml(),
            confirmText: 'Confirm Add',
            cancelText: 'Cancel',
            closeOnConfirm: false,
            onConfirm: function () {
                if (addDatabaseMapping()) {
                    closeTablePickerModal();
                }
            },
            onCancel: function () {
                cleanupTablePickerState();
            }
        });

        initTablePickerControls();
        applyAppendPickerContext(block);
    }

    function applyAppendPickerContext(block) {
        const targetConnectorId = getMappingTargetConnectorId(block) || state.target.connectorId || '';
        state.target.connectorId = targetConnectorId;
        refreshTargetConnectorOptions();
        if (targetConnectorId && pickerTargetConnectorSelect) {
            setSelectValues(pickerTargetConnectorSelect, [targetConnectorId]);
        }
        $('#pickerTargetDatabase').val(block.targetDatabase || '').prop('readonly', true);
        $('#pickerTargetSchema').val(block.targetSchema || '').prop('readonly', true);
        updateTargetSchemaVisibility();
        updateTargetDatabaseFieldVisibility();
        renderSourceTableTreeEmpty('Loading addable tables...');
        loadAppendSourceOptionsThenTables(block);
    }

    /**
     * Additional mode dedicated: fixedSource database(b) Draw immediately thereafter;Library/Schema Draw Coverage only, without waiting to avoid gaps.
     */
    function loadAppendSourceOptionsThenTables(block) {
        const connectorId = state.source.connectorId;
        const sourceDb = block.sourceDatabase || '';
        const sourceSchema = (block.sourceSchema || '').trim();
        if (!connectorId) {
            renderSourceTableTreeEmpty('Source connection cannot be empty');
            return;
        }
        // Key: Map fixed firstValueDraw 194.Result
        state.source.database = isOracleSourceConnector() ? '' : sourceDb;
        state.source.schema = sourceSchema;
        loadSourceTables(null, true);

        if (isOracleSourceConnector()) {
            doGetter('/connector/getSchema', { id: connectorId, database: '' }, function (response) {
                if (!isPickerModalOpen() || !isAppendTablePickerMode()) {
                    return;
                }
                const array = (response.success && response.data)
                    ? (response.data || []).map(function (schema) {
                        return { label: schema, value: schema, disabled: false };
                    })
                    : [];
                state.source.schemaEnabled = array.length > 0;
                if (sourceSchemaSelect) {
                    sourceSchemaSelect.setData(array);
                    setSelectValues(sourceSchemaSelect, sourceSchema ? [sourceSchema] : []);
                }
                state.source.database = '';
                state.source.schema = sourceSchema;
                updateTargetSchemaVisibility();
            });
            return;
        }
        doGetter('/connector/getDatabase', { id: connectorId }, function (response) {
            if (!isPickerModalOpen() || !isAppendTablePickerMode()) {
                return;
            }
            if (!response.success) {
                bootGrowl('Could not close temporary folder: %s ' + (response.message || ''), 'danger');
                return;
            }
            const dbOptions = (response.data || []).map(function (dbName) {
                return { label: dbName, value: dbName, disabled: false };
            });
            if (sourceDbSelect) {
                sourceDbSelect.setData(dbOptions);
                setSelectValues(sourceDbSelect, sourceDb ? [sourceDb] : []);
            }
            // Pull back and fill in again - preventsetData Rewritestate
            state.source.database = sourceDb;
            state.source.schema = sourceSchema;
            if (!sourceDb) {
                return;
            }
            doGetter('/connector/getSchema', { id: connectorId, database: sourceDb }, function (schemaRes) {
                if (!isPickerModalOpen() || !isAppendTablePickerMode()) {
                    return;
                }
                if (schemaRes.success && schemaRes.data) {
                    const array = (schemaRes.data || []).map(function (schema) {
                        return { label: schema, value: schema, disabled: false };
                    });
                    state.source.schemaEnabled = array.length > 0;
                    if (sourceSchemaSelect) {
                        sourceSchemaSelect.setData(array);
                        setSelectValues(sourceSchemaSelect, sourceSchema ? [sourceSchema] : []);
                    }
                } else {
                    state.source.schemaEnabled = false;
                    if (sourceSchemaSelect) {
                        sourceSchemaSelect.setData([]);
                    }
                }
                state.source.database = sourceDb;
                state.source.schema = sourceSchema;
                updateTargetSchemaVisibility();
            });
        });
    }

    function closeTablePickerModal() {
        if (!tablePickerModal) {
            return;
        }
        const modal = tablePickerModal;
        cleanupTablePickerState();
        modal.close();
    }

    /**
     * NoneLibraryMap timeClearRight surface map display area
     */
    function resetMappingDetailView() {
        state.activeMappingIndex = -1;
        state.detailPageNum = 1;
        $('#mappingDetailPanel').addClass('hidden');
        $('#mappingDetailTableBody').empty();
        $('#mappingDetailPagination').addClass('hidden');
        updateWorkspaceEmptyState();
    }

    function renderMappingSidebar() {
        const $list = $('#mappingCardList');
        const $empty = $('#mappingSidebarEmpty');
        updateMappingCount();
        if (!state.mappings.length) {
            $list.empty();
            $empty.removeClass('hidden');
            resetMappingDetailView();
            syncMappingsJson();
            return;
        }
        $empty.addClass('hidden');
        if (state.activeMappingIndex < 0 || state.activeMappingIndex >= state.mappings.length) {
            state.activeMappingIndex = 0;
        }
        const useFlowCard = hasDetailTargetPanel();
        let html = '';
        state.mappings.forEach(function (block, idx) {
            const count = (block.tableMappings || []).length;
            const active = idx === state.activeMappingIndex ? ' is-active' : '';
            html += '<div class="list-card' + active + '" data-index="' + idx + '">';
            if (!isReadOnly()) {
                html += '<button type="button" class="list-card-remove" data-index="' + idx + '"title = "Demand Library Map"'
                    + '<i class="fa fa-times"></i></button>';
            }
            if (useFlowCard) {
                html += formatMappingCardBodyHtml(block)
                    + '<div class="list-card-footer">Selected ' + count + ' Watch</div>';
            } else {
                html += '<div class="flex items-center gap-2 text-sm mb-1">'
                    + '<span class="list-card-label">Source &nbsp; &nbsp; library</span>'
                    + '<span class="list-card-value" title="' + escapeHtml(block.sourceDatabase || '') + '">'
                    + escapeHtml(block.sourceDatabase || '') + '</span></div>'
                    + (block.sourceSchema
                        ? ('<div class="flex items-center gap-2 text-sm mb-1">'
                            + '<span class="list-card-label">Source 142</span>'
                            + '<span class="list-card-value" title="' + escapeHtml(block.sourceSchema || '') + '">'
                            + escapeHtml(block.sourceSchema || '') + '</span></div>')
                        : '')
                    + '<div class="flex items-center gap-2 text-sm mb-1">'
                    + '<span class="list-card-label">Objective</span>'
                    + '<input type="text" class="form-control form-control-sm flex-1 min-w-0 mapping-card-tgt-db"'
                    + ' data-index="' + idx + '" value="' + escapeHtml(block.targetDatabase || '') + '"'
                    + ' title="' + escapeHtml(block.targetDatabase || '') + '"'
                    + (isReadOnly() ? ' readonly' : '') + '/>'
                    + '</div>'
                    + (block.sourceSchema
                        ? ('<div class="flex items-center gap-2 text-sm mb-1">'
                            + '<span class="list-card-label">Target/89</span>'
                            + '<input type="text" class="form-control form-control-sm flex-1 min-w-0 mapping-card-tgt-schema"'
                            + ' data-index="' + idx + '" value="' + escapeHtml(block.targetSchema || '') + '"'
                            + ' title="' + escapeHtml(block.targetSchema || '') + '"'
                            + (isReadOnly() ? ' readonly' : '') + '/>'
                            + '</div>')
                        : '')
                    + '<div class="list-card-footer">#' + (block.index || (idx + 1)) + ' :: Total ' + count + ' Object</div>';
            }
            html += '</div>';
        });
        $list.html(html);
        bindMappingSidebarEvents();
        syncMappingsJson();
    }

    function bindMappingSidebarEvents() {
        $('#mappingCardList .list-card').off('click.selectCard').on('click.selectCard', function (e) {
            if ($(e.target).closest('.list-card-remove, .mapping-card-tgt-db, .mapping-card-tgt-schema').length) {
                return;
            }
            syncActiveMappingFromDetailPanel();
            const idx = Number($(this).data('index'));
            state.activeMappingIndex = idx;
            renderMappingSidebar();
            renderMappingDetail(true);
        });

        $('.list-card-remove').off('click.removeCard').on('click.removeCard', function (e) {
            e.stopPropagation();
            const idx = Number($(this).data('index'));
            showConfirm({
                title: 'Are you sure you want to delete the library sync path?',
                icon: 'warning',
                size: 'large',
                confirmType: 'danger',
                onConfirm: function () {
                    state.mappings.splice(idx, 1);
                    if (!state.mappings.length) {
                        renderMappingSidebar();
                        return;
                    }
                    if (state.activeMappingIndex === idx) {
                        state.activeMappingIndex = Math.min(idx, state.mappings.length - 1);
                    } else if (state.activeMappingIndex > idx) {
                        state.activeMappingIndex--;
                    }
                    renderMappingSidebar();
                    renderMappingDetail(true);
                }
            });
        });

        $('.mapping-card-tgt-db').off('change.cardTgtDb').on('change.cardTgtDb', function () {
            const idx = Number($(this).data('index'));
            if (!state.mappings[idx]) {
                return;
            }
            const value = $(this).val();
            state.mappings[idx].targetDatabase = value;
            $(this).attr('title', value || '');
            syncMappingsJson();
        });

        $('.mapping-card-tgt-schema').off('change.cardTgtSchema').on('change.cardTgtSchema', function () {
            const idx = Number($(this).data('index'));
            if (!state.mappings[idx]) {
                return;
            }
            const value = $(this).val();
            state.mappings[idx].targetSchema = value;
            $(this).attr('title', value || '');
            syncMappingsJson();
        });
    }

    function buildDetailTableRowHtml(block, row, rowIndex, blockIndex) {
        const sourceTable = row.sourceTable || '';
        const targetTable = row.targetTable || '';
        let html = '<tr data-r="' + rowIndex + '">'
            + '<td class="text-center text-tertiary">' + (row.index || (rowIndex + 1)) + '</td>'
            + '<td class="text-primary font-medium text-truncate" title="' + escapeHtml(sourceTable) + '">' + escapeHtml(sourceTable) + '</td>';
        if (isReadOnly()) {
            html += '<td title="' + escapeHtml(targetTable) + '">' + escapeHtml(targetTable) + '</td>';
        } else {
            html += '<td><input type="text" class="form-control form-control-sm db-tgt-tbl"'
                + ' data-b="' + blockIndex + '" data-r="' + rowIndex + '" value="' + escapeHtml(targetTable) + '"'
                + ' title="' + escapeHtml(targetTable) + '"/></td>'
                + '<td><button type="button" class="table-action-btn delete btn-rm-detail-row"'
                + ' data-b="' + blockIndex + '" data-r="' + rowIndex + '"title = delete"'
                + '<i class="fa fa-times"></i></button></td>';
        }
        return html + '</tr>';
    }

    function getDetailDisplayRows(block) {
        const rows = block.tableMappings || [];
        return rows;
    }

    function renderDetailPagination(currentPage, totalPages, total) {
        const $pagination = $('#mappingDetailPagination');
        $pagination.find('.mappingDetailTotalCount').text(total);
        $pagination.find('.mappingDetailCurrentPage').text(currentPage);
        $pagination.find('.mappingDetailTotalPages').text(totalPages);
        const $bar = $pagination.find('.pagination-bar');
        $bar.empty();
        if (total <= 0) {
            $pagination.addClass('hidden');
            return;
        }
        $pagination.removeClass('hidden');

        $bar.append(createDetailNavBtn('Home', 'fa-angle-double-left', currentPage === 1, function () {
            renderDetailTablePage(1);
        }));

        $bar.append(createDetailNavBtn('Previous', 'fa-angle-left', currentPage === 1, function () {
            renderDetailTablePage(currentPage - 1);
        }));

        const startPage = Math.max(1, currentPage - 1);
        const endPage = Math.min(totalPages, startPage + 2);
        for (let i = startPage; i <= endPage; i++) {
            const pageBtn = $('<button type="button" class="pagination-btn' + (i === currentPage ? ' active' : '') + '">' + i + '</button>');
            if (i !== currentPage) {
                pageBtn.on('click', function () {
                    renderDetailTablePage(i);
                });
            }
            $bar.append(pageBtn);
        }

        $bar.append(createDetailNavBtn('Next', 'fa-angle-right', currentPage === totalPages, function () {
            renderDetailTablePage(currentPage + 1);
        }));

        $bar.append(createDetailNavBtn('Last page', 'fa-angle-double-right', currentPage === totalPages, function () {
            renderDetailTablePage(totalPages);
        }));
    }

    function createDetailNavBtn(label, iconClass, disabled, onClick) {
        const btn = $('<button type="button" class="pagination-btn pagination-btn-nav" title="' + label + '"' + (disabled ? ' disabled' : '') + '>'
            + '<i class="fa ' + iconClass + '"></i></button>');
        if (!disabled) {
            btn.on('click', onClick);
        }
        return btn;
    }

    function renderDetailTablePage(pageNum) {
        const idx = state.activeMappingIndex;
        const block = state.mappings[idx];
        if (!block) {
            return;
        }
        const rows = getDetailDisplayRows(block);
        const total = rows.length;
        const totalPages = Math.ceil(total / DETAIL_PAGE_SIZE) || 1;
        const page = Math.min(Math.max(1, pageNum), totalPages);
        state.detailPageNum = page;
        const from = (page - 1) * DETAIL_PAGE_SIZE;
        let html = '';
        for (let i = from; i < Math.min(from + DETAIL_PAGE_SIZE, total); i++) {
            html += buildDetailTableRowHtml(block, rows[i], i, idx);
        }
        if (!html) {
            const colSpan = isReadOnly() ? 3 : 4;
            html = '<tr><td colspan="' + colSpan + '"class="text-center text-tertiary py-4"</td></tr>';
        }
        $('#mappingDetailTableBody').html(html);
        renderDetailPagination(page, totalPages, total);
        bindMappingDetailEvents();
    }

    function renderMappingDetail(reset) {
        const idx = state.activeMappingIndex;
        const block = state.mappings[idx];
        if (!block) {
            resetMappingDetailView();
            return;
        }
        $('#mappingDetailPanel').removeClass('hidden');
        updateWorkspaceEmptyState();
        syncDetailPanelFromActiveMapping();
        if (reset) {
            state.detailPageNum = 1;
        }
        renderDetailTablePage(reset ? 1 : state.detailPageNum);
    }

    function findFirstEmptyTargetTable() {
        for (let i = 0; i < state.mappings.length; i++) {
            const tables = state.mappings[i].tableMappings || [];
            for (let j = 0; j < tables.length; j++) {
                const row = tables[j];
                if (!row || !String(row.targetTable || '').trim()) {
                    return {
                        mappingIndex: i,
                        rowIndex: j,
                        sourceTable: (row && row.sourceTable) || ''
                    };
                }
            }
        }
        return null;
    }

    /**
     * Verify INTRODUCTIONTarget Table NameNon-empty;FailedPosition to matchLibraryMap and focus the input box.
     * @returns {boolean} true 1) Passed
     */
    function validateTargetTablesNotEmpty() {
        const hit = findFirstEmptyTargetTable();
        if (!hit) {
            return true;
        }
        const sourceLabel = hit.sourceTable ? 'Source Table' + hit.sourceTable + '」' : 'Existing Table';
        bootGrowl('Library Map#' + (hit.mappingIndex + 1) + '：' + sourceLabel + 'The target list name cannot be empty', 'warning');
        state.activeMappingIndex = hit.mappingIndex;
        renderMappingSidebar();
        renderMappingDetail(false);
        const page = Math.floor(hit.rowIndex / DETAIL_PAGE_SIZE) + 1;
        renderDetailTablePage(page);
        const $input = $('#mappingDetailTableBody .db-tgt-tbl[data-r="' + hit.rowIndex + '"]');
        if ($input.length) {
            $input.focus().select();
        }
        return false;
    }

    function bindMappingDetailEvents() {
        $('#mappingDetailTableBody .db-tgt-tbl').off('change.detailTgtTbl').on('change.detailTgtTbl', function () {
            const b = Number($(this).data('b'));
            const r = Number($(this).data('r'));
            const row = state.mappings[b] && state.mappings[b].tableMappings && state.mappings[b].tableMappings[r];
            if (row) {
                row.targetTable = $(this).val();
                syncMappingsJson();
            }
        });

        $('.btn-rm-detail-row').off('click.rmDetailRow').on('click.rmDetailRow', function () {
            const b = Number($(this).data('b'));
            const r = Number($(this).data('r'));
            const block = state.mappings[b];
            if (!block || !block.tableMappings) {
                return;
            }
            block.tableMappings.splice(r, 1);
            if (!block.tableMappings.length) {
                state.mappings.splice(b, 1);
                if (!state.mappings.length) {
                    renderMappingSidebar();
                    syncMappingsJson();
                    return;
                }
                if (state.activeMappingIndex >= state.mappings.length) {
                    state.activeMappingIndex = state.mappings.length - 1;
                } else if (state.activeMappingIndex > b) {
                    state.activeMappingIndex--;
                } else if (state.activeMappingIndex === b) {
                    state.activeMappingIndex = Math.min(b, state.mappings.length - 1);
                }
                renderMappingSidebar();
                renderMappingDetail(true);
            } else {
                renderMappingSidebar();
                const totalPages = Math.ceil(getDetailDisplayRows(block).length / DETAIL_PAGE_SIZE) || 1;
                if (state.detailPageNum > totalPages) {
                    state.detailPageNum = totalPages;
                }
                renderMappingDetail(false);
            }
            syncMappingsJson();
        });
    }

    function addDatabaseMapping() {
        const sourceTables = getCheckedSourceTableNames();
        if (!state.source.connectorId) {
            bootGrowl('Please select a source connector first', 'warning');
            return false;
        }
        const pickerTargetId = getPickerTargetConnectorId();
        const sourceConnectorId = state.source.connectorId;
        const targetConnectorId = pickerTargetId || state.target.connectorId;
        if (!sourceConnectorId) {
            bootGrowl('Please select a source connector first', 'warning');
            return false;
        }
        if (!targetConnectorId) {
            bootGrowl('Please select the target connection', 'warning');
            return false;
        }
        if (!isSameConnectorType(sourceConnectorId, targetConnectorId)) {
            bootGrowl('Target connector must be of the same type as source connector', 'warning');
            return false;
        }
        state.target.connectorId = targetConnectorId;
        setSelectValues(targetConnectorSelect, [targetConnectorId]);
        setSelectValues(pickerTargetConnectorSelect, [targetConnectorId]);
        const sourceIsOracle = isOracleSourceConnector();
        const targetIsOracle = isOracleTargetConnector(targetConnectorId);
        if (!isSourceNamespaceReady()) {
            if (state.source.schemaEnabled) {
                bootGrowl('Please select the source library and Schema first', 'warning');
            } else if (sourceIsOracle) {
                bootGrowl('Please select the source, Schema.', 'warning');
            } else {
                bootGrowl('Please select the source library first', 'warning');
            }
            return false;
        }
        if (!sourceTables.length) {
            bootGrowl('Please tick at least one source table to use', 'warning');
            return false;
        }
        const sourceDb = sourceIsOracle ? '' : (state.source.database || '');
        const sourceSchema = (state.source.schema || '').trim();
        let defaultTargetDb = ($('#pickerTargetDatabase').val() || '').trim();
        let defaultTargetSchema = ($('#pickerTargetSchema').val() || '').trim();
        if (targetIsOracle) {
            defaultTargetDb = '';
            if (!defaultTargetSchema) {
                defaultTargetSchema = sourceSchema;
            }
        } else {
            if (!defaultTargetDb) {
                defaultTargetDb = sourceDb;
            }
            if (!defaultTargetSchema && sourceSchema) {
                defaultTargetSchema = sourceSchema;
            }
        }
        if (targetIsOracle) {
            if (!defaultTargetSchema) {
                bootGrowl('Please complete the target, Schema.', 'warning');
                return false;
            }
        } else if (!defaultTargetDb) {
            bootGrowl('Target library name cannot be empty.', 'warning');
            return false;
        }
        const existIdx = findMappingIndex(sourceDb, sourceSchema, defaultTargetDb, defaultTargetSchema,
            sourceConnectorId, targetConnectorId);
        const newRows = sourceTables.map(function (srcName) {
            return { sourceTable: srcName, targetTable: srcName };
        });
        if (existIdx >= 0) {
            const exist = state.mappings[existIdx];
            if (!exist.sourceConnectorId) {
                exist.sourceConnectorId = sourceConnectorId;
            }
            if (!exist.targetConnectorId) {
                exist.targetConnectorId = targetConnectorId;
            }
            const names = {};
            (exist.tableMappings || []).forEach(function (r) {
                names[r.sourceTable] = true;
            });
            let added = 0;
            newRows.forEach(function (r) {
                if (!names[r.sourceTable]) {
                    exist.tableMappings.push(r);
                    added++;
                }
            });
            state.activeMappingIndex = existIdx;
            if (!added) {
                bootGrowl('Map of the source to the target library already exists!', 'info');
            }
        } else {
            state.mappings.push({
                sourceDatabase: sourceDb,
                sourceSchema: sourceSchema,
                sourceConnectorId: sourceConnectorId,
                targetConnectorId: targetConnectorId,
                targetDatabase: defaultTargetDb,
                targetSchema: defaultTargetSchema,
                tableMappings: newRows
            });
            state.activeMappingIndex = state.mappings.length - 1;
        }
        renderMappingSidebar();
        renderMappingDetail(true);
        return true;
    }

    function clearMappings() {
        state.mappings = [];
        renderMappingSidebar();
    }

    function initEditFromTask(task) {
        if (!task) {
            return;
        }
        initializing = true;
        state.mappings = JSON.parse(JSON.stringify(task.databaseMappings || []));
        reindexMappings();
        const first = state.mappings.length ? state.mappings[0] : null;
        lastSourceConnectorId = (first && first.sourceConnectorId) || '';
        state.source.connectorId = lastSourceConnectorId;
        state.target.connectorId = (first && first.targetConnectorId) || '';
        state.activeMappingIndex = state.mappings.length ? 0 : -1;

        const $form = $('#database-sync-form');
        $form.find('input[name="name"]').val(task.name || '');
        $form.find('input[name="id"]').val(task.id || '');

        if (state.source.connectorId) {
            setSelectValues(sourceConnectorSelect, [state.source.connectorId]);
            onSourceConnectorChange(state.source.connectorId);
        }
        if (state.target.connectorId && targetConnectorSelect) {
            setSelectValues(targetConnectorSelect, [state.target.connectorId]);
        }

        renderMappingSidebar();
        if (state.mappings.length) {
            renderMappingDetail(true);
        }
        updateWorkspaceEmptyState();
        updateSourceDatabaseFieldVisibility();
        updateTargetSchemaVisibility();
        initializing = false;
    }

    let overwriteSchemaProgrammatic = false;

    function setOverwriteSchemaChecked(checked) {
        overwriteSchemaProgrammatic = true;
        $('input[name="overwriteSchema"]').prop('checked', checked);
        overwriteSchemaProgrammatic = false;
    }

    function bindOverwriteSchemaConfirm() {
        $('input[name="overwriteSchema"]').off('change.syncStrategyOverwrite').on('change.syncStrategyOverwrite', function () {
            if (overwriteSchemaProgrammatic || isReadOnly()) {
                return;
            }
            if (!$(this).is(':checked')) {
                setOverwriteSchemaChecked(false);
                return;
            }
            setOverwriteSchemaChecked(false);
            showConfirm({
                title: 'Confirming an open cover structure?',
                message: 'Opening removes the already existing table of the target library',
                size: 'large',
                icon: 'warning',
                confirmType: 'danger',
                confirmText: 'Confirm',
                cancelText: 'Cancel',
                onConfirm: function () {
                    setOverwriteSchemaChecked(true);
                },
                onCancel: function () {
                    setOverwriteSchemaChecked(false);
                }
            });
        });
    }

    function bindSyncStrategyEvents() {
        $('.db-sync-strategy-enable').each(function () {
            const $enable = $(this);
            const overwriteName = $enable.data('overwrite');
            const $overwrite = $('input[name="' + overwriteName + '"]');
            if (!$overwrite.length) {
                return;
            }
            function refresh() {
                const on = $enable.is(':checked');
                $overwrite.prop('disabled', !on || isReadOnly());
                if (!on) {
                    $overwrite.prop('checked', false);
                }
            }
            $enable.off('change.syncStrategy').on('change.syncStrategy', refresh);
            refresh();
        });
        bindOverwriteSchemaConfirm();
    }

    function initSyncStrategyCheckboxStyle() {
        $('.ds-strategy-enable-group').each(function () {
            const $group = $(this);
            if ($group.data('checkboxGroupInitialized')) {
                return;
            }
            $group.checkboxGroup({
                size: 'sm',
                layout: 'inline'
            });
        });
    }

    function refreshSyncStrategyCheckboxState() {
        $('input.db-sync-strategy-enable').each(function () {
            const $input = $(this);
            const api = $input.data('checkboxGroup');
            if (api && typeof api.setValue === 'function') {
                api.setValue($input.prop('checked'));
            }
        });
    }

    function initSyncStrategyFromTask(task) {
        if (!task) {
            return;
        }
        $('input[name="enableCopySchema"]').prop('checked', !!task.enableCopySchema);
        $('input[name="overwriteSchema"]').prop('checked', !!task.overwriteSchema);
        $('input[name="enableCopyData"]').prop('checked', !!task.enableCopyData);
        $('input[name="overwriteData"]').prop('checked', !!task.overwriteData);
        refreshSyncStrategyCheckboxState();
        bindSyncStrategyEvents();
    }

    function resetDatabaseSyncListPagination() {
        try {
            const key = 'dbsyncer.pagination.database-sync-list';
            const raw = localStorage.getItem(key);
            if (raw) {
                const parsed = JSON.parse(raw);
                parsed.pageNum = 1;
                localStorage.setItem(key, JSON.stringify(parsed));
            }
        } catch (e) {
            console.warn('[ NPO] Reset list page break failed', e);
        }
    }

    $(document).ready(function () {
        window.backIndexPage = function () {
            doLoader('/database-sync/list');
        };

        initSyncStrategyCheckboxStyle();
        bindSyncStrategyEvents();

        const connectorMeta = resolveConnectorMetaFromTask();
        const sourceType = connectorMeta.sourceType || '';

        sourceConnectorSelect = $('#sourceConnectorId').dbSelect(wrapRemoteConnectorOptions({
            relationOnly: true,
            role: 'source',
            disabled: isReadOnly(),
            defaultValue: connectorMeta.sourceId,
            defaultLabel: connectorMeta.sourceLabel,
            defaultType: connectorMeta.sourceType,
            onSelect: function (ids) {
                if (suppressConnectorSelect) {
                    return;
                }
                const id = ids.length ? ids[0] : '';
                if (id) {
                    rememberConnectorMeta(id, getConnectorTypeBySelect('sourceConnectorId', id), '');
                }
                onSourceConnectorChange(id);
                updateSourceDatabaseFieldVisibility();
                updateTargetSchemaVisibility();
            }
        }));

        targetConnectorRemoteExtra = {
            relationOnly: true,
            role: 'target',
            preferredType: normalizeConnectorType(sourceType),
            disabled: isReadOnly(),
            defaultValue: connectorMeta.targetId,
            defaultLabel: connectorMeta.targetLabel,
            defaultType: connectorMeta.targetType,
            onSelect: function (ids) {
                if (suppressConnectorSelect) {
                    return;
                }
                const id = ids.length ? ids[0] : '';
                if (id) {
                    rememberConnectorMeta(id, getConnectorTypeBySelect('targetConnectorId', id), '');
                }
                state.target.connectorId = id;
                const block = getActiveMappingBlock();
                if (block) {
                    block.targetConnectorId = id;
                    syncMappingsJson();
                    const $card = $('#mappingCardList .list-card.is-active');
                    if ($card.length) {
                        $card.find('.list-card-target').html(formatMappingCardTargetLineHtml(block));
                    }
                }
                setSelectValues(pickerTargetConnectorSelect, id ? [id] : []);
                updateTargetSchemaVisibility();
                updateTargetDatabaseFieldVisibility();
            }
        };
        targetConnectorSelect = $('#targetConnectorId').dbSelect(
            wrapRemoteConnectorOptions(targetConnectorRemoteExtra)
        );

        bindDetailPanelEvents();
        refreshTargetConnectorOptions();
        updateTargetSchemaVisibility();

        $('#btnAddDbMapping').on('click', openTablePickerModal);

        $('#database-sync-submit-btn').on('click', function () {
            syncActiveMappingFromDetailPanel();
            syncMappingsJson();
            const $form = $('#database-sync-form');
            if (!validateForm($form)) {
                return;
            }
            if (!state.mappings.length) {
                bootGrowl('Please add at least one library map', 'warning');
                return;
            }
            if (isUnsupportedSourceConnector(state.source.connectorId)) {
                return;
            }
            for (let i = 0; i < state.mappings.length; i++) {
                const mapping = state.mappings[i];
                const srcId = getMappingSourceConnectorId(mapping);
                const tgtId = getMappingTargetConnectorId(mapping);
                if (!isSameConnectorType(srcId, tgtId)) {
                    bootGrowl('Library Map#' + (i + 1) + 'Target connector must be of the same type as source connector', 'warning');
                    return;
                }
            }
            if (!validateTargetTablesNotEmpty()) {
                return;
            }
            const btn = $(this);
            const original = btn.html();
            const saveUrl = isEditMode() ? '/database-sync/edit' : '/database-sync/add';
            btn.html('<i class="fa fa-spinner fa-spin"></i> Saving...').prop('disabled', true);
            doPoster(saveUrl, $form.serializeJson(), function (response) {
                btn.html(original).prop('disabled', false);
                if (response.success) {
                    bootGrowl(isEditMode() ? 'Changes successfully' : 'Saved successfully', 'success');
                    if (isEditMode()) {
                        resetDatabaseSyncListPagination();
                    }
                    backIndexPage();
                } else {
                    bootGrowl(response.message || 'Save failed', 'danger');
                }
            });
        });

        if (isEditMode() && page.task) {
            initSyncStrategyFromTask(page.task);
            initEditFromTask(page.task);
        } else {
            renderMappingSidebar();
            updateWorkspaceEmptyState();
        }
    });
})();
