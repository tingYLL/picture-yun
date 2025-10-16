-- 创建库
create database if not exists bear_picture;

-- 切换库
use bear_picture;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;


ALTER TABLE user
    ADD COLUMN userEmail      varchar(50)                           not null comment '用户邮箱',
    ADD COLUMN userPhone      varchar(50)                           null comment '用户手机号',
    ADD COLUMN birthday        date                                  null comment '出生日期',
    ADD COLUMN vipNnumber      bigint                                null comment '会员编号',
    ADD COLUMN vipExpireTime datetime                              null comment '会员过期时间',
    ADD COLUMN vipCode        varchar(20)                           null comment '会员兑换码',
    ADD COLUMN vipSign        varchar(20)                           null comment '会员标识（vip 表的类型字段）',
    ADD COLUMN shareCode      varchar(20)                           null comment '分享码',
    ADD COLUMN inviteUserId  bigint                                null comment '邀请用户 ID';

ALTER TABLE user ADD UNIQUE uk_userEmail (userEmail);
ALTER TABLE user ADD UNIQUE uk_userPhone (userPhone);



-- 图片表
create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    introduction varchar(512)                       null comment '简介',
    category     varchar(64)                        null comment '分类',
    tags         varchar(512)                       null comment '标签（JSON 数组）',
    picSize      bigint                             null comment '图片体积',
    picWidth     int                                null comment '图片宽度',
    picHeight    int                                null comment '图片高度',
    picScale     double                             null comment '图片宽高比例',
    picFormat    varchar(32)                        null comment '图片格式',
    userId       bigint                             not null comment '创建用户 id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),                 -- 提升基于图片名称的查询性能
    INDEX idx_introduction (introduction), -- 用于模糊搜索图片简介
    INDEX idx_category (category),         -- 提升基于分类的查询性能
    INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
    INDEX idx_userId (userId)              -- 提升基于用户 ID 的查询性能
) comment '图片' collate = utf8mb4_unicode_ci;

ALTER TABLE picture
    -- 添加新列
    ADD COLUMN reviewStatus INT DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    ADD COLUMN reviewMessage VARCHAR(512) NULL COMMENT '审核信息',
    ADD COLUMN reviewerId BIGINT NULL COMMENT '审核人 ID',
    ADD COLUMN reviewTime DATETIME NULL COMMENT '审核时间';

-- 创建基于 reviewStatus 列的索引
CREATE INDEX idx_reviewStatus ON picture (reviewStatus);

ALTER TABLE picture
    -- 添加新列
    ADD COLUMN thumbnailUrl varchar(512) NULL COMMENT '缩略图 url';

-- 空间表
create table if not exists space
(
    id         bigint auto_increment comment 'id' primary key,
    spaceName  varchar(128)                       null comment '空间名称',
    spaceLevel int      default 0                 null comment '空间级别：0-普通版 1-专业版 2-旗舰版',
    maxSize    bigint   default 0                 null comment '空间图片的最大总大小',
    maxCount   bigint   default 0                 null comment '空间图片的最大数量',
    totalSize  bigint   default 0                 null comment '当前空间下图片的总大小',
    totalCount bigint   default 0                 null comment '当前空间下的图片数量',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    -- 索引设计
    index idx_userId (userId),        -- 提升基于用户的查询效率
    index idx_spaceName (spaceName),  -- 提升基于空间名称的查询效率
    index idx_spaceLevel (spaceLevel) -- 提升按空间级别查询的效率
) comment '空间' collate = utf8mb4_unicode_ci;



-- 添加新列
ALTER TABLE picture
    ADD COLUMN spaceId bigint  null comment '空间 id（为空表示公共空间）';

-- 创建索引
CREATE INDEX idx_spaceId ON picture (spaceId);

-- 添加新列
ALTER TABLE picture
    ADD COLUMN picColor varchar(16) null comment '图片主色调';

-- 支持空间类型，添加新列
ALTER TABLE space
    ADD COLUMN spaceType int default 0 not null comment '空间类型：0-私有 1-团队';

CREATE INDEX idx_spaceType ON space (spaceType);

-- 空间成员表
create table if not exists space_user
(
    id         bigint auto_increment comment 'id' primary key,
    spaceId    bigint                                 not null comment '空间 id',
    userId     bigint                                 not null comment '用户 id',
    spaceRole  varchar(128) default 'viewer'          null comment '空间角色：viewer/editor/admin',
    createTime datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    -- 索引设计
    UNIQUE KEY uk_spaceId_userId (spaceId, userId), -- 唯一索引，用户在一个空间中只能有一个角色
    INDEX idx_spaceId (spaceId),                    -- 提升按空间查询的性能
    INDEX idx_userId (userId)                       -- 提升按用户查询的性能
) comment '空间用户关联' collate = utf8mb4_unicode_ci;

