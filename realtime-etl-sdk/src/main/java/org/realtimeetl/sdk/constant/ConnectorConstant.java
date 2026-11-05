package org.realtimeetl.sdk.constant;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2019/9/19 23:56
 */
public class ConnectorConstant {

    /**
     * Insert
     */
    public static final String OPERTION_INSERT = "INSERT";

    /**
     * Update
     */
    public static final String OPERTION_UPDATE = "UPDATE";

    /**
     * OverwriteUpdate
     */
    public static final String OPERTION_UPSERT = "UPSERT";

    /**
     * Delete
     */
    public static final String OPERTION_DELETE = "DELETE";

    /**
     * TableStructureChange
     */
    public static final String OPERTION_ALTER = "ALTER";

    /**
     * Question
     */
    public static final String OPERTION_QUERY = "QUERY";

    /**
     * Query Curve
     */
    public static final String OPERTION_QUERY_CURSOR = "QUERY_CURSOR";

    /**
     * minPageQuestionObjective CombinedEnd
     */
    public static final String OPERTION_QUERY_TARGET = "QUERY_TARGET";

    /**
     * Objective CombinedEnd PressPrimary keyBatchIN QueryNoneminPage）
     */
    public static final String OPERTION_QUERY_TARGET_IN = "QUERY_TARGET_IN";

    /**
     * Objective CombinedEnd CursorQuery
     */
    public static final String OPERTION_QUERY_TARGET_CURSOR = "QUERY_TARGET_CURSOR";

    /**
     * SourcePressPrimary keyBatchIN QueryNoneminPage）
     */
    public static final String OPERTION_QUERY_SOURCE_IN = "QUERY_SOURCE_IN";

    /**
     * CursorminPageActualPrimary keyFirst NameList（CommaminPhoto from Flickr by Flickr user pic.twitter.comand QUERY_CURSOR Yes.SQL Placeholders agree.
     */
    public static final String CURSOR_PK_NAMES = "CURSOR_PK_NAMES";

    /**
     * QuestionFilter Conditions
     */
    public static final String OPERTION_QUERY_FILTER = "QUERY_FILTER";

    /**
     * Objective CombinedEnd QueryFieldList（ES fetchSource ( will will will will will will, will will, will will will, will, will, will will will, will will will will, will will, will will will, will will will will will, will will will, will will will will, will will will will, will will will will will will will, will will will,, will will will will will, will will will,, will will will will will will will will,, will will will will,, will will will will will will will will will will,, will will will will,,, will will will will will will will will will will will will,,, will will will will,,,, will will will will will will will will,, will will will will will will will will will,,, will will meeting, will will will will will,,, meeting will will will will will will will will will will will meeting,,, will will will will will will will will meeting meeting,,,,, meeting will will will will will will will will meeting meeting meeting,,,, will will will will meeting meeting meeting meeting meeting meeting meeting,, will will will meeting, will meeting,,, will meeting meeting,, will will meeting will meeting, will will will willmin(c) Separation;andSource {@link #OPERTION_QUERY} Correspond.
     */
    public static final String OPERTION_TARGET_QUERY_FIELDS = "TARGET_QUERY_FIELDS";

    /**
     * QuestionTotal
     */
    public static final String OPERTION_QUERY_COUNT = "QUERY_COUNT";

    /**
     * Target tableQuestionTotal
     */
    public static final String TARGET_QUERY_COUNT = "TARGET_QUERY_COUNT";

    /**
     * Main Table，ExtensionMap relation
     * <p>scene1:SupportCustomSQLAs a new table, need to be based onMain TableListenIncrementalData Fact
     * <p>scene2:SupportCustomSemi-structuredFieldAs a new table, need to be based onTopic（Kafka），FileName（File）ListenIncrementalData Fact
     */
    public static final String CUSTOM_TABLE_MAIN = "CT_MAIN";

    public static final String CUSTOM_TABLE_SQL = "CT_SQL";
}
