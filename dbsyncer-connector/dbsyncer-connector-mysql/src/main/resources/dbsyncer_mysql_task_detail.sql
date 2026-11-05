CREATE TABLE `dbsyncer_task_detail` (
  `ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
  `CREATE_TIME` bigint NOT NULL COMMENT 'Created at',
  `UPDATE_TIME` bigint NOT NULL COMMENT 'Updated at',
  `TABLE_GROUP_ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Association Table Map ID',
  `TYPE` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Other Organiser',
  `TARGET_TABLE` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT '' COMMENT 'Target list name',
  `IS_SUCCESS` tinyint NOT NULL DEFAULT 0 COMMENT 'Success, 0-failed 1-success; for life cycle see Meta.STATE / DATA.status',
  `ERROR` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'Malformed information',
  `DATA` blob DEFAULT NULL COMMENT 'Detailed data',
  PRIMARY KEY (`ID`) USING BTREE,
  KEY `IDX_TG_UPDATE` (`TABLE_GROUP_ID`, `UPDATE_TIME`) USING BTREE,
  KEY `IDX_SUCCESS_CREATE_TIME` (`IS_SUCCESS`, `CREATE_TIME`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'Schedule of tasks' ROW_FORMAT = Dynamic;
