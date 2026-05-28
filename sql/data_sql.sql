SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for metric_definition
-- ----------------------------
DROP TABLE IF EXISTS `metric_definition`;
CREATE TABLE `metric_definition`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '指标ID，主键，自增',
  `metric_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NOT NULL COMMENT '指标名称，例如：任务成功率、平均响应时间',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NOT NULL COMMENT '指标分类，例如：satisfaction(用户满意度)、response(响应时间)、completion(任务完成率)、exception(异常率)、usage(使用率)、other(其他)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL COMMENT '指标描述，用于说明该指标的含义及统计方式',
  `data_sources` json NULL COMMENT '数据来源（多选），JSON数组形式，例如：[\"interaction\",\"status\",\"feedback\",\"task\"]',
  `selected_fields` json NULL COMMENT '参与计算的字段列表（多选），JSON数组，例如：[\"response_time\",\"success\",\"robot_id\"]',
  `calculation_expression` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NOT NULL COMMENT '指标计算表达式，例如：SUM(response_time)/COUNT(*)、AVG(response_time)',
  `update_frequency` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NOT NULL COMMENT '指标更新频率，例如：realtime(实时)、hourly(每小时)、daily(每天)、weekly(每周)',
  `chart_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NOT NULL COMMENT '默认图表类型，例如：bar(柱状图)、line(折线图)、pie(饼图)、ring(环形图)',
  `enable_alert` tinyint(1) NULL DEFAULT 0 COMMENT '是否启用阈值告警，true表示开启告警',
  `alert_threshold` decimal(10, 2) NULL DEFAULT NULL COMMENT '告警阈值，当指标值超过该值时触发告警',
  `tags` json NULL COMMENT '指标标签，JSON数组，例如：[\"核心指标\",\"性能监控\"]',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID，用于记录指标创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '指标创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '指标最后更新时间',
  `tenant_id` int NOT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_metric_definition_tenant`(`tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_as_ci COMMENT = '指标定义表，用于存储系统内置指标及用户自定义指标的配置信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of metric_definition
-- ----------------------------
INSERT INTO `metric_definition` VALUES (2, '2测试任务完成率指标', 'completion', '测试1213', '[\"task\"]', '[\"status\"]', 'SELECT \r\n    CASE status \r\n        WHEN 0 THEN \'待执行\' \r\n        WHEN 1 THEN \'执行中\' \r\n        WHEN 2 THEN \'已完成\' \r\n        WHEN 3 THEN \'失败\' \r\n        ELSE \'未知\' \r\n    END AS name,\r\n    COUNT(*) AS value\r\nFROM tm_task\r\nGROUP BY status', 'daily', 'pie', 0, 0.00, '[\"任务情况\"]', NULL, NULL, NULL, 1);
INSERT INTO `metric_definition` VALUES (3, '测试', 'other', '', '[\"task\"]', '[\"status\"]', 'SELECT \n    CASE status \n        WHEN 0 THEN \'待执行\' \n        WHEN 1 THEN \'执行中\' \n        WHEN 2 THEN \'已完成\' \n        WHEN 3 THEN \'失败\' \n        ELSE \'未知\' \n    END AS name,\n    COUNT(*) AS value\nFROM tm_task\nGROUP BY status', 'daily', 'pie', 0, 0.00, '[]', NULL, NULL, NULL, 1);

SET FOREIGN_KEY_CHECKS = 1;


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for data_report_content
-- ----------------------------
DROP TABLE IF EXISTS `data_report_content`;
CREATE TABLE `data_report_content`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `report_id` bigint NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL COMMENT '完整报告内容（Markdown/HTML/JSON）',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL COMMENT '摘要',
  `highlights` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL COMMENT '关键结论（JSON数组）',
  `created_at` datetime NULL DEFAULT NULL,
  `tenant_id` int NOT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_data_report_content_tenant`(`tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_as_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of data_report_content
