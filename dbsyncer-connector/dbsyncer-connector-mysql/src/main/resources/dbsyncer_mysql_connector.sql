CREATE TABLE `dbsyncer_connector` (
  `ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
  `CREATE_TIME` bigint NOT NULL COMMENT 'Created at',
  `UPDATE_TIME` bigint NOT NULL COMMENT 'Updated at',
  `NAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Analysis',
  `TYPE` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Connection Type',
  `IS_SOURCE` Tinyint NOT NULL DEFAULT 1 COMMENT ' as source identifier, 0-No 1-Yes ',
  `IS_TARGET` Tinyint NOT NULL DEFAULT 1 COMMENT 'as target identifier, 0-No 1-Yes ',
  `STATUS` tinyint NOT NULL DEFAULT 1 COMMENT 'Connect, 0-offline 1-Online ',
  `JSON` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Configuration',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'Connection Profile' ROW_FORMAT = Dynamic;
