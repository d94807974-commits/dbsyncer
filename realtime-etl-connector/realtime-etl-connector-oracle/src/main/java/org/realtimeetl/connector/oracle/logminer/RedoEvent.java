/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.realtimeetl.connector.oracle.logminer;

import java.sql.Timestamp;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2023-12-09 20:22
 */
public final class RedoEvent {

    private long scn;
    private int operationCode;
    private String redoSql;
    private String objectOwner;
    private String objectName;
    private Timestamp sourceTime;
    private String transactionId;

    public RedoEvent(long scn, int operationCode, String redoSql, String objectOwner, String objectName, Timestamp sourceTime, String transactionId) {
        this.scn = scn;
        this.operationCode = operationCode;
        this.redoSql = redoSql;
        this.objectOwner = objectOwner;
        this.objectName = objectName;
        this.sourceTime = sourceTime;
        this.transactionId = transactionId;
    }

    public long getScn() {
        return scn;
    }

    public void setScn(long scn) {
        this.scn = scn;
    }

    public int getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(int operationCode) {
        this.operationCode = operationCode;
    }

    public String getRedoSql() {
        return redoSql;
    }

    public void setRedoSql(String redoSql) {
        this.redoSql = redoSql;
    }

    public String getObjectOwner() {
        return objectOwner;
    }

    public void setObjectOwner(String objectOwner) {
        this.objectOwner = objectOwner;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Timestamp getSourceTime() {
        return sourceTime;
    }

    public void setSourceTime(Timestamp sourceTime) {
        this.sourceTime = sourceTime;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return "LogMinerDmlObject{" + "redoSql='" + redoSql + '\'' + ", objectOwner='" + objectOwner + '\'' + ", objectName='" + objectName + '\'' + ", sourceTime=" + sourceTime + ", transactionId='"
                + transactionId + '\'' + ", scn=" + scn + '}';
    }
}