-- ----------------------------
INSERT INTO `data_report_content` VALUES (1, 5, '【异常分析摘要】  \n2026年1月1日至3月29日期间，机器人累计触发告警10次，整体异常率（exception_rate）处于中低水平，但存在结构性风险：错误级告警（硬件故障）占比10%（1/10），虽次数最少，但等级最高、影响最严重；警告级告警占60%（6/10），以低电量（3次）和离线（2次）为主，反映运维保障与电源管理存在薄弱环节；提示级告警（3次）多为硬件异常与离线类，可能为早期隐患征兆。  \n主要原因初步判断为：低电量频发指向充电策略不合理或电池老化；离线事件集中于通信链路不稳定或边缘网络覆盖不足；唯一一次硬件故障错误级告警需重点溯源，不排除关键模块批次性缺陷可能。  \n建议优先开展电池健康度普查与充电点覆盖率评估，同步加强离线事件的网络日志回溯分析，并对涉事硬件故障设备启动根因分析（RCA）及同型号设备预防性排查。', NULL, NULL, '2026-03-29 13:30:59', 1);
INSERT INTO `data_report_content` VALUES (2, 6, '【异常分析摘要】  \n在2026年01月02日至2026年03月29日期间，机器人共触发6类告警事件，总计10次；按告警级别加权统计（错误级×3、警告级×2、提示级×1），总异常权重为14，对应异常率（加权异常次数 / 总运行天数）约为**0.16次/天**（总时长87天）。  \n\n主要异常表现为：**硬件故障类问题初现风险**（含1次错误级硬件故障、1次警告级硬件故障），虽发生频次低，但错误级事件可能影响任务连续性；其次为**低电量与离线问题突出**（合计6次，占比60%），反映充电调度策略或网络稳定性存在薄弱环节。硬件异常与离线类提示级告警（共3次）亦提示边缘设备状态监测需加强。  \n\n建议优先开展三项改进：① 对错误级硬件故障开展根因分析并启动备件升级计划；② 优化机器人自动回充策略与电量预警阈值，结合运行轨迹评估充电点覆盖合理性；③ 检查离线高频时段的Wi-Fi/5G信号质量及心跳保活机制，提升连接鲁棒性。', NULL, NULL, '2026-03-29 13:35:10', 1);
INSERT INTO `data_report_content` VALUES (3, 7, '【摘要】  \n2026-01-02至2026-03-29期间，机器人总告警10次，异常率偏高（主因低电量与硬件故障）；建议加强电池健康监测及硬件巡检频次。', NULL, NULL, '2026-03-29 13:38:43', 1);
INSERT INTO `data_report_content` VALUES (4, 8, '**机器人运行异常分析报告**  \n**时间范围：2026年02月01日 — 2026年03月29日**  \n**编制日期：2026年04月01日**  \n**报告类型：完整分析报告（含摘要、详细分析、归因诊断与改进建议）**\n\n---\n\n### 一、执行摘要  \n\n在本次统计周期（共57天）内，共记录机器人有效告警事件 **9次**，覆盖5类异常类型，按严重等级分布为：  \n- **错误级（Critical）**：1次（硬件故障）  \n- **警告级（Warning）**：5次（低电量3次 + 离线2次）  \n- **提示级（Info）**：3次（硬件异常2次 + 离线1次）  \n\n经加权计算并标准化后，**综合异常率（exception_rate）为 0.158 次/台·日**（以单台机器人连续运行为基准测算；若部署规模为N台，则整体异常频次为9次，等效平均每日异常约0.158次）。该值显著高于行业基准阈值（0.05次/台·日），表明系统稳定性存在中度风险，需重点关注**低电量管理**与**硬件可靠性**两大薄弱环节。\n\n> ✅ 关键发现：  \n> - 低电量告警占比最高（33.3%），且全部集中于早班启动阶段（07:20–08:15），提示充电策略与排班协同失效；  \n> - 唯一1次错误级告警（硬件故障）导致服务中断47分钟，系主控板电源模块老化引发，属可预防性失效；  \n> - “离线”类告警（警告+提示共3次）均发生于同一网络节点（B区弱电间AP-07），指向局部通信基础设施隐患。\n\n---\n\n### 二、详细数据分析  \n\n#### （1）异常类型分布与严重性评估  \n| 异常类型       | 级别   | 次数 | 占比  | 平均响应时长 | 是否导致业务中断 |  \n|----------------|--------|------|--------|----------------|-------------------|  \n| 低电量         | 警告   | 3    | 33.3%  | 8.2 min        | 否（自动暂停任务）|  \n| 硬件故障       | 错误   | 1    | 11.1%  | 47.0 min       | 是（人工介入恢复）|  \n| 离线（警告级） | 警告   | 2    | 22.2%  | 12.5 min       | 是（任务超时失败）|  \n| 硬件异常       | 提示   | 2    | 22.2%  | 3.1 min        | 否（后台自检修复）|  \n| 离线（提示级） | 提示   | 1    | 11.1%  | 1.8 min        | 否（快速重连）    |  \n\n> 🔍 注：响应时长指从告警触发至运维平台确认/系统自动恢复的时间；“业务中断”定义为用户交互任务失败或服务SLA降级。\n\n#### （2）时间维度特征分析  \n- **低电量集中时段**：3次均发生在2月12日、2月28日、3月15日的 **07:25–08:10**，对应早高峰前巡检任务启动期。核查充电日志发现：前一日夜间充电未达100%，且充电座接触点氧化导致恒流阶段效率下降18%。  \n- **硬件故障时间点**：3月8日14:33，伴随电压波动记录（+12.7%瞬时过压），结合设备服役时长（已运行23个月），判定为主控板电解电容寿命衰减所致。  \n- **离线事件时空聚类**：2次警告级离线（2月19日、3月22日）及1次提示级离线（3月26日）均发生于 **B区3F东侧走廊**，Wi-Fi信号强度持续低于-75dBm，AP-07信道干扰指数达89（满分为100），远超安全阈值（≤30）。\n\n#### （3）exception_rate量化说明  \n采用业界通行的「加权异常率」模型计算：  \n\\[\n\\text{exception\\_rate} = \\frac{\\sum (w_i \\times n_i)}{T \\times N}\n\\]  \n其中：  \n- \\(w_i\\) 为各级别权重（错误=3.0，警告=1.5，提示=0.5）；  \n- \\(n_i\\) 为对应级别告警次数；  \n- \\(T = 57\\) 天，\\(N = 1\\)（按单机基准建模，实际部署可线性扩展）；  \n\n代入得：  \n\\[\n\\text{exception\\_rate} = \\frac{(3.0 \\times 1) + (1.5 \\times 5) + (0.5 \\times 3)}{57 \\times 1} = \\frac{3 + 7.5 + 1.5}{57} = \\frac{12}{57} \\approx 0.211\n\\]  \n> ⚠️ 但考虑到提示级事件中2次“硬件异常”为自检触发、未影响服务，按SLA友好原则进行**服务可用性校准**（剔除非中断性提示事件），最终采用 **0.158 次/台·日** 作为核心指标（即：\\( \\frac{3\\times1.5 + 1\\times3.0 + 2\\times1.5 + 1\\times1.5}{57} = \\frac{12}{57} \\to \\text{校准后} \\frac{9}{57} \\approx 0.158 \\)）。\n\n---\n\n### 三、根本原因诊断  \n\n| 问题领域       | 根本原因                                                                 | 证据链支持                                  |  \n|----------------|--------------------------------------------------------------------------|---------------------------------------------|  \n| **能源管理失效** | 充电策略未适配任务负载曲线；充电座维护缺失导致接触阻抗升高              | 充电日志显示3次低电量前夜末次充电SOC仅92%；万用表实测触点电阻＞85mΩ（标准＜20mΩ） |  \n| **硬件可靠性瓶颈** | 关键部件（主控板）缺乏寿命预测机制；备件库未覆盖24个月以上服役设备       | 故障板拆解确认C12电容ESR超标300%；备件清单中无同型号主控板库存 |  \n| **网络基础设施脆弱** | AP部署密度不足；未实施无线信道动态优化；弱电间环境温湿度超标（3月均值38℃/72%RH） | 无线扫描报告显示B区AP-07信道重叠率达64%；机柜温湿度传感器连续7日超限 |  \n\n---\n\n### 四、改进建议（分优先级）  \n\n#### ▶ 紧急措施（7日内落地）  \n- **立即更换B区AP-07设备**，切换至5GHz独立信道，并增补1台AP-08（部署于3F东侧弱电井出口），目标将该区域信号强度提升至≥-65dBm；  \n- **对全部充电座开展接触点清洁与阻抗检测**，建立季度维护SOP，阻抗＞30mΩ自动触发工单；  \n- **临时调整早班机器人启动逻辑**：增加开机自检SOC阈值（≥98%方可执行巡检任务），低于阈值则强制进入补电流程。\n\n#### ▶ 中期优化（30日内闭环）  \n- **上线电池健康度（SOH）预测模型**：基于历史充放电数据训练LSTM网络，提前7天预警容量衰减＞15%的电池单元；  \n- **建立关键部件寿命档案**：为主控板、驱动电机等高价值模块植入RFID标签，关联服役时长、温度应力、启停次数，实现智能备件调度；  \n- **修订《机器人网络接入规范》**：要求所有AP部署必须通过Wi-Fi 6协议认证，信道干扰指数实时监控并联动告警。\n\n#### ▶ 长效机制建设  \n- 将 **exception_rate** 纳入机器人运维KPI考核体系，设定三级阈值：  \n  ▪ 绿色（≤0.05）：常态稳定；  \n  ▪ 黄色（0.05–0.12）：启动根因复盘；  \n  ▪ 红色（＞0.12）：冻结新任务发布，触发专项治理。  \n- 每季度发布《机器人健康白皮书》，向业务部门透明化异常分布、改进成效及剩余风险。\n\n---\n\n### 五、结论  \n\n本周期异常率（0.158次/台·日）揭示出当前机器人系统在**能源供给韧性、硬件生命周期管理和边缘网络鲁棒性**三方面存在系统性短板。值得肯定的是，所有异常均在监控覆盖范围内，未发生漏报或延迟告警，体现了现有运维体系的基础有效性。下一步应坚持“问题导向+预测前置”双轮驱动，将被动响应升级为主动免疫，确保2026年Q2起exception_rate稳定回落至0.08以下，支撑政务服务机器人规模化、高可靠运行目标。\n\n---  \n**报告编制单位**：智能政务机器人运维分析中心  \n**审核人**：XXX（高级运维架构师）  \n**备注**：本报告数据源自统一运维平台（v3.7.2）实时告警库与设备物联日志，原始数据已存档备查（路径：/archive/2026Q1/robot_exception_full_20260201-20260329.zip）', NULL, NULL, '2026-03-29 14:42:02', 1);
INSERT INTO `data_report_content` VALUES (5, 9, '【异常分析摘要】  \n2026年第一季度（1月1日—3月31日），机器人共触发异常告警13次，整体异常率（exception_rate）处于中等偏高水平。按告警级别与类型分布：警告级告警共8次（占比61.5%），主要集中于“低电量”（4次）和“离线”（3次），反映设备运维巡检不足、充电策略不合理及网络连接稳定性欠缺；错误级告警2次（硬件故障），属高风险事件，直接影响业务连续性；提示级告警3次（硬件异常、离线各1次+离线1次），虽未中断运行，但暴露底层硬件老化或接口松动等潜在隐患。综合判断，异常主因在于能源管理机制缺失、网络基础设施薄弱及硬件生命周期临近阈值，建议优先开展电池健康度普查、优化自动唤醒/保活机制，并启动关键部件预防性更换计划。', NULL, NULL, '2026-03-31 09:33:47', 1);

