package org.realtimeetl.biz.enums;

import org.realtimeetl.biz.BizException;
import org.realtimeetl.common.util.StringUtil;

/**
 * System indicators
 *
 * @author AE86
 * @version 1.0.0
 * @date 2021/07/22 19:19
 */
public enum MetricEnum {

    /**
     * ThreadsLiveNumber
     */
    THREADS_LIVE("jvm.threads.live", "App threads", "Live"),

    /**
     * ThreadsPeak
     */
    THREADS_PEAK("jvm.threads.peak", "App threads", "Peak"),

    /**
     * MemoryUsed
     */
    MEMORY_USED("jvm.memory.used", "Memory", "Used"),

    /**
     * MemoryFree
     */
    MEMORY_COMMITTED("jvm.memory.committed", "Memory", "Free"),

    /**
     * MemoryTotal
     */
    MEMORY_MAX("jvm.memory.max", "Memory", "Total"),

    /**
     * GC
     */
    GC_PAUSE("jvm.gc.pause", "GC", ""),

    /**
     * CPUUsed
     */
    CPU_USAGE("system.cpu.usage", "CPU", ""),

    /**
     * System Environment
     */
    SYSTEM_ENV("system.info", "Operating environment", "");

    private final String code;
    private final String group;
    private final String metricName;

    MetricEnum(String code, String group, String metricName) {
        this.code = code;
        this.group = group;
        this.metricName = metricName;
    }

    public static MetricEnum getMetric(String code) throws BizException {
        for (MetricEnum e : MetricEnum.values()) {
            if (StringUtil.equals(code, e.getCode())) {
                return e;
            }
        }
        return null;
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
