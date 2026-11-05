/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.oracle.logminer.parser;

/**
 * LogMiner UPDATE redo No-occupying ranking (except for visibleSQL NULL）。
 * <p>
 * Before writing by{@code OracleListener} PressPrimary keyCheck back.Source tableOverwrite，Avoid AllField UPDATE/MERGE PutObjective Combined LOB Clear。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-08-07 16:45
 */
public final class OracleUnchangedValue {

    public static final OracleUnchangedValue INSTANCE = new OracleUnchangedValue();

    private OracleUnchangedValue() {
    }

    public static boolean isUnchanged(Object value) {
        return value == INSTANCE;
    }

    @Override
    public String toString() {
        return "__UNCHANGED__";
    }
}
