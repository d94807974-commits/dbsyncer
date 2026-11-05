/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.sdk.model;

/**
 * @Author AE86
 * @Version 1.0.0
 * @Date 2024-05-18 00:32
 */
public class Product {

    /**
     * UNDAF functionName
     */
    private String name;

    /**
     * Valid.Time
     */
    private long effectiveTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(long effectiveTime) {
        this.effectiveTime = effectiveTime;
    }
}
