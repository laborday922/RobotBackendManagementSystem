
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_app_api
-- ----------------------------
DROP TABLE IF EXISTS `t_app_api`;
CREATE TABLE `t_app_api`  (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `app_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                              `api_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                              `api_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                              `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
                              `params_schema` json NOT NULL,
                              `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`) USING BTREE,
                              UNIQUE INDEX `uk_app_api`(`app_id` ASC, `api_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of t_app_api
-- ----------------------------
BEGIN;
INSERT INTO `t_app_api` (`id`, `app_id`, `api_key`, `api_name`, `description`, `params_schema`, `created_at`) VALUES (1, 'APP001', 'robot.move', '机器人移动', '控制机器人移动到指定目标位置', '{\"name\": \"target\", \"type\": \"string\", \"required\": true}', '2026-03-28 18:00:25');
COMMIT;

-- ----------------------------
-- Table structure for t_app_constraint
-- ----------------------------
DROP TABLE IF EXISTS `t_app_constraint`;
CREATE TABLE `t_app_constraint`  (
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `app_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                     `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                     `expression` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                     `error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                     `severity` enum('error','warning') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'error',
                                     `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                     PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of t_app_constraint
-- ----------------------------
BEGIN;
INSERT INTO `t_app_constraint` (`id`, `app_id`, `name`, `expression`, `error_message`, `severity`, `created_at`) VALUES (1, 'APP001', '载重限制校验', 'form_data.weight <= app_param.max_load_weight', '餐品重量不能超过机器人最大载重', 'error', '2026-03-28 19:53:23');
COMMIT;

-- ----------------------------
-- Table structure for t_app_library
-- ----------------------------
DROP TABLE IF EXISTS `t_app_library`;
CREATE TABLE `t_app_library`  (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增ID',
                                  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '应用唯一标识（业务ID）',
                                  `app_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '应用名称',
                                  `app_type` tinyint NOT NULL COMMENT '应用类型：0(交互类)、1(控制类)、2(监控类)',
                                  `enabled` tinyint(1) NOT NULL COMMENT '启用状态：1(启用)、0(禁用)',
                                  `create_time` datetime NOT NULL COMMENT '创建时间',
                                  `update_time` datetime NULL DEFAULT NULL COMMENT '最后更新时间',
                                  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用描述',
                                  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应用库表';

-- ----------------------------
-- Records of t_app_library
-- ----------------------------
BEGIN;
INSERT INTO `t_app_library` (`id`, `app_id`, `app_name`, `app_type`, `enabled`, `create_time`, `update_time`, `description`) VALUES (6, 'APP001', '机器人配送管理系统', 1, 1, '2024-01-01 00:00:00', '2025-01-01 00:00:00', '管理配送机器人的任务分配、路径规划、状态监控'), (7, 'APP002', '机器人清洁调度系统', 1, 0, '2024-01-05 00:00:00', '2025-02-01 00:00:00', '调度清洁机器人的清洁任务、区域分配、清洁效果统计'), (8, 'APP003', '机器人巡检监控平台', 2, 0, '2024-01-10 00:00:00', '2025-01-15 00:00:00', '实时监控巡检机器人的位置、状态、巡检数据上报'), (9, 'APP004', '导诊机器人交互系统', 0, 0, '2024-01-15 00:00:00', '2025-02-10 00:00:00', '实现导诊机器人与患者的语音交互、导航引导、问题解答');
COMMIT;

-- ----------------------------
-- Table structure for t_app_param
-- ----------------------------
DROP TABLE IF EXISTS `t_app_param`;
CREATE TABLE `t_app_param`  (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `app_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                `param_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                `param_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                `param_type` enum('number','string','boolean','enum') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                `default_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
                                `validation_rule` json NULL,
                                `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `uk_app_param`(`app_id` ASC, `param_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of t_app_param
-- ----------------------------
BEGIN;
INSERT INTO `t_app_param` (`id`, `app_id`, `param_key`, `param_name`, `param_type`, `default_value`, `validation_rule`, `created_at`) VALUES (1, 'APP001', 'max_speed', '最大速度', 'number', '1.3', '{\"max\": 1.5, \"min\": 0.5, \"type\": \"range\"}', '2026-03-28 18:07:16');
COMMIT;

-- ----------------------------
-- Table structure for t_interaction_history
-- ----------------------------
DROP TABLE IF EXISTS `t_interaction_history`;
CREATE TABLE `t_interaction_history`  (
                                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增ID',
                                          `task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务id',
                                          `interaction_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单次交互唯一标识',
                                          `robot_id` bigint NULL DEFAULT NULL COMMENT '交互的机器人ID',
                                          `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作用户ID（如管理员/终端用户）',
                                          `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作用户名称',
                                          `interaction_type` tinyint NULL DEFAULT NULL COMMENT '交互类型：0(配送)、1(清洁)、2(巡检)、3(维保)、4(安防)',
                                          `interaction_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '交互内容（请求/响应数据）',
                                          `interaction_time` datetime NOT NULL COMMENT '交互发生时间',
                                          `duration` int NULL DEFAULT NULL COMMENT '交互耗时（秒）',
                                          `rating` tinyint NULL DEFAULT NULL COMMENT '交互评分：0(差)、1(中等)、2(良好)',
                                          `evaluation_text` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评价文本（用户/系统评价）',
                                          `status` tinyint NULL DEFAULT NULL COMMENT '交互状态：0(成功)、1(失败)、2(超时)',
                                          `ext_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '扩展信息（JSON格式，存储额外字段）',
                                          PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '交互历史表';

-- ----------------------------
-- Records of t_interaction_history
-- ----------------------------
BEGIN;
INSERT INTO `t_interaction_history` (`id`, `task_id`, `interaction_id`, `robot_id`, `user_id`, `user_name`, `interaction_type`, `interaction_content`, `interaction_time`, `duration`, `rating`, `evaluation_text`, `status`, `ext_info`) VALUES (7, 'TASK001', 'INTER001', 1, 'USER001', '张护士', 0, '请求配送药品：阿莫西林5盒至住院楼3层10号病房', '2025-03-14 07:50:00', 600, 2, '配送及时，药品无误', 0, '{\"priority\":\"normal\",\"delivery_type\":\"urgent\"}'), (8, 'TASK002', 'INTER002', 2, 'USER002', '李医生', 0, '请求配送检查报告至门诊楼2层诊室', '2025-03-14 09:50:00', 300, 1, '配送延迟5分钟，需优化路径', 0, '{\"priority\":\"high\",\"delivery_type\":\"normal\"}'), (9, 'TASK003', 'INTER003', 3, 'USER003', '王保洁', 1, '下达清洁任务：医技楼3层走廊深度清洁', '2025-03-14 08:50:00', 900, 0, '清洁刷故障，任务未完成', 1, '{\"clean_type\":\"deep\",\"area_size\":\"200㎡\"}'), (10, 'TASK004', 'INTER004', 4, 'USER004', '赵管理员', 1, '设置清洁机器人002充电时间为每日12:00', '2025-03-14 10:50:00', 60, 2, '设置成功，机器人已生效', 0, '{\"charge_cycle\":\"daily\",\"charge_duration\":\"2h\"}'), (11, 'TASK005', 'INTER005', 5, 'USER005', '孙保安', 2, '查看巡检机器人001实时位置和巡检画面', '2025-03-14 07:20:00', 120, 2, '画面清晰，定位准确', 0, '{\"inspect_type\":\"security\",\"camera_angle\":\"180°\"}'), (12, 'TASK006', 'INTER006', 6, 'USER006', '周工程师', 2, '远程校准巡检机器人002传感器', '2025-03-14 08:50:00', 180, 1, '校准成功，但响应略有延迟', 0, '{\"sensor_type\":\"position\",\"calibration_accuracy\":\"±0.1m\"}'), (13, 'TASK007', 'INTER007', 7, 'USER007', '吴患者', 0, '咨询：门诊楼口腔科位置及就诊流程', '2025-03-14 08:20:00', 240, 2, '回答清晰，导航准确', 0, '{\"consult_type\":\"navigation\",\"department\":\"stomatology\"}'), (14, 'TASK008', 'INTER008', 8, 'USER008', '郑护士', 0, '请求导诊机器人002引导急诊患者至抢救室', '2025-03-14 10:20:00', 150, 0, '机器人离线，引导失败', 1, '{\"patient_type\":\"emergency\",\"priority\":\"highest\"}'), (15, 'TASK009', 'INTER009', 9, 'USER009', '冯医生', 0, '请求配送手术耗材至手术室1号门', '2025-03-14 09:20:00', 480, 2, '耗材齐全，配送准时', 0, '{\"surgical_type\":\"general\",\"consumables\":[\"纱布\",\"手术刀\"]}'), (16, 'TASK010', 'INTER010', 10, 'USER010', '陈管理员', 2, '查看巡检机器人003电量和运行状态', '2025-03-14 09:50:00', 90, 1, '电量充足，但移动速度略慢', 0, '{\"battery_health\":\"95%\",\"speed_limit\":\"0.8m/s\"}');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
