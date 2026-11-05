/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.sdk.connector.database;

import net.sf.jsqlparser.statement.alter.Alter;
import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.sdk.config.SqlBuilderConfig;
import org.dbsyncer.sdk.enums.SqlBuilderEnum;
import org.dbsyncer.sdk.model.Field;
import org.dbsyncer.sdk.model.PageSql;
import org.dbsyncer.sdk.model.ValidateSyncTask;
import org.dbsyncer.sdk.plugin.ReaderContext;

import java.util.List;
import java.util.stream.Collectors;

public interface Database {

    /**
     * AccessdbsUnique ID number
     */
    default String generateUniqueCode() {
        return StringUtil.EMPTY;
    }

    /**
     * Get quotes (not by default)
     */
    default String buildSqlWithQuotation() {
        return StringUtil.EMPTY;
    }

    /**
     * BackWith quotation marks.Name
     */
    default String buildWithQuotation(String name) {
        return buildSqlWithQuotation() + name + buildSqlWithQuotation();
    }

    /**
     * GenerateCreate DataLibrary（Or a class namespace.DDL；No errors reported when existing (per cent)LibrarySourceConnectorOverwrite）。
     */
    String buildCreateDatabaseSql(String databaseName, String schemaName);

    /**
     * Judgement ConfirmationObjective CombinedNamespaceYesNoExistenceLibraryFirst Name+ Schema ByConnectorAn explanation of the dialect 101.
     */
    boolean databaseExists(DatabaseConnectorInstance connectorInstance, String databaseName, String schemaName);

    /**
     * GenerateCreate TableDDL
     */
    String getTargetTableDDL(DatabaseConnectorInstance targetInstance, String tableName, String sourceDDL);

    /**
     * FromSource databaseFetching TablesDDL
     */
    String getSourceTableDDL(DatabaseConnectorInstance sourceInstance, String sourceTableName);

    /**
     * GenerateDeleteTableDDL；targetInstance
     */
    String buildDropTableSql(DatabaseConnectorInstance targetInstance, String tableName);

    /**
     * AccessPrimary keyField nameClaims
     */
    default List<String> buildPrimaryKeys(List<String> primaryKeys) {
        if (CollectionUtils.isEmpty(primaryKeys)) {
            return primaryKeys;
        }
        return primaryKeys.stream().map(this::buildWithQuotation).collect(Collectors.toList());
    }

    /**
     * App AuthorityPrimary keyandParameterPlaceholder
     *
     * @param sql
     * @param primaryKeys
     */
    default void appendPrimaryKeys(StringBuilder sql, List<String> primaryKeys) {
        if (CollectionUtils.isEmpty(primaryKeys)) {
            return;
        }
        List<String> pks = primaryKeys.stream().map(name -> buildWithQuotation(name) + "=?").collect(Collectors.toList());
        sql.append(StringUtil.join(pks, " AND "));
    }

    /**
     * AccessminPageSQL
     *
     * @param config
     * @return
     */
    String getPageSql(PageSql config);

    /**
     * AccessminPageParameter
     *
     * @param context
     * @return
     */
    Object[] getPageArgs(ReaderContext context);

    /**
     * Get cursorsminPageSQL
     *
     * @param pageSql
     * @return
     */
    String getPageCursorSql(PageSql pageSql);

    /**
     * Get cursorsminPageParameter
     *
     * @param context
     * @return
     */
    Object[] getPageCursorArgs(ReaderContext context);

    /**
     * Batch BuildMODIFY / ALTER COLUMN statement.
     * Default achieves retreat to column-by-column; recommends dataLibrarydialectOverwrite(zz) Be original mass syntax.
     *
     * @param targetInstance    Target ConnectionExamples
     * @param task              Verify INTRODUCTIONTasks
     * @param targetTableName   Target Table Name
     * @param sourceDefinitions SourceFieldDefinitionsList（(Performance of expectations)
     * @param targetColumnNames Objective CombinedListingList（and sourceDefinitions One-on-one
     * @return Executable lotDDL
     */
    String buildModifyColumnsSql(DatabaseConnectorInstance targetInstance, ValidateSyncTask task,
                                 String targetTableName, List<Field> sourceDefinitions,
                                 List<String> targetColumnNames);

    /**
     * Health screening
     *
     * @return
     */
    default String getValidationQuery() {
        return "select 1";
    }

    /**
     * Get queriesTotalSQL
     */
    default String getQueryCountSql(SqlBuilderConfig sqlConfig) {
        return SqlBuilderEnum.QUERY_COUNT.getSqlBuilder().buildSql(sqlConfig);
    }

    /**
     * QuestionObjective CombinedTotalTable
     */
    default String getQueryTargetCountSql(SqlBuilderConfig sqlConfig) {
        return SqlBuilderEnum.TARGET_QUERY_COUNT.getSqlBuilder().buildSql(sqlConfig);
    }

    /**
     * Generateupsert
     */
    String buildUpsertSql(DatabaseConnectorInstance connectorInstance, SqlBuilderConfig config);

    /**
     * Generateinsert
     */
    default String buildInsertSql(SqlBuilderConfig config) {
        return SqlBuilderEnum.INSERT.getSqlBuilder().buildSql(config);
    }

    default boolean buildCustom(List<String> fs, Field field) {
        return false;
    }

    /**
     * For SpecialFieldTypeICALLYCustomYes.ValueExpression
     *
     * <p>ForINSERT/UPDATE StatementVALUES Departmentmin，Allow DataLibraryConnectorFor SpecificFieldType（Likegeometry、geography）
     * ProvisionCustomYes.SQL Expression, noYes52. Simple placeholder?</p>
     *
     * <p>For example:SQL Server Yes.geometry TypeRequiredgeometry::STGeomFromText(?, ?) Come on.Convert</p>
     *
     * @param vs    ValueExpressionList（VALUES Departmentmin）
     * @param field FieldInfo
     * @return true OrganisationAddCustomValueExpression Actualfalse For default? Placeholder
     */
    default boolean buildCustomValue(List<String> vs, Field field) {
        return false;
    }

    /**
     * Replaceddlprepared forcatalog
     *
     * @param connectorInstance
     * @param alter
     * @return
     */
    default String buildAlterCatalog(DatabaseConnectorInstance connectorInstance, Alter alter) {
        return alter.toString();
    }
}