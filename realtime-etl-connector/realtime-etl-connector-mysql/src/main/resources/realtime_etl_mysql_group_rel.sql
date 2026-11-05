CREATE TABLE `realtime_etl_group_rel` (
  `ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
  `CREATE_TIME` bigint NOT NULL COMMENT 'Created at',
  `UPDATE_TIME` bigint NOT NULL COMMENT 'Updated at',
  `GROUP_ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Group ID',
  `RESOURCE_TYPE` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Resource type: Contractor/mapping/validateSync/databaseSync',
  `RESOURCE_ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Resource ID (connection or task ID)',
  PRIMARY KEY (`ID`) USING BTREE,
  UNIQUE KEY `UK_GROUP_RESOURCE` (`GROUP_ID`, `RESOURCE_TYPE`, `RESOURCE_ID`) USING BTREE,
  KEY `IDX_GROUP_RESOURCE` (`GROUP_ID`, `RESOURCE_TYPE`) USING BTREE,
  KEY `IDX_RESOURCE` (`RESOURCE_TYPE`, `RESOURCE_ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'Business group relationship tables' ROW_FORMAT = Dynamic;
