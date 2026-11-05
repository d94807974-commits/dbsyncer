package org.realtimeetl.parser.util;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.model.Convert;
import org.realtimeetl.parser.model.FieldMapping;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.Filter;
import org.realtimeetl.sdk.model.ValidateSyncTask;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class PickerUtil {

    /**
     * MergeParameterConfigure、Filter Conditions、Convert Configuration、Plugin Configuration、Target source field、Source fields
     *
     * @param mapping
     * @param tableGroup
     */
    public static TableGroup mergeTableGroupConfig(Mapping mapping, TableGroup tableGroup) {
        TableGroup group = new TableGroup();
        BeanUtils.copyProperties(tableGroup, group);

        // ParameterConfigure(Default global use)
        group.setParams(CollectionUtils.isEmpty(tableGroup.getParams()) ? mapping.getParams() : tableGroup.getParams());
        // Filter Conditions(Default global use)
        group.setFilter(CollectionUtils.isEmpty(tableGroup.getFilter()) ? mapping.getFilter() : tableGroup.getFilter());
        // Convert Configuration(Default global use)
        group.setConvert(CollectionUtils.isEmpty(tableGroup.getConvert()) ? mapping.getConvert() : tableGroup.getConvert());
        // Plugin Configuration(Default global use)
        group.setPlugin(null == tableGroup.getPlugin() ? mapping.getPlugin() : tableGroup.getPlugin());
        // PluginParameter(Default global use)
        group.setPluginExtInfo(StringUtil.isBlank(tableGroup.getPluginExtInfo()) ? mapping.getPluginExtInfo() : tableGroup.getPluginExtInfo());

        // MergeIncrementalConfigure/Filter Conditions/Convert ConfigurationField
        appendFieldMapping(mapping, group);
        return group;
    }

    /**
     * Revised verification tasksMerge Table GroupConfigure、Filter Conditions、Convert Configuration、Plugin Configuration、Target source field、Source fields
     */
    public static TableGroup mergeTableGroupConfig(ValidateSyncTask task, TableGroup tableGroup) {
        TableGroup group = new TableGroup();
        BeanUtils.copyProperties(tableGroup, group);
        group.setFilter(CollectionUtils.isEmpty(tableGroup.getFilter()) ? task.getFilter() : tableGroup.getFilter());
        mapFieldsForSyncValidate(group);
        return group;
    }

    public static Map<String, Field> convert2Map(List<Field> col) {
        return col.stream().collect(Collectors.toMap(Field::getName, f->f, (k1, k2)->k1));
    }

    /**
     * Data validationDo it.Field mapping
     */
    private static void mapFieldsForSyncValidate(TableGroup group) {
        final List<FieldMapping> fieldMapping = group.getFieldMapping();
        List<Filter> filter = group.getFilter();
        if (!CollectionUtils.isEmpty(filter)) {
            Map<String, Field> fields = convert2Map(group.getSourceTable().getColumn());
            filter.forEach(f -> addFieldMapping(fieldMapping, f.getName(), fields, true));
        }
        List<Convert> convert = group.getConvert();
        if (!CollectionUtils.isEmpty(convert)) {
            Map<String, Field> fields = convert2Map(group.getTargetTable().getColumn());
            convert.forEach(c -> addFieldMapping(fieldMapping, c.getName(), fields, false));
        }
    }

    private static void appendFieldMapping(Mapping mapping, TableGroup group) {
        final List<FieldMapping> fieldMapping = group.getFieldMapping();

        // InspectionIncrementalFieldYesNoYes.MappingMedium
        String eventFieldName = mapping.getListener() != null ? mapping.getListener().getEventFieldName() : null;
        if (StringUtil.isNotBlank(eventFieldName)) {
            Map<String, Field> fields = convert2Map(group.getSourceTable().getColumn());
            addFieldMapping(fieldMapping, eventFieldName, fields, true);
        }

        // InspectionFilter ConditionsYesNoYes.MappingMedium
        List<Filter> filter = group.getFilter();
        if (!CollectionUtils.isEmpty(filter)) {
            Map<String, Field> fields = convert2Map(group.getSourceTable().getColumn());
            filter.forEach(f->addFieldMapping(fieldMapping, f.getName(), fields, true));
        }

        // InspectionConvert ConfigurationYesNoYes.MappingMedium
        List<Convert> convert = group.getConvert();
        if (!CollectionUtils.isEmpty(convert)) {
            Map<String, Field> fields = convert2Map(group.getTargetTable().getColumn());
            convert.forEach(c->addFieldMapping(fieldMapping, c.getName(), fields, false));
        }
    }

    private static void addFieldMapping(List<FieldMapping> fieldMapping, String name, Map<String, Field> fields, boolean checkSource) {
        if (StringUtil.isNotBlank(name)) {
            boolean exist = false;
            for (FieldMapping m : fieldMapping) {
                Field f = checkSource ? m.getSource() : m.getTarget();
                if (null == f) {
                    continue;
                }
                if (StringUtil.equals(f.getName(), name)) {
                    exist = true;
                    break;
                }
            }
            if (!exist && null != fields.get(name)) {
                FieldMapping fm = checkSource ? new FieldMapping(fields.get(name), null) : new FieldMapping(null, fields.get(name));
                fieldMapping.add(fm);
            }
        }
    }

    public static List<Field> pickCommonFields(List<Field> column, List<Field> target) {
        if (CollectionUtils.isEmpty(column) || CollectionUtils.isEmpty(target)) {
            return target;
        }
        List<Field> list = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        column.forEach(f->keys.add(f.getName()));
        target.forEach(f-> {
            if (keys.contains(f.getName())) {
                list.add(f);
            }
        });
        return list;
    }
}
