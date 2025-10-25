-- VIP过期检查定时任务配置
-- 插入 VIP 过期检查定时任务到 scheduled_task 表

INSERT INTO scheduled_task (taskKey, taskName, taskCron, taskDesc, taskBean, taskStatus, isDelete, createTime, updateTime)
VALUES ('vip_expire_check',
        'VIP过期检查任务',
        '0 0 2 * * ?',
        '每天凌晨2点执行，检查并更新过期的VIP会员状态，将isVip字段设置为false',
        'checkAndUpdateExpiredVIPTask',
        1,
        0,
        NOW(),
        NOW());

-- 说明：
-- taskKey: vip_expire_check - 任务的唯一标识
-- taskName: VIP过期检查任务 - 任务名称
-- taskCron: 0 0 2 * * ? - Cron表达式，表示每天凌晨2点执行
--   格式: 秒 分 时 日 月 星期
--   0 0 2 * * ? 表示每天的2:00:00执行
-- taskDesc: 任务描述
-- taskBean: checkAndUpdateExpiredVIPTask - 对应VIPTask类中定义的@Bean方法名
-- taskStatus: 1 - 任务状态开启
-- isDelete: 0 - 未删除

-- Cron表达式说明：
-- 如果需要修改执行时间，可以使用以下示例：
-- 每小时执行一次: 0 0 * * * ?
-- 每天凌晨1点执行: 0 0 1 * * ?
-- 每天中午12点执行: 0 0 12 * * ?
-- 每6小时执行一次: 0 0 */6 * * ?