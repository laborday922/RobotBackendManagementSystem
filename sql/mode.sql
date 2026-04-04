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
-- 1、机器人分组表
-- ----------------------------
DROP TABLE IF EXISTS `robot_groups`;
CREATE TABLE `robot_groups` (
                                `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '分组ID',
                                `name` varchar(50) NOT NULL COMMENT '分组名称',
                                `description` varchar(255) DEFAULT '' COMMENT '分组描述',
                                `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_name` (`name`) COMMENT '分组名称唯一'
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人分组表';

-- ----------------------------
-- Records of robot_groups
-- ----------------------------
BEGIN;
INSERT INTO `robot_groups` VALUES (1, '配送组', '负责物品配送的机器人组', '2026-03-19 10:28:17', '2026-03-19 10:28:17');
INSERT INTO `robot_groups` VALUES (2, '清洁组', '负责清洁任务的机器人组', '2026-03-19 10:28:17', '2026-03-19 10:28:17');
INSERT INTO `robot_groups` VALUES (3, '巡检组', '负责巡逻巡检的机器人组', '2026-03-19 10:28:17', '2026-03-19 10:28:17');
INSERT INTO `robot_groups` VALUES (4, '导览组', '负责智能导览和导航服务的机器人组', '2026-04-02 00:28:54', '2026-04-02 00:28:54');
COMMIT;

-- ----------------------------
-- 2、机器人基础信息表
-- ----------------------------
DROP TABLE IF EXISTS `robots`;
CREATE TABLE `robots` (
                          `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '机器人ID',
                          `code` varchar(30) NOT NULL COMMENT '机器人编号（唯一标识）',
                          `name` varchar(50) NOT NULL COMMENT '机器人名称',
                          `group_id` int unsigned DEFAULT NULL COMMENT '所属分组ID',
                          `manufacturer` varchar(50) DEFAULT '' COMMENT '生产厂家',
                          `production_date` date DEFAULT NULL COMMENT '生产日期',
                          `area` varchar(50) DEFAULT '' COMMENT '所属区域',
                          `status` tinyint unsigned NOT NULL DEFAULT '2' COMMENT '在线状态（0-离线，1-在线，2-待激活）',
                          `hardware_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '硬件状态（0-正常，1-警告，2-故障）',
                          `task_status` tinyint unsigned NOT NULL DEFAULT '2' COMMENT '任务状态（0-执行中，1-充电中，2-闲置，3-维护）',
                          `battery` tinyint unsigned NOT NULL DEFAULT '100' COMMENT '当前电量（0-100）',
                          `current_mode` int DEFAULT NULL COMMENT '当前模式ID',
                          `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
                          `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                          `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                          `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          `idle_start_time` datetime DEFAULT NULL COMMENT '空闲开始时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_code` (`code`),
                          KEY `idx_current_mode` (`current_mode`),
                          KEY `idx_group_id` (`group_id`),
                          KEY `idx_status` (`status`),
                          KEY `idx_task_status` (`task_status`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人基础信息表';

-- ----------------------------
-- Records of robots
-- ----------------------------
BEGIN;
INSERT INTO `robots` VALUES (1, 'ROB001', '小旋1号', 1, '极智科技', '2024-01-10', '药房', 0, 0, 2, 85, 1, '0', 'admin', '2026-03-02 20:37:52', 'admin', '2026-03-02 20:37:52', NULL);
INSERT INTO `robots` VALUES (2, 'ROB002', '小旋2号', 1, '极智科技', '2024-01-15', '病房区', 1, 0, 2, 12, 1, '0', 'admin', '2026-03-02 20:37:52', 'admin', '2026-03-31 09:20:56', '2026-03-31 09:20:56');
INSERT INTO `robots` VALUES (3, 'ROB003', '大白1号', 2, '清洁科技', '2023-12-01', '门诊大厅', 2, 0, 2, 76, 1, '0', 'admin', '2026-03-02 20:37:52', 'admin', '2026-03-30 16:34:33', '2026-03-30 16:34:33');
INSERT INTO `robots` VALUES (4, 'ROB004', '安巡1号', 3, '安防科技', '2024-02-20', '走廊', 0, 0, 3, 30, 2, '0', 'admin', '2026-03-02 20:37:52', 'admin', '2026-03-02 20:37:52', NULL);
INSERT INTO `robots` VALUES (5, 'ROB005', '闪电侠', 1, '极智科技', '2024-03-01', '监控室', 1, 0, 2, 95, 1, '0', 'admin', '2026-03-02 20:37:52', 'admin', '2026-03-31 08:50:44', '2026-03-31 08:50:44');
INSERT INTO `robots` VALUES (6, 'ROB006', '小黄人', 1, '清洁科技', '2024-01-05', '物流分拣中心', 1, 0, 2, 60, 1, '0', 'admin', '2026-03-30 21:36:44', 'admin', '2026-03-30 21:36:44', '2026-03-30 21:36:44');
INSERT INTO `robots` VALUES (7, 'ROB007', '巡警1号', 2, '安防科技', '2023-11-11', '东区巡逻路线', 0, 0, 1, 45, 3, '0', 'admin', '2026-03-30 21:36:44', 'admin', '2026-03-30 21:36:44', NULL);
INSERT INTO `robots` VALUES (8, 'ROB008', '引路人', 4, '极智科技', '2024-03-15', '门诊大厅导诊台', 2, 0, 2, 95, 1, '0', 'admin', '2026-03-30 21:36:44', 'admin', '2026-03-31 00:37:14', '2026-03-31 00:37:14');
INSERT INTO `robots` VALUES (9, 'ROB009', '解说员', 4, '极智科技', '2024-03-20', '院史馆/文化长廊', 1, 0, 2, 88, 1, '0', 'admin', '2026-03-30 21:36:44', 'admin', '2026-03-30 21:36:44', '2026-03-30 21:36:44');
COMMIT;

-- ----------------------------
-- 3、机器人状态预警表
-- ----------------------------
DROP TABLE IF EXISTS `robot_warnings`;
CREATE TABLE `robot_warnings` (
                                  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '预警ID',
                                  `robot_id` bigint unsigned NOT NULL COMMENT '关联机器人ID',
                                  `warning_type` tinyint unsigned NOT NULL COMMENT '预警类型（0-低电量，1-硬件故障，2-硬件异常，3-离线）',
                                  `warning_content` varchar(255) NOT NULL COMMENT '预警描述内容',
                                  `warning_level` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '预警级别（0-提示，1-警告，2-错误）',
                                  `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '预警状态（0-待处理，1-已解决）',
                                  `resolve_time` datetime DEFAULT NULL COMMENT '处理完成时间',
                                  `resolve_user` varchar(50) DEFAULT '' COMMENT '处理人',
                                  `resolve_note` varchar(255) DEFAULT '' COMMENT '处理备注',
                                  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预警创建时间',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人状态预警表';

-- ----------------------------
-- Records of robot_warnings
-- ----------------------------
BEGIN;
INSERT INTO `robot_warnings` VALUES (1, 2, 0, '机器人小旋2号电量低于15%，当前12%', 2, 0, NULL, '', '', '2025-03-19 09:15:00');
INSERT INTO `robot_warnings` VALUES (2, 3, 1, '清洁电机异常，需要维护', 1, 1, '2025-03-19 10:30:00', 'admin', '已重启清洁电机', '2025-03-19 08:20:00');
INSERT INTO `robot_warnings` VALUES (3, 3, 2, '传感器读数漂移', 1, 0, NULL, '', '', '2025-03-19 11:00:00');
INSERT INTO `robot_warnings` VALUES (4, 4, 2, '离线超过30分钟', 2, 0, NULL, '', '', '2025-03-19 07:00:00');
INSERT INTO `robot_warnings` VALUES (5, 4, 1, '底盘电机故障', 2, 0, NULL, '', '', '2025-03-19 07:05:00');
INSERT INTO `robot_warnings` VALUES (6, 7, 2, '摄像头故障，无法获取图像', 2, 0, NULL, '', '', '2025-03-19 12:00:00');
INSERT INTO `robot_warnings` VALUES (7, 7, 0, '电量低于50%', 1, 1, '2025-03-19 13:00:00', 'system', '已自动充电', '2025-03-19 12:30:00');
COMMIT;

-- ----------------------------
-- 4、机器人位置历史信息表
-- ----------------------------
DROP TABLE IF EXISTS `robot_position_history`;
CREATE TABLE `robot_position_history` (
                                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
                                          `robot_id` bigint unsigned NOT NULL COMMENT '机器人ID',
                                          `record_time` datetime NOT NULL COMMENT '记录时间',
                                          `location_area` varchar(50) NOT NULL COMMENT '位置区域（如：药房、走廊、病房区）',
                                          `specific_location` varchar(100) NOT NULL COMMENT '具体位置（如：药房存储区、1号楼3层走廊）',
                                          `coordinate_x` decimal(10,2) NOT NULL COMMENT '坐标X',
                                          `coordinate_y` decimal(10,2) NOT NULL COMMENT '坐标Y',
                                          `move_speed` decimal(5,2) NOT NULL COMMENT '移动速度（单位：m/s）',
                                          `status_desc` varchar(200) NOT NULL COMMENT '状态描述（含主状态和子状态）',
                                          PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人位置历史信息表';

-- ----------------------------
-- Records of robot_position_history
-- ----------------------------
BEGIN;
INSERT INTO `robot_position_history` VALUES (1, 1, '2026-03-19 00:24:26', '急诊科', '急诊药房', 10.50, 20.30, 1.20, '待命 准备执行配送任务');
COMMIT;

-- ----------------------------
-- 5、地图表
-- ----------------------------
DROP TABLE IF EXISTS `sys_map`;
CREATE TABLE `sys_map` (
                           `map_id` int NOT NULL AUTO_INCREMENT COMMENT '地图ID',
                           `map_name` varchar(100) NOT NULL COMMENT '地图名称',
                           `map_type` varchar(50) DEFAULT NULL COMMENT '地图类型',
                           `map_path` varchar(255) DEFAULT NULL COMMENT '地图路径',
                           `thumbnail` varchar(255) DEFAULT NULL COMMENT '缩略图路径',
                           `description` text COMMENT '地图描述',
                           `map_file` varchar(255) DEFAULT NULL COMMENT '地图文件路径',
                           `point_count` int DEFAULT '0' COMMENT '点位数量',
                           `version` varchar(20) DEFAULT '1.0' COMMENT '地图版本',
                           `status` char(1) DEFAULT '1' COMMENT '状态（0禁用 1启用）',
                           `is_enable` tinyint(1) DEFAULT '1' COMMENT '是否启用（0否 1是）',
                           `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
                           `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                           `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                           `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                           `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                           `remark` varchar(255) DEFAULT NULL COMMENT '备注信息',
                           PRIMARY KEY (`map_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='地图表';

-- ----------------------------
-- Records of sys_map
-- ----------------------------
BEGIN;
INSERT INTO `sys_map` VALUES (1, '一楼大厅', NULL, NULL, NULL, NULL, '/uploads/map/60c6917fb0424bc4a308ef7c17e67316.png', 30, '1.0', '1', 1, '0', '', NULL, '', '2026-03-25 17:43:13', NULL);
INSERT INTO `sys_map` VALUES (2, '二楼社保专区', NULL, NULL, NULL, NULL, NULL, 25, '1.0', '1', 1, '0', '', NULL, '', NULL, NULL);
COMMIT;

-- ----------------------------
-- 6、点位表
-- ----------------------------
DROP TABLE IF EXISTS `sys_point`;
CREATE TABLE `sys_point` (
                             `point_id` int NOT NULL AUTO_INCREMENT COMMENT '点位ID',
                             `map_id` int NOT NULL COMMENT '所属地图ID',
                             `point_name` varchar(50) NOT NULL COMMENT '点位名称',
                             `point_code` varchar(50) DEFAULT NULL COMMENT '点位编码',
                             `point_type` varchar(20) DEFAULT 'normal' COMMENT '点位类型(normal/vip/service/exit)',
                             `coordinate_x` decimal(10,2) DEFAULT NULL COMMENT 'X坐标',
                             `coordinate_y` decimal(10,2) DEFAULT NULL COMMENT 'Y坐标',
                             `status` char(1) DEFAULT '1' COMMENT '状态（0禁用 1启用）',
                             `order_num` int DEFAULT '0' COMMENT '显示顺序',
                             `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                             `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                             `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                             `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                             `remark` varchar(255) DEFAULT NULL COMMENT '备注信息',
                             `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
                             PRIMARY KEY (`point_id`),
                             KEY `idx_map_id` (`map_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点位表';

-- ----------------------------
-- Records of sys_point
-- ----------------------------
BEGIN;
INSERT INTO `sys_point` VALUES (1, 1, '社保窗口', 'P001', 'normal', NULL, NULL, '1', 1, '', NULL, '', NULL, NULL, '0');
INSERT INTO `sys_point` VALUES (2, 1, '不动产窗口', 'P002', 'normal', NULL, NULL, '1', 2, '', NULL, '', NULL, NULL, '0');
INSERT INTO `sys_point` VALUES (3, 1, '填表区', 'P003', 'normal', NULL, NULL, '1', 3, '', NULL, '', NULL, NULL, '0');
INSERT INTO `sys_point` VALUES (4, 1, '自助机', 'P004', 'normal', NULL, NULL, '1', 4, '', NULL, '', NULL, NULL, '0');
INSERT INTO `sys_point` VALUES (5, 1, '咨询台', 'P005', 'normal', NULL, NULL, '1', 5, '', NULL, '', NULL, NULL, '0');
INSERT INTO `sys_point` VALUES (6, 1, '休息区', 'P006', 'normal', NULL, NULL, '1', 6, '', NULL, '', NULL, NULL, '0');
INSERT INTO `sys_point` VALUES (7, 2, '1', '', 'normal', 0.00, 0.00, '1', 1, NULL, '2026-03-26 09:18:13', '', NULL, '', '0');
COMMIT;

-- ----------------------------
-- 7、模式主表
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
                            PRIMARY KEY (`mode_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模式主表';

-- ----------------------------
-- Records of sys_mode
-- ----------------------------
BEGIN;
INSERT INTO `sys_mode` VALUES (1, '待机模式', 'system', 1, '#95a5a6', 'fas fa-pause-circle', '机器人进入低功耗状态，等待唤醒指令', '1', 128, 15, 1, '0', '', '2026-03-02 20:37:52', '', '2026-03-31 09:21:42');
INSERT INTO `sys_mode` VALUES (2, '维护模式', 'system', 1, '#f1c40f', 'fas fa-tools', '进行系统维护和调试，暂停所有任务', '1', 89, 20, 2, '0', '', '2026-03-02 20:37:52', '', '2026-03-31 08:47:07');
INSERT INTO `sys_mode` VALUES (3, '充电模式', 'system', 1, '#2ecc71', 'fas fa-bolt', '低电量时自动返回充电桩充电', '1', 156, 20, 3, '0', '', '2026-03-02 20:37:52', '', '2026-03-31 08:49:10');
INSERT INTO `sys_mode` VALUES (7, '1', 'custom', 2, '#1890ff', 'fa fa-cog', NULL, '1', 0, 0, 0, '0', '', '2026-03-31 09:21:48', '', NULL);
COMMIT;

-- ----------------------------
-- 8、模式参数表
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
                                  PRIMARY KEY (`param_id`),
                                  KEY `idx_mode_id` (`mode_id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模式参数表';

-- ----------------------------
-- Records of sys_mode_param
-- ----------------------------
BEGIN;
INSERT INTO `sys_mode_param` VALUES (26, 2, '维护人员权限', 'select', NULL, 'option1', '[{\"label\":\"基础权限\",\"value\":\"option1\"},{\"label\":\"高级权限\",\"value\":\"option2\"},{\"label\":\"管理员权限\",\"value\":\"option3\"}]', 0, 100, '', 0, '0', '', '2026-03-31 08:47:07', '', NULL);
INSERT INTO `sys_mode_param` VALUES (27, 2, '维护时间', 'number', NULL, '0', NULL, 0, 100, '分钟', 0, '0', '', '2026-03-31 08:47:07', '', NULL);
INSERT INTO `sys_mode_param` VALUES (28, 2, '警告提醒', 'boolean', NULL, 'true', NULL, 0, 100, '', 0, '0', '', '2026-03-31 08:47:07', '', NULL);
INSERT INTO `sys_mode_param` VALUES (30, 3, '低电量阈值', 'range', NULL, '20', '[]', 0, 100, '%', 0, '0', '', '2026-03-31 08:49:10', '', NULL);
INSERT INTO `sys_mode_param` VALUES (31, 3, '充电优先级', 'select', NULL, 'option1', '[{\"label\":\"完成任务后充电\",\"value\":\"option1\"},{\"label\":\"立即充电\",\"value\":\"option2\"},{\"label\":\"定时充电\",\"value\":\"option3\"}]', 0, 100, '', 0, '0', '', '2026-03-31 08:49:10', '', NULL);
INSERT INTO `sys_mode_param` VALUES (32, 3, '充电完成提醒', 'boolean', NULL, 'true', NULL, 0, 100, '', 0, '0', '', '2026-03-31 08:49:10', '', NULL);
INSERT INTO `sys_mode_param` VALUES (33, 1, '唤醒触发条件', 'select', NULL, 'option1', '[{\"label\":\"手动唤醒\",\"value\":\"option1\"},{\"label\":\"定时唤醒\",\"value\":\"option2\"},{\"label\":\"远程唤醒\",\"value\":\"option3\"}]', 0, 100, '', 0, '0', '', '2026-03-31 09:21:43', '', NULL);
INSERT INTO `sys_mode_param` VALUES (34, 1, '唤醒电量阈值', 'range', NULL, '20', '[]', 0, 100, '%', 0, '0', '', '2026-03-31 09:21:43', '', NULL);
INSERT INTO `sys_mode_param` VALUES (35, 1, '低温保护', 'boolean', NULL, 'false', '[]', 0, 100, '', 0, '0', '', '2026-03-31 09:21:43', '', NULL);
INSERT INTO `sys_mode_param` VALUES (36, 1, '网络保持', 'boolean', NULL, 'false', '[]', 0, 100, '', 0, '0', '', '2026-03-31 09:21:43', '', NULL);
INSERT INTO `sys_mode_param` VALUES (37, 1, '1', 'string', NULL, '', NULL, 0, 100, '', 0, '0', '', '2026-03-31 09:21:43', '', NULL);
COMMIT;

-- ----------------------------
-- 9、模式切换历史记录表
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
                                    PRIMARY KEY (`history_id`),
                                    KEY `idx_operation_time` (`operation_time`),
                                    KEY `idx_robot_id` (`robot_id`),
                                    KEY `idx_operation_type` (`operation_type`)
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模式切换历史记录表';

-- ----------------------------
-- Records of sys_mode_history
-- ----------------------------
BEGIN;
INSERT INTO `sys_mode_history` VALUES (1, '2026-03-02 20:37:53', 'mode-switch', 1, '机器人A', 1, '待机模式', '切换到待机模式', 'admin', 'success', '', '2026-03-02 20:37:53');
INSERT INTO `sys_mode_history` VALUES (77, '2026-03-31 09:20:57', 'mode-switch', 2, '小旋2号', 2, '维护模式', '切换到维护模式', 'admin', 'success', '', '2026-03-31 09:20:56');
INSERT INTO `sys_mode_history` VALUES (78, '2026-04-02 00:50:45', 'emergency_stop', 1, '小旋1号', NULL, NULL, '已对 1 个机器人执行紧急停止操作', 'admin', 'success', '', '2026-04-02 00:50:45');
COMMIT;

-- ----------------------------
-- 10、模式排程表
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
                                     PRIMARY KEY (`schedule_id`),
                                     KEY `idx_mode_id` (`mode_id`),
                                     KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模式排程表';

-- ----------------------------
-- Records of sys_mode_schedule
-- ----------------------------
BEGIN;
INSERT INTO `sys_mode_schedule` VALUES (1, '夜间充电计划', 3, '充电模式', '每天 22:00', 'daily', '2024-01-01', '22:00:00', 2.00, 'paused', NULL, NULL, '0', '', '2026-03-02 20:37:53', '', NULL);
INSERT INTO `sys_mode_schedule` VALUES (2, '午间待机计划', 1, '待机模式', '工作日 12:00-14:00', 'weekdays', '2024-01-01', '12:00:00', 2.00, 'running', NULL, NULL, '0', '', '2026-03-02 20:37:53', '', NULL);
INSERT INTO `sys_mode_schedule` VALUES (3, '每周维护计划', 2, '维护模式', '每周一 08:00', 'weekly', '2024-01-01', '08:00:00', 1.00, 'pending', NULL, NULL, '0', '', '2026-03-02 20:37:53', '', NULL);
INSERT INTO `sys_mode_schedule` VALUES (6, '1', 1, NULL, '2026-04-01 08:00', 'daily', '2026-04-01', '08:00:00', 2.00, 'pending', NULL, NULL, '0', '', '2026-03-31 09:21:16', '', NULL);
COMMIT;

-- ----------------------------
-- 11、排程机器人关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_schedule_robot`;
CREATE TABLE `sys_schedule_robot` (
                                      `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                      `schedule_id` int NOT NULL COMMENT '排程ID',
                                      `robot_id` int NOT NULL COMMENT '机器人ID',
                                      `robot_name` varchar(100) DEFAULT NULL COMMENT '机器人名称',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_schedule_id` (`schedule_id`),
                                      KEY `idx_robot_id` (`robot_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='排程机器人关联表';

-- ----------------------------
-- Records of sys_schedule_robot
-- ----------------------------
BEGIN;
INSERT INTO `sys_schedule_robot` VALUES (1, 1, 1, '机器人A');
INSERT INTO `sys_schedule_robot` VALUES (2, 1, 2, '机器人B');
INSERT INTO `sys_schedule_robot` VALUES (3, 1, 4, '机器人D');
INSERT INTO `sys_schedule_robot` VALUES (4, 2, 1, '机器人A');
INSERT INTO `sys_schedule_robot` VALUES (5, 2, 3, '机器人C');
INSERT INTO `sys_schedule_robot` VALUES (6, 3, 2, '机器人B');
INSERT INTO `sys_schedule_robot` VALUES (7, 3, 5, '机器人E');
INSERT INTO `sys_schedule_robot` VALUES (12, 6, 1, '小旋1号');
INSERT INTO `sys_schedule_robot` VALUES (13, 6, 2, '小旋2号');
COMMIT;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;