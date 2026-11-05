/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.plugin.model;


import org.realtimeetl.sdk.enums.NoticeTypeEnum;
import org.realtimeetl.sdk.enums.ModelEnum;
import org.realtimeetl.sdk.model.NoticeContent;

/**
 * MappingStopMessage
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2026-03-04 19:00
 */
public final class MappingStopContent extends NoticeContent {

    private String name;

    private ModelEnum model;

    public MappingStopContent() {
        setNoticeType(NoticeTypeEnum.MAPPING_STOP);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModelEnum getModel() {
        return model;
    }

    public void setModel(ModelEnum model) {
        this.model = model;
    }

}
