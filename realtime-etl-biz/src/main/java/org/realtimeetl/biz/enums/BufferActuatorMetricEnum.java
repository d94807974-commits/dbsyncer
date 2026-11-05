package org.realtimeetl.biz.enums;

/**
 * Cache Executor Indicators
 *
 * @author AE86
 * @version 1.0.0
 * @date 2021/07/23 0:19
 */
public enum BufferActuatorMetricEnum {

    /**
     * Storage actuator
     */
    STORAGE("buffer.actuator.storage", "Storage actuator", ""),
    /**
     * General actuator
     */
    GENERAL("buffer.actuator.general", "General actuator", ""),
    /**
     * TableExecut MOU
     */
    TABLE_GROUP("buffer.actuator.table.group", "Table-group actuator", "");

    private final String code;
    private final String group;
    private final String metricName;

    BufferActuatorMetricEnum(String code, String group, String metricName) {
        this.code = code;
        this.group = group;
        this.metricName = metricName;
    }

    public String getCode() {
        return code;
    }

    public String getGroup() {
        return group;
    }

    public String getMetricName() {
        return metricName;
    }
}
