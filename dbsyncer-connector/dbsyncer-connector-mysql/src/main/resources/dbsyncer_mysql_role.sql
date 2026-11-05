CREATE TABLE `dbsyncer_role` (
  `ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
  `CREATE_TIME` bigint NOT NULL COMMENT 'Created at',
  `UPDATE_TIME` bigint NOT NULL COMMENT 'Updated at',
  `NAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Role Name',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'Role Sheet' ROW_FORMAT = Dynamic;
