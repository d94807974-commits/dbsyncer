CREATE TABLE `dbsyncer_user_role`
(
    `ID`          varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Unique ID',
    `CREATE_TIME` bigint                                                NOT NULL COMMENT 'Created at',
    `UPDATE_TIME` bigint                                                NOT NULL COMMENT 'Updated at',
    `USER_ID`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'User ID',
    `ROLE_ID`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Role 42',
    PRIMARY KEY (`ID`) USING BTREE,
    UNIQUE KEY `UK_USER_ROLE` (`USER_ID`, `ROLE_ID`) USING BTREE,
    KEY           `IDX_UR_ROLE` (`ROLE_ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'User Role Table' ROW_FORMAT = Dynamic;


