/*
 Navicat Premium Dump SQL

 Source Server         : ruoyi
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : 8.163.1.154:3306
 Source Schema         : ry-vue

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 11/08/2026 20:30:10
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for qa_chat
-- ----------------------------
DROP TABLE IF EXISTS `qa_chat`;
CREATE TABLE `qa_chat`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '问答ID',
  `chat_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问答名称',
  `chat_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '问答描述',
  `chat_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'dify' COMMENT '对话类型(dify/openai)',
  `api_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'API Key',
  `base_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接口地址',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名称',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_qa_chat_name`(`chat_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '问答配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qa_chat
-- ----------------------------
INSERT INTO `qa_chat` VALUES (1, '政务指南问答', '', 'dify', 'app-EdNaeoq8ej55IDR7LGo6Clz1', 'http://119.91.158.102:8048/v1', NULL, '', '2026-07-25 16:36:55', '', '2026-08-11 19:52:22');
INSERT INTO `qa_chat` VALUES (2, 'deepseek API', 'deepseek的一个API', 'openai', 'sk-aff9a28daf214d2fafea1bd9555e8752', 'https://api.deepseek.com', 'deepseek-v4-pro', '', '2026-08-11 19:57:00', '', '2026-08-11 20:08:33');

SET FOREIGN_KEY_CHECKS = 1;
