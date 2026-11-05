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
import org.dbsyncer.parser.model.TableGroup;
import org.dbsyncer.sdk.constant.ConfigConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Count sync tasksTasks
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2025-06-13 00:00
 */
@Service
public final class MappingCountTask extends AbstractCountTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private TableGroupService tableGroupService;

    private String mappingId;

    private String metaSnapshot;

    @Override
    public String getUniqueId() {
        return mappingId;
    }

    @Override
    public DispatchTaskEnum getType() {
        return DispatchTaskEnum.MAPPING_COUNT;
    }

    @Override
    public void execute() {
        Mapping mapping = mappingProfile.get(mappingId);
        int groupCount = tableGroupProfile.getTableGroupCount(mappingId);
        logger.info("Counting {}: {} tables", mapping.getName(), groupCount);
        if (groupCount > 0) {
            tableGroupProfile.pageScanTableGroups(mappingId, ConfigConstant.PAGE_SIZE, page -> {
                if (CollectionUtils.isEmpty(page)) {
                    return;
                }
                for (TableGroup tableGroup : page) {
                    if (tableGroup == null) {
                        continue;
                    }
                    // Sync taskTypeSwitches, early release.Tasks
                    if (shouldStop(mappingId)) {
                        return;
                    }
                    updateTableGroupCount(mapping, tableGroup);
                }
            });
            Meta meta = tableGroupService.refreshMetaTotal(mapping, metaSnapshot);
            logger.info("Completed statistics: }, } sheets, total: {", mapping.getName(), groupCount, meta.getTotal());
        }
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    public void setMetaSnapshot(String metaSnapshot) {
        this.metaSnapshot = metaSnapshot;
    }
}
