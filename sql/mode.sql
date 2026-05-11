-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: localhost    Database: ry-vue
-- ------------------------------------------------------
-- Server version	8.0.38

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- ----------------------------
-- 1、模式主表
-- ----------------------------
DROP TABLE IF EXISTS `sys_mode`;
CREATE TABLE `sys_mode` (
                            `mode_id` int NOT NULL AUTO_INCREMENT COMMENT '模式ID',
                            `mode_name` varchar(100) NOT NULL COMMENT '模式名称',
                            `mode_type` varchar(50) DEFAULT 'custom' COMMENT '模式类型(system系统/custom自定义)',
                            `category_id` int DEFAULT NULL COMMENT '分类ID',
                            `mode_color` varchar(20) DEFAULT '#1890ff' COMMENT '模式颜色',
                            `mode_icon` varchar(100) DEFAULT 'fa fa-robot' COMMENT '模式图标',
                            `description` varchar(500) DEFAULT NULL COMMENT '模式描述',
                            `enabled` char(1) DEFAULT '1' COMMENT '是否启用（0禁用 1启用）',
                            `usage_count` int DEFAULT '0' COMMENT '使用次数',
                            `robot_count` int DEFAULT '0' COMMENT '关联机器人数量',
                            `order_num` int DEFAULT '0' COMMENT '显示顺序',
                            `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                            `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
                            PRIMARY KEY (`mode_id`),
                            KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模式主表';

-- ----------------------------
-- Records of sys_mode
-- ----------------------------
BEGIN;
INSERT INTO `sys_mode` VALUES (1, '待机模式', 'system', 1, '#95a5a6', 'fas fa-pause-circle', '机器人进入低功耗状态，等待唤醒指令', '1', 128, 15, 1, '0', '', '2026-03-02 20:37:52', '', '2026-03-31 09:21:42', 0);
INSERT INTO `sys_mode` VALUES (2, '维护模式', 'system', 1, '#f1c40f', 'fas fa-tools', '进行系统维护和调试，暂停所有任务', '1', 89, 20, 2, '0', '', '2026-03-02 20:37:52', '', '2026-03-31 08:47:07', 0);
INSERT INTO `sys_mode` VALUES (3, '充电模式', 'system', 1, '#2ecc71', 'fas fa-bolt', '低电量时自动返回充电桩充电', '1', 156, 20, 3, '0', '', '2026-03-02 20:37:52', '', '2026-03-31 08:49:10', 0);
INSERT INTO `sys_mode` VALUES (7, '1', 'custom', 2, '#1890ff', 'fa fa-cog', NULL, '1', 0, 0, 0, '0', '', '2026-03-31 09:21:48', '', NULL, 0);
COMMIT;

-- ----------------------------
-- 2、模式参数表
-- ----------------------------
DROP TABLE IF EXISTS `sys_mode_param`;
CREATE TABLE `sys_mode_param` (
                                  `param_id` int NOT NULL AUTO_INCREMENT COMMENT '参数ID',
                                  `mode_id` int NOT NULL COMMENT '所属模式ID',
                                  `param_name` varchar(100) NOT NULL COMMENT '参数名称',
                                  `param_type` varchar(20) DEFAULT 'string' COMMENT '参数类型(string文本/number数字/boolean开关/select下拉/range滑块)',
                                  `param_description` varchar(200) DEFAULT NULL COMMENT '参数描述',
                                  `param_value` text COMMENT '参数默认值',
                                  `param_options` text COMMENT '选项配置(JSON格式)',
                                  `param_min` int DEFAULT NULL COMMENT '最小值',
                                  `param_max` int DEFAULT NULL COMMENT '最大值',
                                  `param_unit` varchar(20) DEFAULT NULL COMMENT '单位',
                                  `order_num` int DEFAULT '0' COMMENT '显示顺序',
                                  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                                  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
                                  PRIMARY KEY (`param_id`),
                                  KEY `idx_mode_id` (`mode_id`),
                                  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模式参数表';

-- ----------------------------
-- Records of sys_mode_param
-- ----------------------------
BEGIN;
INSERT INTO `sys_mode_param` VALUES (26, 2, '维护人员权限', 'select', NULL, 'option1', '[{\"label\":\"基础权限\",\"value\":\"option1\"},{\"label\":\"高级权限\",\"value\":\"option2\"},{\"label\":\"管理员权限\",\"value\":\"option3\"}]', 0, 100, '', 0, '0', '', '2026-03-31 08:47:07', '', NULL, 0);
INSERT INTO `sys_mode_param` VALUES (27, 2, '维护时间', 'number', NULL, '0', NULL, 0, 100, '分钟', 0, '0', '', '2026-03-31 08:47:07', '', NULL, 0);
INSERT INTO `sys_mode_param` VALUES (28, 2, '警告提醒', 'boolean', NULL, 'true', NULL, 0, 100, '', 0, '0', '', '2026-03-31 08:47:07', '', NULL, 0);
INSERT INTO `sys_mode_param` VALUES (30, 3, '低电量阈值', 'range', NULL, '20', '[]', 0, 100, '%', 0, '0', '', '2026-03-31 08:49:10', '', NULL, 0);
INSERT INTO `sys_mode_param` VALUES (31, 3, '充电优先级', 'select', NULL, 'option1', '[{\"label\":\"完成任务后充电\",\"value\":\"option1\"},{\"label\":\"立即充电\",\"value\":\"option2\"},{\"label\":\"定时充电\",\"value\":\"option3\"}]', 0, 100, '', 0, '0', '', '2026-03-31 08:49:10', '', NULL, 0);
INSERT INTO `sys_mode_param` VALUES (32, 3, '充电完成提醒', 'boolean', NULL, 'true', NULL, 0, 100, '', 0, '0', '', '2026-03-31 08:49:10', '', NULL, 0);
INSERT INTO `sys_mode_param` VALUES (33, 1, '唤醒触发条件', 'select', NULL, 'option1', '[{\"label\":\"手动唤醒\",\"value\":\"option1\"},{\"label\":\"定时唤醒\",\"value\":\"option2\"},{\"label\":\"远程唤醒\",\"value\":\"option3\"}]', 0, 100, '', 0, '0', '', '2026-03-31 09:21:43', '', NULL, 0);
INSERT INTO `sys_mode_param` VALUES (34, 1, '唤醒电量阈值', 'range', NULL, '20', '[]', 0, 100, '%', 0, '0', '', '2026-03-31 09:21:43', '', NULL, 0);
INSERT INTO `sys_mode_param` VALUES (35, 1, '低温保护', 'boolean', NULL, 'false', '[]', 0, 100, '', 0, '0', '', '2026-03-31 09:21:43', '', NULL, 0);
INSERT INTO `sys_mode_param` VALUES (36, 1, '网络保持', 'boolean', NULL, 'false', '[]', 0, 100, '', 0, '0', '', '2026-03-31 09:21:43', '', NULL, 0);
INSERT INTO `sys_mode_param` VALUES (37, 1, '1', 'string', NULL, '', NULL, 0, 100, '', 0, '0', '', '2026-03-31 09:21:43', '', NULL, 0);
COMMIT;

-- ----------------------------
-- 3、模式切换历史记录表
-- ----------------------------
DROP TABLE IF EXISTS `sys_mode_history`;
CREATE TABLE `sys_mode_history` (
                                    `history_id` int NOT NULL AUTO_INCREMENT COMMENT '历史ID',
                                    `operation_time` datetime DEFAULT NULL COMMENT '操作时间',
                                    `operation_type` varchar(50) DEFAULT NULL COMMENT '操作类型(mode-switch/config-change/alert/emergency)',
                                    `robot_id` int DEFAULT NULL COMMENT '机器人ID',
                                    `robot_name` varchar(100) DEFAULT NULL COMMENT '机器人名称',
                                    `mode_id` int DEFAULT NULL COMMENT '模式ID',
                                    `mode_name` varchar(100) DEFAULT NULL COMMENT '模式名称',
                                    `content` varchar(500) DEFAULT NULL COMMENT '操作内容',
                                    `operator` varchar(50) DEFAULT NULL COMMENT '操作人',
                                    `status` varchar(20) DEFAULT 'success' COMMENT '状态(success/warning/danger)',
                                    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                    `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
                                    PRIMARY KEY (`history_id`),
                                    KEY `idx_operation_time` (`operation_time`),
                                    KEY `idx_robot_id` (`robot_id`),
                                    KEY `idx_operation_type` (`operation_type`),
                                    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模式切换历史记录表';

-- ----------------------------
-- Records of sys_mode_history
-- ----------------------------
BEGIN;
INSERT INTO `sys_mode_history` VALUES (1, '2026-03-02 20:37:53', 'mode-switch', 1, '机器人A', 1, '待机模式', '切换到待机模式', 'admin', 'success', '', '2026-03-02 20:37:53', 0);
INSERT INTO `sys_mode_history` VALUES (77, '2026-03-31 09:20:57', 'mode-switch', 2, '小旋2号', 2, '维护模式', '切换到维护模式', 'admin', 'success', '', '2026-03-31 09:20:56', 0);
INSERT INTO `sys_mode_history` VALUES (78, '2026-04-02 00:50:45', 'emergency_stop', 1, '小旋1号', NULL, NULL, '已对 1 个机器人执行紧急停止操作', 'admin', 'success', '', '2026-04-02 00:50:45', 0);
COMMIT;

-- ----------------------------
-- 4、模式排程表
-- ----------------------------
DROP TABLE IF EXISTS `sys_mode_schedule`;
CREATE TABLE `sys_mode_schedule` (
                                     `schedule_id` int NOT NULL AUTO_INCREMENT COMMENT '排程ID',
                                     `schedule_name` varchar(100) NOT NULL COMMENT '排程名称',
                                     `mode_id` int DEFAULT NULL COMMENT '模式ID',
                                     `mode_name` varchar(100) DEFAULT NULL COMMENT '模式名称',
                                     `execute_time` varchar(100) DEFAULT NULL COMMENT '执行时间描述',
                                     `repeat_type` varchar(20) DEFAULT 'once' COMMENT '重复类型(once/daily/weekly/monthly/weekdays)',
                                     `start_date` date DEFAULT NULL COMMENT '开始日期',
                                     `start_time` time DEFAULT NULL COMMENT '开始时间',
                                     `duration` decimal(5,2) DEFAULT '2.00' COMMENT '持续时间(小时)',
                                     `status` varchar(20) DEFAULT 'pending' COMMENT '状态(running/paused/pending/completed/failed)',
                                     `last_execute_time` datetime DEFAULT NULL COMMENT '上次执行时间',
                                     `last_execute_status` varchar(20) DEFAULT NULL COMMENT '上次执行状态',
                                     `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                                     `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                     `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                     `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                     `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
                                     PRIMARY KEY (`schedule_id`),
                                     KEY `idx_mode_id` (`mode_id`),
                                     KEY `idx_status` (`status`),
                                     KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模式排程表';

-- ----------------------------
-- Records of sys_mode_schedule
-- ----------------------------
BEGIN;
INSERT INTO `sys_mode_schedule` VALUES (1, '夜间充电计划', 3, '充电模式', '每天 22:00', 'daily', '2024-01-01', '22:00:00', 2.00, 'paused', NULL, NULL, '0', '', '2026-03-02 20:37:53', '', NULL, 0);
INSERT INTO `sys_mode_schedule` VALUES (2, '午间待机计划', 1, '待机模式', '工作日 12:00-14:00', 'weekdays', '2024-01-01', '12:00:00', 2.00, 'running', NULL, NULL, '0', '', '2026-03-02 20:37:53', '', NULL, 0);
INSERT INTO `sys_mode_schedule` VALUES (3, '每周维护计划', 2, '维护模式', '每周一 08:00', 'weekly', '2024-01-01', '08:00:00', 1.00, 'pending', NULL, NULL, '0', '', '2026-03-02 20:37:53', '', NULL, 0);
INSERT INTO `sys_mode_schedule` VALUES (6, '1', 1, NULL, '2026-04-01 08:00', 'daily', '2026-04-01', '08:00:00', 2.00, 'pending', NULL, NULL, '0', '', '2026-03-31 09:21:16', '', NULL, 0);
COMMIT;

-- ----------------------------
-- 5、排程机器人关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_schedule_robot`;
CREATE TABLE `sys_schedule_robot` (
                                      `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                      `schedule_id` int NOT NULL COMMENT '排程ID',
                                      `robot_id` int NOT NULL COMMENT '机器人ID',
                                      `robot_name` varchar(100) DEFAULT NULL COMMENT '机器人名称',
                                      `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_schedule_id` (`schedule_id`),
                                      KEY `idx_robot_id` (`robot_id`),
                                      KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='排程机器人关联表';

-- ----------------------------
-- Records of sys_schedule_robot
-- ----------------------------
BEGIN;
INSERT INTO `sys_schedule_robot` VALUES (1, 1, 1, '机器人A', 0);
INSERT INTO `sys_schedule_robot` VALUES (2, 1, 2, '机器人B', 0);
INSERT INTO `sys_schedule_robot` VALUES (3, 1, 4, '机器人D', 0);
INSERT INTO `sys_schedule_robot` VALUES (4, 2, 1, '机器人A', 0);
INSERT INTO `sys_schedule_robot` VALUES (5, 2, 3, '机器人C', 0);
INSERT INTO `sys_schedule_robot` VALUES (6, 3, 2, '机器人B', 0);
INSERT INTO `sys_schedule_robot` VALUES (7, 3, 5, '机器人E', 0);
INSERT INTO `sys_schedule_robot` VALUES (12, 6, 1, '小旋1号', 0);
INSERT INTO `sys_schedule_robot` VALUES (13, 6, 2, '小旋2号', 0);
COMMIT;

-- ----------------------------
-- 6、机器人模式扩展表
-- ----------------------------
DROP TABLE IF EXISTS `sys_robot_ext`;
CREATE TABLE `sys_robot_ext` (
                                 `robot_id` bigint NOT NULL COMMENT '机器人ID',
                                 `current_mode` bigint DEFAULT NULL COMMENT '当前模式ID',
                                 `current_mode_name` varchar(100) DEFAULT NULL COMMENT '当前模式名称',
                                 `last_mode_switch_time` datetime DEFAULT NULL COMMENT '最后模式切换时间',
                                 `mode_switch_count` int DEFAULT '0' COMMENT '模式切换次数',
                                 `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
                                 `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
                                 `need_auto_charge` tinyint DEFAULT '0' COMMENT '是否需要自动充电 0-否 1-是',
                                 PRIMARY KEY (`robot_id`),
                                 KEY `idx_tenant_id` (`tenant_id`),
                                 KEY `idx_current_mode` (`current_mode`),
                                 KEY `idx_sys_robot_ext_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人模式扩展表';

-- ----------------------------
-- 7、机器人模式配置表
-- ----------------------------
DROP TABLE IF EXISTS `sys_robot_mode_config`;
CREATE TABLE `sys_robot_mode_config` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                         `robot_id` bigint NOT NULL COMMENT '机器人ID',
                                         `mode_id` bigint NOT NULL COMMENT '模式ID',
                                         `config` json DEFAULT NULL COMMENT '配置参数(JSON格式)',
                                         `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
                                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `uk_robot_mode` (`robot_id`,`mode_id`),
                                         KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人模式配置表';

-- ----------------------------
-- 8、机器人操作记录表
-- ----------------------------
DROP TABLE IF EXISTS `sys_robot_operation`;
CREATE TABLE `sys_robot_operation` (
                                       `operation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '操作ID',
                                       `robot_id` bigint NOT NULL COMMENT '机器人ID',
                                       `robot_name` varchar(100) DEFAULT NULL COMMENT '机器人名称',
                                       `operation_type` varchar(50) DEFAULT NULL COMMENT '操作类型',
                                       `operation_result` varchar(20) DEFAULT NULL COMMENT '操作结果',
                                       `operation_time` datetime DEFAULT NULL COMMENT '操作时间',
                                       `operator` varchar(50) DEFAULT NULL COMMENT '操作人',
                                       `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                       `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
                                       `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       PRIMARY KEY (`operation_id`),
                                       KEY `idx_robot_id` (`robot_id`),
                                       KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人操作记录表';

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;