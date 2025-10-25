-- ======================================================
-- 数据库迁移脚本：移除 user 表的 vipSign 字段
--
-- 目的：消除冗余，统一使用 vip_memberships 表管理 VIP 状态
--
-- 执行前注意事项：
-- 1. 请在非生产环境先测试此迁移脚本
-- 2. 建议在业务低峰期执行
-- 3. 执行前请务必备份数据库
--
-- 执行步骤：
-- 1. 备份数据库
-- 2. 数据迁移（可选）：如果需要保留历史数据，先将 user.vipSign 同步到 vip_memberships
-- 3. 删除 vipSign 字段
--
-- 日期：2025-10-20
-- ======================================================

USE bear_picture_test;

-- ======================================================
-- 步骤 1：数据迁移（可选）
-- 如果 user 表中有 vipSign=1 但 vip_memberships 表中没有对应记录的用户，
-- 可以执行此步骤将其同步到 vip_memberships 表
-- ======================================================

-- 查看需要迁移的数据（仅查询，不会修改数据）
SELECT
    u.id AS user_id,
    u.userAccount,
    u.vipSign,
    vm.id AS vip_membership_id,
    vm.is_vip
FROM user u
LEFT JOIN vip_memberships vm ON u.id = vm.user_id
WHERE u.vipSign = 1
  AND (vm.id IS NULL OR vm.is_vip = 0)
  AND u.isDelete = 0;

-- 如果上述查询有结果，可以执行下面的迁移语句
-- 注意：这会为所有 vipSign=1 但没有 VIP 记录的用户创建 VIP 记录
-- 默认给予 30 天的 VIP 时长，您可以根据实际情况调整

-- INSERT INTO vip_memberships (user_id, is_vip, vip_start_date, vip_end_date, created_at, updated_at)
-- SELECT
--     u.id,
--     1,
--     NOW(),
--     DATE_ADD(NOW(), INTERVAL 30 DAY),
--     NOW(),
--     NOW()
-- FROM user u
-- LEFT JOIN vip_memberships vm ON u.id = vm.user_id
-- WHERE u.vipSign = 1
--   AND vm.id IS NULL
--   AND u.isDelete = 0;

-- ======================================================
-- 步骤 2：删除 user 表的 vipSign 字段
-- ======================================================

-- 删除 vipSign 字段
ALTER TABLE `user` DROP COLUMN `vipSign`;

-- ======================================================
-- 验证迁移结果
-- ======================================================

-- 查看 user 表结构，确认 vipSign 字段已删除
SHOW COLUMNS FROM `user`;

-- 查看 vip_memberships 表数据统计
SELECT
    COUNT(*) AS total_records,
    SUM(CASE WHEN is_vip = 1 THEN 1 ELSE 0 END) AS active_vip_count,
    SUM(CASE WHEN is_vip = 0 OR is_vip IS NULL THEN 1 ELSE 0 END) AS inactive_count
FROM vip_memberships;

-- ======================================================
-- 回滚方案（如果需要恢复 vipSign 字段）
-- ======================================================

-- 如果迁移出现问题，可以执行以下语句恢复 vipSign 字段
-- ALTER TABLE `user` ADD COLUMN `vipSign` tinyint(1) NULL DEFAULT 0 COMMENT '会员标识：0-非VIP, 1-VIP' AFTER `vipCode`;

-- 从 vip_memberships 恢复数据到 user.vipSign
-- UPDATE `user` u
-- INNER JOIN vip_memberships vm ON u.id = vm.user_id
-- SET u.vipSign = vm.is_vip
-- WHERE vm.is_vip = 1 AND vm.vip_end_date > NOW();

-- ======================================================
-- 迁移完成
-- ======================================================