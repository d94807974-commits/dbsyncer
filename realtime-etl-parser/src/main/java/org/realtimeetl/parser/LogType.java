package org.realtimeetl.parser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LogTypeEnumeration
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2020/04/21 16:19
 */
public interface LogType {

    /**
     * minCategoryName
     *
     * @return
     */
    String getName();

    /**
     * Type
     *
     * @return
     */
    String getType();

    /**
     * Contents
     *
     * @return
     */
    String getMessage();

    /**
     * AllLogModule(a) Enumeration;InsertModuleI'll just add it here.
     */
    LogType[][] ALL = {
            SystemLog.values(),
            ConnectorLog.values(),
            MappingLog.values(),
            TableGroupLog.values(),
            MetaLog.values(),
            PluginLog.values(),
            CacheLog.values(),
            UserLog.values(),
            LoginLog.values()
    };

    /**
     * Presstype Code ParsingModulenames; when not matchedBackOriginaltype。
     */
    static String getNameByType(String type) {
        LogType matched = findByType(type);
        return matched == null ? type : matched.getName();
    }

    /**
     * Presstype code search count; Article not matchedBack null。
     */
    static LogType findByType(String type) {
        if (type == null) {
            return null;
        }
        for (LogType[] group : ALL) {
            LogType matched = match(group, type);
            if (matched != null) {
                return matched;
            }
        }
        return null;
    }

    static LogType match(LogType[] values, String type) {
        for (LogType value : values) {
            if (type.equals(value.getType())) {
                return value;
            }
        }
        return null;
    }

    /**
     * Operation logsOptionalModuleName (Exclusion)Login），Press{@link #ALL} Order to heavy.
     * <p>GroupTotalHen.{@link #getName()}，Select the first entry of the group.
     */
    static List<String> listOperModuleNames() {
        return Arrays.stream(ALL)
                .filter(group -> group.length > 0 && !(group[0] instanceof LoginLog))
                .map(group -> group[0].getName())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * PressModuleDivisionTypeCode (coma spell) forIN queries;not matchingBack {@code null}。
     */
    static String joinTypesByModuleName(String moduleName) {
        if (moduleName == null || moduleName.isEmpty()) {
            return null;
        }
        String codes = Arrays.stream(ALL)
                .filter(group -> group.length > 0 && !(group[0] instanceof LoginLog)
                        && moduleName.equals(group[0].getName()))
                .flatMap(Arrays::stream)
                .map(LogType::getType)
                .collect(Collectors.joining(","));
        return codes.isEmpty() ? null : codes;
    }

    /**
     * System Log1
     */
    enum SystemLog implements LogType {

        /**
         * Normal
         */
        INFO("10", "Normal"),
        /**
         * Warning
         */
        WARN("11", "Warning"),
        /**
         * Error
         */
        ERROR("12", "Error");

        private String type;
        private String message;

        SystemLog(String type, String message) {
            this.type = type;
            this.message = message;
        }

        @Override
        public String getName() {
            return "System Log";
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * Connector2
     */
    enum ConnectorLog implements LogType {

        INSERT("20", "Insert"), UPDATE("21", "Update"), DELETE("22", "Delete"), FAILED("23", "Connection failed"), COPY("25", "Copy");

        private String type;
        private String message;

        ConnectorLog(String type, String message) {
            this.type = type;
            this.message = message;
        }

        @Override
        public String getName() {
            return "Connector";
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * Mapping3
     */
    enum MappingLog implements LogType {

        INSERT("30", "Insert"), UPDATE("31", "Update"), DELETE("32", "Delete"), RUNNING("33", "Start"), STOP("34", "Stop"), CLEAR_DATA("35", "Empty Sync Data"), COPY("36", "Copy");

        private String type;
        private String message;

        MappingLog(String type, String message) {
            this.type = type;
            this.message = message;
        }

        @Override
        public String getName() {
            return "Sync task";
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * Mapping4
     */
    enum TableGroupLog implements LogType {

        INSERT("40", "Insert"), UPDATE("41", "Update"), DELETE("42", "Delete"), INCREMENT_FAILED("43", "Incremental sync anomaly"), FULL_FAILED("44", "Full sync anomaly"), COPY("45", "Copy");

        private String type;
        private String message;

        TableGroupLog(String type, String message) {
            this.type = type;
            this.message = message;
        }

        @Override
        public String getName() {
            return "Mapping";
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * MetaInfo5
     */
    enum MetaLog implements LogType {

        DELETE("50", "Delete"), CLEAR("51", "Delete Data");

        private String type;
        private String message;

        MetaLog(String type, String message) {
            this.type = type;
            this.message = message;
        }

        @Override
        public String getName() {
            return "MetaInfo";
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * PluginInfo6
     */
    enum PluginLog implements LogType {

        UPDATE("60", "Upload Apartment"), CHECK_ERROR("61", "Malformed"), DELETE("62", "Deleted successfully");

        private String type;
        private String message;

        PluginLog(String type, String message) {
            this.type = type;
            this.message = message;
        }

        @Override
        public String getName() {
            return "Plugin";
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * CacheConfiguration7
     */
    enum CacheLog implements LogType {

        IMPORT("70", "Import Configuration"), IMPORT_ERROR("71", "Import Configuration Abnormal"), EXPORT("72", "Export Configuration");

        private String type;
        private String message;

        CacheLog(String type, String message) {
            this.type = type;
            this.message = message;
        }

        @Override
        public String getName() {
            return "Configure";
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * User log8
     */
    enum UserLog implements LogType {

        INSERT("80", "Insert"), UPDATE("81", "Update"), DELETE("82", "Delete"), UPLOAD_LICENSE_FILE("83", "Upload Authorization File"), UPLOAD_LICENSE_FILE_ERROR("84", "Failed to upload authorization file CONCERNING"), ACTIVATE_FREE_LICENSE_FILE("85", "Activate Authorization File"),
        ACTIVATE_FREE_LICENSE_FILE_ERROR("86", "Failed to activate authorisation file"), DELETE_LICENSE_FILE("87", "Delete Authorization File"), DELETE_LICENSE_FILE_ERROR("88", "Failed to delete permission file");

        private String type;
        private String message;

        UserLog(String type, String message) {
            this.type = type;
            this.message = message;
        }

        @Override
        public String getName() {
            return "User log";
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * Login logs9
     */
    enum LoginLog implements LogType {

        SUCCESS("90", "Login succeeded"), FAIL("91", "Login failed");

        private String type;
        private String message;

        LoginLog(String type, String message) {
            this.type = type;
            this.message = message;
        }

        @Override
        public String getName() {
            return "Login logs";
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

}