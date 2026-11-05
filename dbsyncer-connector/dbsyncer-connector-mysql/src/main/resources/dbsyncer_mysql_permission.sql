CREATE TABLE `dbsyncer_permission` (
  `ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
  `CREATE_TIME` bigint NOT NULL COMMENT 'Created at',
  `UPDATE_TIME` bigint NOT NULL COMMENT 'Updated at',
  `CODE` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Permission Encoding, e. g.mapping:edit',
  `NAME` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Permission Name',
  `MODULE` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Module: user/conector Increasingly/notice/license/system etc.',
  PRIMARY KEY (`ID`) USING BTREE,
  UNIQUE KEY `UK_PERM_CODE` (`CODE`) USING BTREE,
  KEY `IDX_PERM_MODULE` (`MODULE`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'Menu Permission Table' ROW_FORMAT = Dynamic;
