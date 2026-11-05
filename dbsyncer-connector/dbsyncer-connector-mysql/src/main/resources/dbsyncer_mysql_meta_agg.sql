CREATE TABLE `dbsyncer_meta_agg`
(
    `ID`          varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
    `STAT_DATE`   varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Statistical date yyy-MM-dd',
    `TOTAL`       bigint                                                NOT NULL DEFAULT 0 COMMENT 'Sync Tasks',
    `SUCCESS`     bigint                                                NOT NULL DEFAULT 0 COMMENT 'Synchronise Task',
    `FAIL`        bigint                                                NOT NULL DEFAULT 0 COMMENT 'Synchronise Taskfail',
    `CREATE_TIME` bigint                                                NOT NULL COMMENT 'Created at',
    `UPDATE_TIME` bigint                                                NOT NULL COMMENT 'Updated at',
    PRIMARY KEY (`ID`) USING BTREE,
    UNIQUE KEY `UK_META_AGG_STAT_DATE` (`STAT_DATE`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='Sync Task Meta Aggregation' ROW_FORMAT=Dynamic;
