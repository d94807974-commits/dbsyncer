CREATE TABLE `realtime_etl_config` (
  `ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
  `CREATE_TIME` bigint NOT NULL COMMENT 'Created at',
  `UPDATE_TIME` bigint NOT NULL COMMENT 'Updated at',
  `NAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Name',
  `TYPE` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'system',
  `JSON` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Configuration',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'Configure Information Sheet' ROW_FORMAT = Dynamic;
