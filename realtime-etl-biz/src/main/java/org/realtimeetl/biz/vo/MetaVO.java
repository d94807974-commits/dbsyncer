package org.realtimeetl.biz.vo;

import org.realtimeetl.parser.model.Meta;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2020/01/03 17:20
 */
public class MetaVO extends Meta {

    // Synchronisation Method
    private String model;
    // Sync taskName
    private String mappingName;
    // YesNoStatisticsTotalMedium
    private boolean counting;

    public MetaVO(String model, String mappingName) {
        this.model = model;
        this.mappingName = mappingName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getMappingName() {
        return mappingName;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    public boolean isCounting() {
        return counting;
    }

    public void setCounting(boolean counting) {
        this.counting = counting;
    }
}
