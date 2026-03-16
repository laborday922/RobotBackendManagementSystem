CREATE TABLE IF NOT EXISTS `robot_groups` (
                                              `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分组ID',
                                              `name` VARCHAR(50) NOT NULL COMMENT '分组名称',
                                              `description` VARCHAR(255) DEFAULT '' COMMENT '分组描述',
                                              `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                              PRIMARY KEY (`id`),
                                              UNIQUE KEY `uk_name` (`name`) COMMENT '分组名称唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机器人分组表';

CREATE TABLE IF NOT EXISTS `robots` (
                                        `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '机器人ID',
                                        `code` VARCHAR(30) NOT NULL COMMENT '机器人编号（唯一标识）',
                                        `name` VARCHAR(50) NOT NULL COMMENT '机器人名称',
                                        `group_id` INT UNSIGNED DEFAULT NULL COMMENT '所属分组ID（逻辑外键）',
                                        `manufacturer` VARCHAR(50) DEFAULT '' COMMENT '生产厂家',
                                        `production_date` DATE DEFAULT NULL COMMENT '生产日期',
                                        `area` VARCHAR(50) DEFAULT '' COMMENT '所属区域',
                                        `status` TINYINT UNSIGNED NOT NULL DEFAULT 2 COMMENT '在线状态（0-离线，1-在线，2-待激活）',
                                     `hardware_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '硬件状态（0-正常，1-警告，2-故障）',
                                       `task_status` TINYINT UNSIGNED NOT NULL DEFAULT 2 COMMENT '任务状态（0-执行中，1-充电中，2-闲置，3-维护）',
                                        `battery` TINYINT UNSIGNED NOT NULL DEFAULT 100 COMMENT '当前电量（0-100）',
                                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `idle_start_time` DATETIME DEFAULT NULL COMMENT '闲置开始时间（任务状态为闲置时记录）', -- 新增字段
                                        PRIMARY KEY (`id`),
                                        UNIQUE KEY `uk_code` (`code`) COMMENT '机器人编号唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机器人基础信息表';

CREATE TABLE IF NOT EXISTS `robot_warnings` (
                                                `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '预警ID',
                                                `robot_id` BIGINT UNSIGNED NOT NULL COMMENT '关联机器人ID（逻辑外键）',
                                                `warning_type` TINYINT UNSIGNED NOT NULL COMMENT '预警类型（0-低电量，1-硬件故障，2-硬件异常，3-离线）',
                                                `warning_content` VARCHAR(255) NOT NULL COMMENT '预警描述内容',
                                                `warning_level` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '预警级别（0-提示，1-警告，2-错误）',
                                                `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '预警状态（0-待处理，1-已解决）',
                                                `resolve_time` DATETIME DEFAULT NULL COMMENT '处理完成时间',
                                                `resolve_user` VARCHAR(50) DEFAULT '' COMMENT '处理人',
                                                `resolve_note` VARCHAR(255) DEFAULT '' COMMENT '处理备注',
                                                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预警创建时间',
                                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机器人状态预警表';

CREATE TABLE IF NOT EXISTS `robot_position_history` (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
                                        robot_id BIGINT UNSIGNED NOT NULL COMMENT '机器人ID',
                                        record_time DATETIME NOT NULL COMMENT '记录时间',
                                        location_area VARCHAR(50) NOT NULL COMMENT '位置区域（如：药房、走廊、病房区）',
                                        specific_location VARCHAR(100) NOT NULL COMMENT '具体位置（如：药房存储区、1号楼3层走廊）',
                                        coordinate_x DECIMAL(10, 2) NOT NULL COMMENT '坐标X',
                                        coordinate_y DECIMAL(10, 2) NOT NULL COMMENT '坐标Y',
                                        move_speed DECIMAL(5, 2) NOT NULL COMMENT '移动速度（单位：m/s）',
                                        status_desc VARCHAR(200) NOT NULL COMMENT '状态描述（含主状态和子状态，如：待命 准备执行配送任务）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机器人位置历史信息表';