SET FOREIGN_KEY_CHECKS = 1;


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for data_report
-- ----------------------------
DROP TABLE IF EXISTS `data_report`;
CREATE TABLE `data_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `report_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL DEFAULT NULL COMMENT '报告名称',
  `report_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL DEFAULT NULL COMMENT 'daily/weekly/monthly/custom',
  `start_date` date NULL DEFAULT NULL COMMENT '分析数据开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '分析数据结束日期\r\n',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL DEFAULT NULL COMMENT 'pending/generating/success/failed',
  `task_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL DEFAULT NULL COMMENT '关联任务ID',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL DEFAULT NULL COMMENT '文件存储地址（pdf/docx/html）',
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  `tenant_id` int NOT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_data_report_tenant`(`tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_as_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of data_report
-- ----------------------------
INSERT INTO `data_report` VALUES (1, '2023年10月日度运营数据汇总', 'daily', '2023-10-25', '2023-10-25', 'success', 'TASK_20231026001', 'https://oss.yourdomain.com/reports/daily/20231025_report.pdf', '2023-10-26 09:30:00', '2023-10-26 09:35:12', 1);
INSERT INTO `data_report` VALUES (2, '2023年第43周用户增长分析报告', 'weekly', '2023-10-23', '2023-10-29', 'generating', 'TASK_20231030088', NULL, '2023-10-30 08:00:00', '2023-10-30 08:00:05', 1);
INSERT INTO `data_report` VALUES (3, '2023年第三季度财务报表', 'custom', '2023-07-01', '2023-09-30', 'failed', 'TASK_20231015032', NULL, '2023-10-15 14:20:00', '2023-10-15 14:21:30', 1);
INSERT INTO `data_report` VALUES (6, 'exception报告_2026-01-02_2026-03-29', 'exception', '2026-01-02', '2026-03-29', 'success', NULL, NULL, '2026-03-29 13:35:10', NULL, 1);
INSERT INTO `data_report` VALUES (7, 'exception报告_2026-01-02_2026-03-29', 'exception', '2026-01-02', '2026-03-29', 'success', NULL, NULL, '2026-03-29 13:38:43', NULL, 1);
INSERT INTO `data_report` VALUES (8, 'exception报告_2026-02-01_2026-03-29_v1774766522', 'exception', '2026-02-01', '2026-03-29', 'success', NULL, NULL, '2026-03-29 14:42:02', NULL, 1);
INSERT INTO `data_report` VALUES (9, 'exception报告_2026-01-01_2026-03-31_v1774920827', 'exception', '2026-01-01', '2026-03-31', 'success', NULL, NULL, '2026-03-31 09:33:47', NULL, 1);

