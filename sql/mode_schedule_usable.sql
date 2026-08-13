-- =====================================================
-- sys_mode_schedule 表新增 usable 字段（启用标志）
-- 2026-08-13
-- =====================================================

ALTER TABLE `sys_mode_schedule`
ADD COLUMN `usable` char(1) NOT NULL DEFAULT '1' COMMENT '是否启用（0停用 1启用）' AFTER `status`;

-- 已有排程状态迁移：
-- status='paused' 的排程，usable 设为 '0'
UPDATE `sys_mode_schedule` SET `usable` = '0' WHERE `status` = 'paused' AND `del_flag` = '0';
