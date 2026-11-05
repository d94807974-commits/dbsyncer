/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.sdk.model;

import java.util.Map;

/**
 * minSni Expected Boundary Resolution Request: By Initial CurveandThe budget resolves the cursor.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-09-04
 */
public class CursorBoundRequest {

    /**
     * TableTable GroupSavedEnforcement ordersQUERY / QUERY_CURSOR / CURSOR_PK_NAMES Wait.
     */
    private Map<String, String> command;

    /**
     * Source table（HamFieldandLordKey Tags）
     */
    private Table sourceTable;

    /**
     * Start Cursor (Strength Line); empty string indicates the header
     */
    private String startCursor = "";

    /**
     * Maximum number Policies
     */
    private int budget;

    public Map<String, String> getCommand() {
        return command;
    }

    public void setCommand(Map<String, String> command) {
        this.command = command;
    }

    public Table getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(Table sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String getStartCursor() {
        return startCursor;
    }

    public void setStartCursor(String startCursor) {
        this.startCursor = startCursor;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }
}
