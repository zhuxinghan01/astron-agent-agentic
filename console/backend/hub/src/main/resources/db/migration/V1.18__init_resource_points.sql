CREATE TABLE IF NOT EXISTS `resource_record`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT 'Primary key ID',
    `uid`          varchar(128) NOT NULL COMMENT 'User UID',
    `resource_type` varchar(32) NOT NULL COMMENT 'Resource type',
    `source_type`  varchar(32)  NOT NULL COMMENT 'Resource source type',
    `total_amount` bigint       NOT NULL DEFAULT 0 COMMENT 'Granted amount',
    `used_amount`  bigint       NOT NULL DEFAULT 0 COMMENT 'Used amount',
    `remain_amount` bigint      NOT NULL DEFAULT 0 COMMENT 'Remaining amount',
    `grant_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Grant time',
    `expire_time`  datetime              DEFAULT NULL COMMENT 'Expire time',
    `status`       tinyint      NOT NULL DEFAULT 1 COMMENT '1-valid, 2-used_up, 4-invalid',
    `version`      bigint       NOT NULL DEFAULT 0 COMMENT 'Manual optimistic lock version',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (`id`),
    KEY            `idx_resource_record_uid_type_status_expire` (`uid`, `resource_type`, `status`, `expire_time`),
    KEY            `idx_resource_record_uid_type_source_status` (`uid`, `resource_type`, `source_type`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='Resource balance batches';

CREATE TABLE IF NOT EXISTS `resource_flow`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT 'Primary key ID',
    `flow_no`       varchar(64)  NOT NULL COMMENT 'Flow number',
    `uid`           varchar(128) NOT NULL COMMENT 'User UID',
    `resource_type` varchar(32)  NOT NULL COMMENT 'Resource type',
    `record_id`     bigint                DEFAULT NULL COMMENT 'Related resource record ID',
    `direction`     tinyint      NOT NULL COMMENT '1-add, 2-deduct, 3-failure, 4-refund',
    `amount`        bigint       NOT NULL DEFAULT 0 COMMENT 'Changed amount',
    `request_id`    varchar(128)          DEFAULT NULL COMMENT 'Request id',
    `operator_name` varchar(128)          DEFAULT NULL COMMENT 'Operator name',
    `reason`        varchar(255)          DEFAULT NULL COMMENT 'Reason',
    `remark`        varchar(512)          DEFAULT NULL COMMENT 'Remark',
    `visible`       tinyint      NOT NULL DEFAULT 1 COMMENT '1-visible, 0-hidden',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY      `uk_resource_flow_no` (`flow_no`),
    KEY             `idx_resource_flow_uid_type_time` (`uid`, `resource_type`, `create_time`),
    KEY             `idx_resource_flow_request_id` (`request_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='Resource audit flows';

CREATE TABLE IF NOT EXISTS `resource_request`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT 'Primary key ID',
    `uid`            varchar(128) NOT NULL COMMENT 'User UID',
    `resource_type`  varchar(32)  NOT NULL COMMENT 'Resource type',
    `request_id`     varchar(128) NOT NULL COMMENT 'Request id',
    `request_type`   varchar(32)  NOT NULL COMMENT 'Request type',
    `biz_id`         varchar(128)          DEFAULT NULL COMMENT 'Business id',
    `operation_type` varchar(32)  NOT NULL COMMENT 'Operation type',
    `status`         tinyint      NOT NULL DEFAULT 0 COMMENT '0-processing, 1-success, 2-failed',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY       `uk_resource_request` (`resource_type`, `request_type`, `request_id`),
    KEY              `idx_resource_request_uid_type_status` (`uid`, `resource_type`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='Resource idempotent requests';

CREATE TABLE IF NOT EXISTS `resource_deduct_detail`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT 'Primary key ID',
    `flow_id`       bigint       NOT NULL COMMENT 'Deduct flow id',
    `record_id`     bigint       NOT NULL COMMENT 'Resource record id',
    `uid`           varchar(128) NOT NULL COMMENT 'User UID',
    `resource_type` varchar(32)  NOT NULL COMMENT 'Resource type',
    `amount`        bigint       NOT NULL DEFAULT 0 COMMENT 'Deducted amount from the record',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    PRIMARY KEY (`id`),
    KEY             `idx_resource_deduct_detail_flow_id` (`flow_id`),
    KEY             `idx_resource_deduct_detail_record_id` (`record_id`),
    KEY             `idx_resource_deduct_detail_uid_type` (`uid`, `resource_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='Resource deduct details';
