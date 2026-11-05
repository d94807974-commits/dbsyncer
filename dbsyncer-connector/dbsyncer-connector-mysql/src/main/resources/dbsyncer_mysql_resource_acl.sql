CREATE TABLE `dbsyncer_resource_acl` (
  `ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
  `CREATE_TIME` bigint NOT NULL COMMENT 'Created at',
  `UPDATE_TIME` bigint NOT NULL COMMENT 'Updated at',
  `SUBJECT_TYPE` tinyint NOT NULL COMMENT 'Subject type: 0-USER; 1-ROLE',
  `SUBJECT_ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Subject ID',
  `RESOURCE_TYPE` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Resource type: group/conector/mapping/validateSync/databaseSync',
  `RESOURCE_ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Resource ID',
  `PERMISSION` tinyint NOT NULL COMMENT '0-read-only, 1-read-write',
  PRIMARY KEY (`ID`) USING BTREE,
  UNIQUE KEY `UK_RES_ACL` (`SUBJECT_TYPE`, `SUBJECT_ID`, `RESOURCE_TYPE`, `RESOURCE_ID`) USING BTREE,
  KEY `IDX_RES_ACL_RESOURCE` (`SUBJECT_ID`, `RESOURCE_TYPE`, `RESOURCE_ID`) USING BTREE,
  KEY `IDX_RES_ACL_BY_RES` (`RESOURCE_TYPE`, `RESOURCE_ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'Resource Accountability Table' ROW_FORMAT = Dynamic;
