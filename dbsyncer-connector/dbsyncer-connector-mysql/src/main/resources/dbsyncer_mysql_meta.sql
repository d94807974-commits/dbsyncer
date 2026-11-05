CREATE TABLE `dbsyncer_meta` (
  `ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
  `CREATE_TIME` bigint NOT NULL COMMENT 'Created at',
  `UPDATE_TIME` bigint NOT NULL COMMENT 'Updated at',
  `START_TIME` Bigint NOT NULL DEFAULT 0 COMMENT 'task start ',
  `TASK_ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Task ID/table group id',
  `STATE` tinyint NOT NULL DEFAULT 0 COMMENT 'Mission status, 0-not implemented; 1-in-progress; 2-defended; 3-completed;',
  `IS_TASK_DETAIL` tinyint NOT NULL DEFAULT 1 COMMENT 'Is the task specified, 0-No 1-Yes',
  `TOTAL` bigint NOT NULL DEFAULT 0 COMMENT 'Total',
  `SUCCESS` bigint NOT NULL DEFAULT 0 COMMENT 'Successes',
  `FAIL` bigint NOT NULL DEFAULT 0 COMMENT 'Failures',
  `DIFF` bigint NOT NULL DEFAULT 0 COMMENT 'Diffs',
  `FIXED` bigint NOT NULL DEFAULT 0 COMMENT 'Revised',
  `SNAPSHOT` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Can not open message',
  PRIMARY KEY (`ID`) USING BTREE,
  KEY `IDX_TASK_UPDATE_TIME_FAIL` (`TASK_ID`, `FAIL`, `UPDATE_TIME`) USING BTREE,
  KEY `IDX_TASK_IS_DETAIL` (`TASK_ID`, `IS_TASK_DETAIL`) USING BTREE,
  KEY `IDX_TASK_DETAIL_STATE` (`IS_TASK_DETAIL`, `STATE`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'Task execution results table' ROW_FORMAT = Dynamic;