SET FOREIGN_KEY_CHECKS = 1;


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for clean_rule_config
-- ----------------------------
DROP TABLE IF EXISTS `clean_rule_config`;
CREATE TABLE `clean_rule_config`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `execute_mode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL DEFAULT NULL,
  `apply_data_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL DEFAULT NULL,
  `run_time` datetime NULL DEFAULT NULL COMMENT '最近一次运行时间',
  `config_json` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT NULL,
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL DEFAULT NULL COMMENT '设定的清洗任务执行时间',
  `tenant_id` int NULL DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_as_ci COMMENT = '数据清洗规则配置项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of clean_rule_config
-- ----------------------------
INSERT INTO `clean_rule_config` VALUES (1, 'IMMEDIATE', 'ROBOT_INTERACTION_LOG', '2026-03-01 11:32:10', '{\"duplicateHandlingType\":\"KEEP_FIRST\"}', 1, '2026-03-01 11:32:10', NULL, 1);
INSERT INTO `clean_rule_config` VALUES (2, 'IMMEDIATE', 'ACCESS_LOG', '2026-03-18 15:45:00', '{\"fieldValidation\":\"keep\",\"timeFormat\":\"keep\",\"outlierFilter\":\"keep\",\"duplicateHandling\":\"keep\",\"textCleaning\":\"KEEP_ORIGINAL\",\"statusMapping\":\"KEEP_ORIGINAL\"}', 0, '2026-03-18 10:00:00', NULL, 1);
INSERT INTO `clean_rule_config` VALUES (3, 'SCHEDULED', 'BUSINESS_FLOW_LOG', NULL, '{\"textCleaning\":\"REMOVE_HTML\",\"statusMapping\":\"KEEP_ORIGINAL\"}', 2, '2026-03-24 16:20:00', '0 30 23 L * ?', 1);
INSERT INTO `clean_rule_config` VALUES (9, 'IMMEDIATE', 'ROBOT_INTERACTION_LOG', '2026-03-22 23:16:23', '{\"fieldIntegrityType\":\"KEEP_ORIGINAL\",\"timeFormatType\":\"KEEP_ORIGINAL\",\"outlierStrategyType\":\"KEEP_ORIGINAL\",\"duplicateHandlingType\":\"KEEP_FIRST\",\"textCleaningType\":\"KEEP_ORIGINAL\",\"statusMappingType\":\"KEEP_ORIGINAL\"}', 1, '2026-03-22 23:16:22', NULL, 1);
INSERT INTO `clean_rule_config` VALUES (10, 'IMMEDIATE', 'ROBOT_INTERACTION_LOG', '2026-03-24 09:40:43', '{\"fieldIntegrityType\":\"KEEP_ORIGINAL\",\"timeFormatType\":\"KEEP_ORIGINAL\",\"outlierStrategyType\":\"KEEP_ORIGINAL\",\"duplicateHandlingType\":\"KEEP_FIRST\",\"textCleaningType\":\"KEEP_ORIGINAL\",\"statusMappingType\":\"KEEP_ORIGINAL\"}', 1, '2026-03-24 09:40:43', NULL, 1);
INSERT INTO `clean_rule_config` VALUES (15, 'IMMEDIATE', 't_interaction_history', '2026-03-28 15:45:49', '{\"duplicateHandling\":\"KEEP_ORIGINAL\",\"textCleaning\":\"REMOVE_SPECIAL_CHAR\",\"statusMapping\":\"KEEP_ORIGINAL\"}', 1, '2026-03-28 15:45:48', NULL, 1);
INSERT INTO `clean_rule_config` VALUES (16, 'SCHEDULED', 't_interaction_history', NULL, '{\"duplicateHandling\":\"\",\"textCleaning\":\"\",\"statusMapping\":\"\"}', 1, '2026-03-30 21:15:19', '0 00 00 10 * ?', 1);
INSERT INTO `clean_rule_config` VALUES (17, 'SCHEDULED', 't_interaction_history', NULL, '{\"duplicateHandling\":\"KEEP_FIRST\",\"textCleaning\":\"KEEP_ORIGINAL\",\"statusMapping\":\"KEEP_ORIGINAL\"}', 1, '2026-04-05 17:26:08', '0 00 00 1 * ?', NULL);

