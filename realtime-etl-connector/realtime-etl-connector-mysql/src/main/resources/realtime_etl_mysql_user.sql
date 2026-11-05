CREATE TABLE `realtime_etl_user` (
  `ID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
  `CREATE_TIME` bigint NOT NULL COMMENT 'Created at',
  `UPDATE_TIME` bigint NOT NULL COMMENT 'Updated at',
  `USERNAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Account',
  `PASSWORD` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Password',
  `NICKNAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Nickname',
  `ROLE` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Role',
  `LOGIN_FAIL_COUNT` Int NOT NULL DEFAULT 0 COMMENT 's number of consecutive login failures ',
  `LOCK_UNTIL` Bigint NOT NULL DEFAULT 0 COMMENT 'lock deadline (ms, 0 unlocked) ',
  PRIMARY KEY (`ID`) USING BTREE,
  UNIQUE KEY `UK_USERNAME` (`USERNAME`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'User Profile' ROW_FORMAT = Dynamic;
