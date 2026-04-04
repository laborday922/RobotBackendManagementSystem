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
-- 12、业务接待配置表
-- ----------------------------
DROP TABLE IF EXISTS `sys_reception_config`;
CREATE TABLE `sys_reception_config` (
                                        `config_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
                                        `robot_id` int NOT NULL COMMENT '机器人ID',
                                        `welcome` varchar(300) DEFAULT NULL COMMENT '首次进入欢迎语',
                                        `repeat` varchar(300) DEFAULT NULL COMMENT '重复唤醒播报语',
                                        `idle` varchar(300) DEFAULT NULL COMMENT '未唤醒状态播报语',
                                        `vip_enabled` char(1) DEFAULT '1' COMMENT 'VIP识别开关（0关闭 1开启）',
                                        `vip_greeting` varchar(300) DEFAULT NULL COMMENT 'VIP识别语',
                                        `vip_multi` varchar(20) DEFAULT NULL COMMENT '多VIP识别语',
                                        `stranger_enabled` char(1) DEFAULT '1' COMMENT '陌生人识别开关（0关闭 1开启）',
                                        `stranger_greeting` varchar(300) DEFAULT NULL COMMENT '陌生人识别语',
                                        `stranger_multi` varchar(20) DEFAULT NULL COMMENT '多性别陌生人识别语',
                                        `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                        `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                        `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                        `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                        PRIMARY KEY (`config_id`),
                                        KEY `idx_robot_id` (`robot_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务接待配置表';

-- ----------------------------
-- Records of sys_reception_config
-- ----------------------------
BEGIN;
INSERT INTO `sys_reception_config` VALUES (1, 1, '请问关于公司：有哪些需要了解的？', '您好', '真正的智能服务机器人 #机器人名称# 即将为您服务', '1', '尊敬的#全名# #职位# 您好', '尊敬的贵宾们你们好', '1', '#性别# 您好', '先生女士们好', NULL, '2026-03-14 19:12:52', '', '2026-03-25 23:58:06');
INSERT INTO `sys_reception_config` VALUES (5, 9, '请问关于公司：有哪些需要了解的？', '您好', '真正的智能服务机器人 #机器人名称# 即将为您服务', '1', '尊敬的#全名# #职位# 您好', '尊敬的贵宾们你们好', '1', '#性别# 您好', '先生女士们好', NULL, '2026-03-31 00:38:48', '', '2026-03-31 09:22:05');
COMMIT;

-- ----------------------------
-- 13、导航配置表
-- ----------------------------
DROP TABLE IF EXISTS `sys_nav_config`;
CREATE TABLE `sys_nav_config` (
                                  `nav_id` int NOT NULL AUTO_INCREMENT COMMENT '导航ID',
                                  `nav_name` varchar(100) DEFAULT NULL COMMENT '导航名称',
                                  `map_id` int DEFAULT NULL COMMENT '当前地图ID',
                                  `voice_type` varchar(20) DEFAULT 'default' COMMENT '播报类型(default默认/custom自定义/none无)',
                                  `voice_path` varchar(255) DEFAULT NULL COMMENT '语音文件路径',
                                  `before_msg` varchar(200) DEFAULT NULL COMMENT '出发前播报',
                                  `during_msg` varchar(200) DEFAULT NULL COMMENT '导航中播报',
                                  `after_msg` varchar(200) DEFAULT NULL COMMENT '到达后播报',
                                  `wait_time` int DEFAULT '0' COMMENT '等待时间（秒）',
                                  `retry_count` int DEFAULT '3' COMMENT '重试次数',
                                  `timeout` int DEFAULT '30' COMMENT '超时时间（秒）',
                                  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                  `remark` varchar(255) DEFAULT NULL COMMENT '备注信息',
                                  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
                                  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
                                  `sort_order` int DEFAULT '0' COMMENT '排序号',
                                  PRIMARY KEY (`nav_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='导航配置表';

-- ----------------------------
-- Records of sys_nav_config
-- ----------------------------
BEGIN;
INSERT INTO `sys_nav_config` VALUES (1, NULL, NULL, 'default', NULL, '现在带您去社保窗口', '请跟随我', '已到达', 0, 3, 30, '', NULL, '', NULL, NULL, '0', '0', 0);
COMMIT;

-- ----------------------------
-- 14、讲解内容表
-- ----------------------------
DROP TABLE IF EXISTS `sys_tour_content`;
CREATE TABLE `sys_tour_content` (
                                    `content_id` int NOT NULL AUTO_INCREMENT COMMENT '内容ID',
                                    `robot_id` int NOT NULL COMMENT '机器人ID',
                                    `point_id` int DEFAULT NULL COMMENT '关联点位ID',
                                    `point_name` varchar(50) NOT NULL COMMENT '讲解点名称',
                                    `point_desc` varchar(200) DEFAULT NULL COMMENT '讲解点描述',
                                    `broadcast_type` varchar(20) DEFAULT 'text' COMMENT '播报类型(text文本/audio音频)',
                                    `broadcast_text` text COMMENT '播报内容',
                                    `audio_file` varchar(255) DEFAULT NULL COMMENT '音频文件路径',
                                    `voice_type` varchar(50) DEFAULT '温柔女声' COMMENT '音色',
                                    `speech_rate` int DEFAULT '50' COMMENT '语速(%)',
                                    `interval_time` int DEFAULT '0' COMMENT '间隔时间(毫秒)',
                                    `content_type` varchar(20) DEFAULT 'text' COMMENT '内容类型(text/audio/image/video)',
                                    `media_file` varchar(255) DEFAULT NULL COMMENT '媒体文件路径',
                                    `arm_action` varchar(50) DEFAULT '0°' COMMENT '手臂动作',
                                    `chassis_angle` int DEFAULT '0' COMMENT '底盘转角',
                                    `order_num` int DEFAULT '0' COMMENT '显示顺序',
                                    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                    PRIMARY KEY (`content_id`),
                                    KEY `idx_robot_id` (`robot_id`),
                                    KEY `idx_point_id` (`point_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='讲解内容表';

-- ----------------------------
-- Records of sys_tour_content
-- ----------------------------
BEGIN;
INSERT INTO `sys_tour_content` VALUES (3, 8, NULL, '社保区', '', 'text', '1', '', '温柔女声', 50, 0, 'text', '', '0°', 0, NULL, 'admin', '2026-03-31 09:22:57', '', NULL);
COMMIT;

-- ----------------------------
-- 15、讲解通用配置表
-- ----------------------------
DROP TABLE IF EXISTS `sys_tour_general`;
CREATE TABLE `sys_tour_general` (
                                    `config_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
                                    `robot_id` int NOT NULL COMMENT '机器人ID',
                                    `map_id` int DEFAULT NULL COMMENT '当前地图ID',
                                    `route_id` int DEFAULT NULL COMMENT '当前路线ID',
                                    `voice` varchar(50) DEFAULT '温柔女声' COMMENT '讲解音色',
                                    `voice_interaction` char(1) DEFAULT '1' COMMENT '语音交互（0关闭 1开启）',
                                    `start_command` varchar(50) DEFAULT '开始讲解' COMMENT '开始讲解口令',
                                    `before_tip` varchar(200) DEFAULT NULL COMMENT '运动前提示播报',
                                    `end_tip` varchar(200) DEFAULT NULL COMMENT '讲解结束播报',
                                    `after_action` varchar(20) DEFAULT 'stay' COMMENT '结束后动作(stay停留/charge充电)',
                                    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                    PRIMARY KEY (`config_id`),
                                    KEY `idx_robot_id` (`robot_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='讲解通用配置表';

-- ----------------------------
-- Records of sys_tour_general
-- ----------------------------
BEGIN;
INSERT INTO `sys_tour_general` VALUES (3, 8, 1, 1, '温柔女声', '1', '开始讲解', '即将开始讲解，请跟随我', '本次讲解结束，谢谢', 'stay', NULL, '2026-03-31 00:38:41', '', '2026-03-31 09:22:34');
COMMIT;

-- ----------------------------
-- 16、讲解路线表
-- ----------------------------
DROP TABLE IF EXISTS `sys_tour_route`;
CREATE TABLE `sys_tour_route` (
                                  `route_id` int NOT NULL AUTO_INCREMENT COMMENT '路线ID',
                                  `route_name` varchar(50) NOT NULL COMMENT '路线名称',
                                  `map_id` int DEFAULT NULL COMMENT '地图ID',
                                  `point_ids` text COMMENT '点位ID列表(JSON格式)',
                                  `point_count` int DEFAULT '0' COMMENT '点位数量',
                                  `status` char(1) DEFAULT '1' COMMENT '状态（0禁用 1启用）',
                                  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                  PRIMARY KEY (`route_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='讲解路线表';

-- ----------------------------
-- Records of sys_tour_route
-- ----------------------------
BEGIN;
INSERT INTO `sys_tour_route` VALUES (1, '企业展厅路线', 1, NULL, 3, '1', '', NULL, '', NULL);
INSERT INTO `sys_tour_route` VALUES (2, '政务参观路线', 2, NULL, 2, '1', '', NULL, '', NULL);
INSERT INTO `sys_tour_route` VALUES (3, '新路线', 1, NULL, 0, '1', 'admin', '2026-03-25 19:35:44', '', NULL);
COMMIT;

-- ----------------------------
-- 17、路线点位关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_route_point`;
CREATE TABLE `sys_route_point` (
                                   `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                   `route_id` int NOT NULL COMMENT '路线ID',
                                   `point_id` int NOT NULL COMMENT '点位ID',
                                   `content_id` int DEFAULT NULL COMMENT '关联讲解内容ID',
                                   `order_num` int DEFAULT '0' COMMENT '显示顺序',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_route_id` (`route_id`),
                                   KEY `idx_point_id` (`point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线点位关联表';

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;