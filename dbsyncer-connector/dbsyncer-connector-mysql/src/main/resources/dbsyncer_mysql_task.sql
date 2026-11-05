CREATE TABLE `dbsyncer_task` (
  `ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
  `CREATE_TIME` bigint NOT NULL COMMENT 'Created at',
  `UPDATE_TIME` bigint NOT NULL COMMENT 'Updated at',
  `NAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Task name',
  `TYPE` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Legal, data synchronisation/revision/pool migration',
  `JSON` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Configuration',
  PRIMARY KEY (`ID`) USING BTREE,
  KEY `IDX_UPDATE_TIME` (`UPDATE_TIME`) USING BTREE,
  KEY `IDX_TYPE_UPDATE_TIME` (`TYPE`, `UPDATE_TIME`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'Task Profile' ROW_FORMAT = Dynamic;
