/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.dbsyncer.biz.task;

import org.dbsyncer.biz.TableGroupService;
import org.dbsyncer.common.enums.DispatchTaskEnum;
import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.parser.MappingProfile;
import org.dbsyncer.parser.TableGroupProfile;
import org.dbsyncer.parser.model.Mapping;
import org.dbsyncer.parser.model.Meta;
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
        logger.info("Other Organiser", mapping.getName(), groupCount);
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