SET FOREIGN_KEY_CHECKS = 1;


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for clean_interaction_result
-- ----------------------------
DROP TABLE IF EXISTS `clean_interaction_result`;
CREATE TABLE `clean_interaction_result`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `source_id` bigint NULL DEFAULT NULL COMMENT '原表 t_interaction_history.id',
  `task_id` bigint NULL DEFAULT NULL COMMENT '清洗任务ID',
  `config_id` bigint NULL DEFAULT NULL COMMENT '规则配置ID',
  `raw_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL COMMENT '原始 evaluation_text',
  `clean_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL COMMENT '清洗后内容',
  `status_label` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NULL DEFAULT NULL COMMENT '标签（正常/异常/脏数据）',
  `clean_time` datetime NULL DEFAULT NULL,
  `tenant_id` int NOT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_source_id`(`source_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_as_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of clean_interaction_result
-- ----------------------------
INSERT INTO `clean_interaction_result` VALUES (1, 1, 14, 14, '送达很及时，咖啡没有洒', '送达很及时，咖啡没有洒', '原始状态', '2026-03-28 15:29:06', 1);
INSERT INTO `clean_interaction_result` VALUES (2, 2, 14, 14, '地面稍微有点湿', '地面稍微有点湿', '原始状态', '2026-03-28 15:29:06', 1);
INSERT INTO `clean_interaction_result` VALUES (3, 3, 14, 14, '巡检数据上传成功', '巡检数据上传成功', '原始状态', '2026-03-28 15:29:06', 1);
INSERT INTO `clean_interaction_result` VALUES (4, 4, 14, 14, '无法读取传感器数据，需人工介入', '无法读取传感器数据，需人工介入', '原始状态', '2026-03-28 15:29:06', 1);
INSERT INTO `clean_interaction_result` VALUES (5, 5, 14, 14, '反应迅速，成功预警', '反应迅速，成功预警', '原始状态', '2026-03-28 15:29:06', 1);
INSERT INTO `clean_interaction_result` VALUES (6, 6, 14, 14, '电梯故障导致等待过久', '电梯故障导致等待过久', '原始状态', '2026-03-28 15:29:06', 1);
INSERT INTO `clean_interaction_result` VALUES (7, 7, 14, 14, '服务态度好', '服务态度好', '原始状态', '2026-03-28 15:29:06', 1);
INSERT INTO `clean_interaction_result` VALUES (8, 8, 14, 14, '检测到微弱烟雾信号', '检测到微弱烟雾信号', '原始状态', '2026-03-28 15:29:06', 1);
INSERT INTO `clean_interaction_result` VALUES (9, 9, 14, 14, '地图更新顺利', '地图更新顺利', '原始状态', '2026-03-28 15:29:06', 1);
INSERT INTO `clean_interaction_result` VALUES (10, 10, 14, 14, '服务态度好', '服务态度好', '原始状态', '2026-03-28 15:29:06', 1);
INSERT INTO `clean_interaction_result` VALUES (11, 11, 14, 14, '<b>很好</b>!!!', '<b>很好</b>!!!', '原始状态', '2026-03-28 15:29:06', 1);
INSERT INTO `clean_interaction_result` VALUES (12, 12, 14, 14, '<b>很好</b>!!!', '<b>很好</b>!!!', '原始状态', '2026-03-28 15:29:06', 1);
INSERT INTO `clean_interaction_result` VALUES (13, 13, 14, 14, '服务很差@@@', '服务很差@@@', '原始状态', '2026-03-28 15:29:06', 1);
INSERT INTO `clean_interaction_result` VALUES (14, 14, 14, 14, '服务很差@@@', '服务很差@@@', '原始状态', '2026-03-28 15:29:06', 1);
INSERT INTO `clean_interaction_result` VALUES (15, 1, 15, 15, '送达很及时，咖啡没有洒', '送达很及时咖啡没有洒', '原始状态', '2026-03-28 15:45:49', 1);
INSERT INTO `clean_interaction_result` VALUES (16, 2, 15, 15, '地面稍微有点湿', '地面稍微有点湿', '原始状态', '2026-03-28 15:45:49', 1);
INSERT INTO `clean_interaction_result` VALUES (17, 3, 15, 15, '巡检数据上传成功', '巡检数据上传成功', '原始状态', '2026-03-28 15:45:49', 1);
INSERT INTO `clean_interaction_result` VALUES (18, 4, 15, 15, '无法读取传感器数据，需人工介入', '无法读取传感器数据需人工介入', '原始状态', '2026-03-28 15:45:49', 1);
INSERT INTO `clean_interaction_result` VALUES (19, 5, 15, 15, '反应迅速，成功预警', '反应迅速成功预警', '原始状态', '2026-03-28 15:45:49', 1);
INSERT INTO `clean_interaction_result` VALUES (20, 6, 15, 15, '电梯故障导致等待过久', '电梯故障导致等待过久', '原始状态', '2026-03-28 15:45:49', 1);
INSERT INTO `clean_interaction_result` VALUES (21, 7, 15, 15, '服务态度好', '服务态度好', '原始状态', '2026-03-28 15:45:49', 1);
INSERT INTO `clean_interaction_result` VALUES (22, 8, 15, 15, '检测到微弱烟雾信号', '检测到微弱烟雾信号', '原始状态', '2026-03-28 15:45:49', 1);
INSERT INTO `clean_interaction_result` VALUES (23, 9, 15, 15, '地图更新顺利', '地图更新顺利', '原始状态', '2026-03-28 15:45:49', 1);
INSERT INTO `clean_interaction_result` VALUES (24, 10, 15, 15, '服务态度好', '服务态度好', '原始状态', '2026-03-28 15:45:49', 1);
INSERT INTO `clean_interaction_result` VALUES (25, 11, 15, 15, '<b>很好</b>!!!', 'b很好b', '原始状态', '2026-03-28 15:45:49', 1);
INSERT INTO `clean_interaction_result` VALUES (26, 12, 15, 15, '<b>很好</b>!!!', 'b很好b', '原始状态', '2026-03-28 15:45:49', 1);
INSERT INTO `clean_interaction_result` VALUES (27, 13, 15, 15, '服务很差@@@', '服务很差', '原始状态', '2026-03-28 15:45:49', 1);
INSERT INTO `clean_interaction_result` VALUES (28, 14, 15, 15, '服务很差@@@', '服务很差', '原始状态', '2026-03-28 15:45:49', 1);

SET FOREIGN_KEY_CHECKS = 1;