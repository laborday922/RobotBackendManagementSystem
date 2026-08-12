-- =====================================================
-- sys_mode_param 表结构升级 + 数据迁移
-- 2026-08-12
-- =====================================================

-- 1. 新增 param_label 字段
ALTER TABLE `sys_mode_param`
ADD COLUMN `param_label` varchar(100) NULL COMMENT '参数标签（前端显示用，如 充电策略）' AFTER `param_name`;

-- 2. 删除旧的无效参数（del_flag='2' 或脏数据）
DELETE FROM `sys_mode_param` WHERE del_flag = '2';

-- 3. 清除旧的全部参数，用新的规范数据替换
DELETE FROM `sys_mode_param` WHERE del_flag = '0';

INSERT INTO `ry-vue`.`sys_mode_param` (`param_id`, `mode_id`, `param_name`, `param_label`, `param_type`, `param_description`, `param_value`, `param_options`, `param_min`, `param_max`, `param_unit`, `order_num`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `tenant_id`) VALUES (77, 3, 'charge_strategy', '充电策略', 'select', '选择充电执行策略', 'after_task', '[{\"label\":\"完成任务后充电\",\"value\":\"after_task\"},{\"label\":\"立即充电\",\"value\":\"immediate\"}]', 0, 2, '', 1, '0', 'system', '2026-08-12 22:28:25', '', NULL, 0);
INSERT INTO `ry-vue`.`sys_mode_param` (`param_id`, `mode_id`, `param_name`, `param_label`, `param_type`, `param_description`, `param_value`, `param_options`, `param_min`, `param_max`, `param_unit`, `order_num`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `tenant_id`) VALUES (78, 3, 'charge_threshold', '充电电量阈值', 'range', '低电量触发充电的电量阈值', '20', NULL, 0, 100, '%', 2, '0', 'system', '2026-08-12 22:28:25', '', NULL, 0);