ALTER TABLE picture
    ADD COLUMN categoryId       bigint null comment '分类 ID',
    ADD COLUMN resourceStatus   tinyint        default 0                 not null comment '资源状态（0-存在 COS, 1-不存在 COS）',
    ADD COLUMN viewQuantity     int            default 0                 not null comment '查看数量',
    ADD COLUMN likeQuantity     int            default 0                 not null comment '点赞数量',
    ADD COLUMN collectQuantity  int            default 0                 not null comment '收藏数量',
    ADD COLUMN downloadQuantity int            default 0                 not null comment '下载数量',
    ADD COLUMN shareQuantity    int            default 0                 not null comment '分享数量',
    ADD COLUMN isShare          tinyint        default 0                 not null comment '是否分享（0-分享, 1-不分享）',
    ADD COLUMN expandStatus     tinyint        default 0                 not null comment '扩图状态（0-普通图片, 1-扩图图片, 2-扩图成功后的图片）',
    ADD COLUMN recommendScore   decimal(10, 4) default 0.0000            not null comment '推荐综合得分';


#  开发阶段 先移除
ALTER TABLE user DROP INDEX uk_userEmail;
ALTER TABLE user MODIFY COLUMN userEmail VARCHAR(50) NULL;
ALTER TABLE user
    ADD COLUMN isDisabled  tinyint default 0 not null comment '是否禁用（0-正常, 1-禁用）';

create table category
(
    id          bigint unsigned auto_increment comment '主键 ID'
        primary key,
    name        varchar(128)                              not null comment '名称',
    parentId   bigint unsigned default '0'               null comment '父分类 ID（0-表示顶层分类）',
    useNum     int             default 0                 not null comment '使用数量',
    userId     bigint                                    not null comment '创建用户 ID',
    isDelete   tinyint         default 0                 not null comment '是否删除（0-正常, 1-删除）',
    editTime   datetime        default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime datetime        default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '分类表' collate = utf8mb4_unicode_ci;

create index idx_name
    on category (name);

create table picture_interaction
(
    userId            bigint                             not null comment '用户 ID',
    pictureId         bigint                             not null comment '图片 ID',
    interactionType   tinyint                            not null comment '交互类型（0-点赞, 1-收藏）',
    interactionStatus tinyint                            not null comment '交互状态（0-存在, 1-取消）',
    isDelete          tinyint  default 0                 not null comment '是否删除（0-正常, 1-删除）',
    editTime          datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime        datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime        datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    primary key (userId, pictureId, interactionType)
)
    comment '图片交互表' collate = utf8mb4_unicode_ci;

create table scheduled_task
(
    id          bigint unsigned auto_increment comment '主键ID'
        primary key,
    tasKey    varchar(255)                       not null comment '任务 KEY（存在内存中）',
    taskName   varchar(255)                       not null comment '任务名称',
    taskCron   varchar(255)                       not null comment '任务 corn 表达式',
    taskDesc   varchar(255)                       null comment '任务描述',
    taskBean   varchar(255)                       not null comment '任务 Bean 名称（执行任务的 bean）',
    taskStatus tinyint  default 0                 not null comment '任务状态（0-关闭, 1-开启）',
    isDelete   tinyint  default 0                 not null comment '是否删除（0-正常, 1-删除）',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '定时任务表' collate = utf8mb4_unicode_ci;

ALTER TABLE scheduled_task
    CHANGE COLUMN tasKey taskKey VARCHAR(255) NOT NULL COMMENT '任务 KEY（存在内存中）';

-- VIP会员表
CREATE TABLE IF NOT EXISTS vip_memberships (
                                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                               user_id BIGINT NOT NULL,
                                               is_vip BOOLEAN DEFAULT FALSE,
                                               vip_start_date TIMESTAMP NULL,
                                               vip_end_date TIMESTAMP NULL,
                                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- VIP兑换码表
CREATE TABLE IF NOT EXISTS vip_redemption_codes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  code VARCHAR(64) NOT NULL COMMENT '兑换码',
  is_used TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已使用',
  user_id BIGINT DEFAULT NULL COMMENT '使用兑换码的用户ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_code (code),
  KEY idx_user_id (user_id)
)comment 'VIP兑换码' collate = utf8mb4_unicode_ci;

ALTER TABLE vip_redemption_codes ADD COLUMN used_at DATETIME NULL COMMENT '使用时间';


-- 下载记录表
CREATE TABLE IF NOT EXISTS download_logs (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                             user_id BIGINT NOT NULL,
                                             file_id BIGINT NOT NULL,
                                             downloaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER  TABLE  user ADD COLUMN  balance  tinyint      default 0                 not null comment '余额';