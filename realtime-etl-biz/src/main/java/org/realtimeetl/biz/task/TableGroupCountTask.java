/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.realtimeetl.biz.task;

import org.realtimeetl.biz.TableGroupService;
import org.realtimeetl.common.enums.DispatchTaskEnum;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.TableGroupProfile;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Count sync task tablesTasks
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2025-06-24 01:23
 */
@Service
public final class TableGroupCountTask extends AbstractCountTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private TableGroupService tableGroupService;

    private String mappingId;

    private List<String> tableGroups;

    @Override
    public void execute() throws Exception {
        Mapping mapping = mappingProfile.get(mappingId);
        int groupCount = tableGroupProfile.getTableGroupCount(mappingId);
        logger.info("Counting {}: {} tables", mapping.getName(), groupCount);
        if (!CollectionUtils.isEmpty(tableGroups)) {
            for (String tableGroupId : tableGroups) {
                // Task TypeSwitches, early release.Tasks
                if (shouldStop(mappingId)) {
                    return;
                }
                updateTableGroupCount(mapping, tableGroupProfile.getTableGroup(tableGroupId));
            }
        }
        Meta meta = tableGroupService.refreshMetaTotal(mapping, null);
        logger.info("Completed statistics: }, } sheets, total: {", mapping.getName(), groupCount, meta.getTotal());
    }

    @Override
    public String getUniqueId() {
        return CollectionUtils.isEmpty(tableGroups) ? mappingId : Integer.toHexString(tableGroups.hashCode());
    }

    @Override
    public DispatchTaskEnum getType() {
        return DispatchTaskEnum.TABLE_GROUP_COUNT;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    public void setTableGroups(List<String> tableGroups) {
        this.tableGroups = tableGroups;
    }
}
