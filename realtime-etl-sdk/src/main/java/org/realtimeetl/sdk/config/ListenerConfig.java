package org.realtimeetl.sdk.config;

import org.realtimeetl.sdk.enums.ListenerTypeEnum;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2019/10/8 22:36
 */
public class ListenerConfig {

    /**
     * ListenerType
     * {@link ListenerTypeEnum}
     */
    private String listenerType;

    /**
     * ScheduledExpression, Format: [seconds] [min] [hours] [Day] [Intern] [Week]
     */
    private String cron = "*/30 * * * * ?";

    /**
     * EventField
     */
    private String eventFieldName = "";

    /**
     * UpdateEvent, Like when?eventFieldNameValueequalsU orupdateat which time the article data were determined to beUpdateActions
     */
    private String update = "U";

    /**
     * InsertEvent
     */
    private String insert = "I";

    /**
     * DeleteEvent
     */
    private String delete = "D";

    /**
     * DisableUpdateEvent
     */
    private boolean enableUpdate = true;

    /**
     * DisableInsertEvent
     */
    private boolean enableInsert = true;

    /**
     * DisableDeleteEvent
     */
    private boolean enableDelete = true;

    /**
     * DisableddlEvent
     */
    private boolean enableDDL;

    public ListenerConfig() {
    }

    public ListenerConfig(String listenerType) {
        this.listenerType = listenerType;
    }

    public String getListenerType() {
        return listenerType;
    }

    public void setListenerType(String listenerType) {
        this.listenerType = listenerType;
    }

    public String getEventFieldName() {
        return eventFieldName;
    }

    public void setEventFieldName(String eventFieldName) {
        this.eventFieldName = eventFieldName;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String getInsert() {
        return insert;
    }

    public void setInsert(String insert) {
        this.insert = insert;
    }

    public String getDelete() {
        return delete;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public boolean isEnableUpdate() {
        return enableUpdate;
    }

    public void setEnableUpdate(boolean enableUpdate) {
        this.enableUpdate = enableUpdate;
    }

    public boolean isEnableInsert() {
        return enableInsert;
    }

    public void setEnableInsert(boolean enableInsert) {
        this.enableInsert = enableInsert;
    }

    public boolean isEnableDelete() {
        return enableDelete;
    }

    public void setEnableDelete(boolean enableDelete) {
        this.enableDelete = enableDelete;
    }

    public boolean isEnableDDL() {
        return enableDDL;
    }

    public void setEnableDDL(boolean enableDDL) {
        this.enableDDL = enableDDL;
    }
}
