/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.web.log;

/**
 * {@link OperLog#type()} Constant,and {@link org.dbsyncer.parser.LogType} TypeCode alignment.
 *
 * @author wuji
 * @version 1.0.0
 */
public final class OperLogType {

    /**
     * System-Normal
     */
    public static final String SYSTEM_INFO = "10";
    /**
     * System-Error
     */
    public static final String SYSTEM_ERROR = "12";

    /**
     * Connector-Insert
     */
    public static final String CONNECTOR_INSERT = "20";
    /**
     * Connector-Update
     */
    public static final String CONNECTOR_UPDATE = "21";
    /**
     * Connector-Delete
     */
    public static final String CONNECTOR_DELETE = "22";
    /**
     * Connector-Copy
     */
    public static final String CONNECTOR_COPY = "25";

    /**
     * Sync task-Insert
     */
    public static final String MAPPING_INSERT = "30";
    /**
     * Sync task-Update
     */
    public static final String MAPPING_UPDATE = "31";
    /**
     * Sync task-Delete
     */
    public static final String MAPPING_DELETE = "32";
    /**
     * Sync task-Start
     */
    public static final String MAPPING_RUNNING = "33";
    /**
     * Sync task-Stop
     */
    public static final String MAPPING_STOP = "34";
    /**
     * Sync task-Empty Sync Data
     */
    public static final String MAPPING_CLEAR_DATA = "35";
    /**
     * Sync task-Copy
     */
    public static final String MAPPING_COPY = "36";

    /**
     * Mapping-Insert
     */
    public static final String TABLE_GROUP_INSERT = "40";
    /**
     * Mapping-Update
     */
    public static final String TABLE_GROUP_UPDATE = "41";
    /**
     * Mapping-Delete
     */
    public static final String TABLE_GROUP_DELETE = "42";
    /**
     * Mapping-Copy
     */
    public static final String TABLE_GROUP_COPY = "45";

    /**
     * MetaInfo-Delete Data
     */
    public static final String META_CLEAR = "51";

    /**
     * Plugin-Upload
     */
    public static final String PLUGIN_UPDATE = "60";
    /**
     * Plugin-Delete
     */
    public static final String PLUGIN_DELETE = "62";

    /**
     * Configure-Import
     */
    public static final String CACHE_IMPORT = "70";
    /**
     * Configure-Export
     */
    public static final String CACHE_EXPORT = "72";

    /**
     * User-Insert
     */
    public static final String USER_INSERT = "80";
    /**
     * User-Update
     */
    public static final String USER_UPDATE = "81";
    /**
     * User-Delete
     */
    public static final String USER_DELETE = "82";
    /**
     * Permission-Upload
     */
    public static final String USER_UPLOAD_LICENSE = "83";
    /**
     * Allow - Activate
     */
    public static final String USER_ACTIVATE_LICENSE = "85";
    /**
     * Permission-Delete
     */
    public static final String USER_DELETE_LICENSE = "87";
}
