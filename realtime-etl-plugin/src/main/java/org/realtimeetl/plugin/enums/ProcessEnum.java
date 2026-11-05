/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.plugin.enums;

/**
 * @Author AE86
 * @Version 1.0.0
 * @Date 2024-08-13 00:27
 */
public enum ProcessEnum {

    /**
     * Full syncPreprocess
     */
    BEFORE,

    /**
     * Full sync/Incremental syncConvert
     */
    CONVERT,

    /**
     * Full sync/Incremental syncPost-processing
     */
    AFTER;
}
