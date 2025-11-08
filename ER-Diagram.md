# jdjm-picture-backend 数据库ER图

## 实体关系图

```mermaid
erDiagram
    USER ||--o{ PICTURE : "1:N"
    USER ||--o{ SPACE : "1:N"
    USER ||--o{ CATEGORY : "1:N"
    USER ||--o{ PICTURE_INTERACTION : "1:N"
    USER ||--o{ COMMENT : "1:N"
    USER ||--o{ COMMENT_NOTIFICATION : "1:N"
    USER ||--o{ DOWNLOAD_LOGS : "1:N"
    USER ||--|| VIP_MEMBERSHIPS : "1:1"
    USER ||--o{ VIP_REDEMPTION_CODES : "1:N"
    USER ||--o{ SPACE_USER : "1:N"

    SPACE ||--o{ PICTURE : "1:N"
    SPACE ||--o{ COMMENT : "1:N"
    SPACE ||--o{ SPACE_USER : "1:N"
    SPACE ||--o{ DOWNLOAD_LOGS : "1:N"

    PICTURE ||--o{ PICTURE_INTERACTION : "1:N"
    PICTURE ||--o{ COMMENT : "1:N"
    PICTURE ||--o{ DOWNLOAD_LOGS : "1:N"
    PICTURE }o--|| CATEGORY : "N:1"

    CATEGORY ||--o{ CATEGORY : "1:N"

    COMMENT ||--o{ COMMENT : "1:N"
    COMMENT ||--o{ COMMENT_NOTIFICATION : "1:N"

    USER {
        bigint id PK
        varchar userAccount UK "账号"
        varchar userPassword "密码"
        varchar userName "昵称"
        varchar userAvatar "头像"
        varchar userProfile "简介"
        varchar userRole "角色(user/admin)"
        varchar userEmail "邮箱"
        varchar userPhone UK "手机号"
        date birthday "出生日期"
        bigint vipNnumber "会员编号"
        datetime vipExpireTime "会员过期时间"
        varchar vipCode "会员兑换码"
        tinyint vipSign "会员标识"
        varchar shareCode "分享码"
        bigint inviteUserId "邀请用户ID"
        tinyint isDisabled "是否禁用"
        tinyint balance "余额"
        tinyint isDelete "是否删除"
        datetime createTime "创建时间"
        datetime updateTime "更新时间"
    }

    SPACE {
        bigint id PK
        varchar spaceName "空间名称"
        int spaceLevel "空间级别(0普通/1专业/2旗舰)"
        bigint maxSize "最大总大小"
        bigint maxCount "最大数量"
        bigint totalSize "当前总大小"
        bigint totalCount "当前数量"
        bigint userId FK "创建用户ID"
        int spaceType "空间类型(0私有/1团队)"
        tinyint isDelete "是否删除"
        datetime createTime "创建时间"
        datetime updateTime "更新时间"
    }

    SPACE_USER {
        bigint id PK
        bigint spaceId FK "空间ID"
        bigint userId FK "用户ID"
        varchar spaceRole "空间角色(viewer/editor/admin)"
        datetime createTime "创建时间"
        datetime updateTime "更新时间"
    }

    PICTURE {
        bigint id PK
        varchar url "图片URL"
        varchar name "图片名称"
        varchar introduction "简介"
        varchar category "分类"
        varchar tags "标签(JSON)"
        bigint picSize "图片体积"
        int picWidth "宽度"
        int picHeight "高度"
        double picScale "宽高比"
        varchar picFormat "格式"
        bigint userId FK "创建用户ID"
        bigint spaceId FK "空间ID(null为公共)"
        bigint categoryId FK "分类ID"
        varchar picColor "主色调"
        varchar thumbnailUrl "缩略图URL"
        int reviewStatus "审核状态(0待审/1通过/2拒绝)"
        varchar reviewMessage "审核信息"
        bigint reviewerId "审核人ID"
        datetime reviewTime "审核时间"
        tinyint resourceStatus "资源状态(0存在/1不存在)"
        int viewQuantity "查看数"
        int likeQuantity "点赞数"
        int collectQuantity "收藏数"
        int downloadQuantity "下载数"
        int shareQuantity "分享数"
        tinyint isShare "是否分享"
        tinyint expandStatus "扩图状态"
        decimal recommendScore "推荐得分"
        tinyint isDelete "是否删除"
        datetime createTime "创建时间"
        datetime updateTime "更新时间"
    }

    CATEGORY {
        bigint id PK
        varchar name "名称"
        bigint parentId FK "父分类ID(0为顶层)"
        int useNum "使用数量"
        bigint userId FK "创建用户ID"
        tinyint isDelete "是否删除"
        datetime createTime "创建时间"
        datetime updateTime "更新时间"
    }

    PICTURE_INTERACTION {
        bigint userId PK,FK "用户ID"
        bigint pictureId PK,FK "图片ID"
        tinyint interactionType PK "交互类型(0点赞/1收藏)"
        tinyint interactionStatus "交互状态(0存在/1取消)"
        tinyint isDelete "是否删除"
        datetime createTime "创建时间"
        datetime updateTime "更新时间"
    }

    COMMENT {
        bigint id PK
        bigint pictureId FK "图片ID"
        bigint userId FK "用户ID"
        bigint parentId FK "父评论ID(null为一级)"
        bigint rootId FK "根评论ID"
        text content "评论内容"
        tinyint status "状态(0正常/1删除/2违规)"
        int likeCount "点赞数"
        int replyCount "回复数"
        bigint spaceId FK "空间ID(null为公共)"
        tinyint isDelete "是否删除"
        datetime createTime "创建时间"
        datetime updateTime "更新时间"
    }

    COMMENT_NOTIFICATION {
        bigint id PK
        bigint userId FK "接收用户ID"
        bigint commentId FK "评论ID"
        bigint pictureId FK "图片ID"
        bigint triggerUserId FK "触发用户ID"
        tinyint notificationType "通知类型(0新评论/1回复/2通过/3拒绝)"
        varchar title "标题"
        text content "内容"
        tinyint isRead "是否已读"
        tinyint isDelete "是否删除"
        datetime createTime "创建时间"
        datetime updateTime "更新时间"
    }

    DOWNLOAD_LOGS {
        bigint id PK
        bigint user_id FK "用户ID"
        bigint file_id FK "文件ID"
        bigint space_id FK "空间ID(null为公共)"
        timestamp downloaded_at "下载时间"
    }

    VIP_MEMBERSHIPS {
        bigint id PK
        bigint user_id FK "用户ID"
        tinyint is_vip "是否VIP"
        timestamp vip_start_date "开始时间"
        timestamp vip_end_date "结束时间"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
    }

    VIP_REDEMPTION_CODES {
        bigint id PK
        varchar code UK "兑换码"
        tinyint is_used "是否已使用"
        bigint user_id FK "使用用户ID"
        datetime created_at "创建时间"
        datetime updated_at "更新时间"
        datetime used_at "使用时间"
    }

    SCHEDULED_TASK {
        bigint id PK
        varchar taskKey "任务KEY"
        varchar taskName "任务名称"
        varchar taskCron "Cron表达式"
        varchar taskDesc "任务描述"
        varchar taskBean "任务Bean"
        tinyint taskStatus "任务状态(0关闭/1开启)"
        tinyint isDelete "是否删除"
        datetime createTime "创建时间"
        datetime updateTime "更新时间"
    }
```

## 数据库表概览

### 核心业务表 (12张)

| 表名 | 中文名 | 说明 |
|-----|-------|------|
| user | 用户表 | 管理所有用户信息，包含VIP会员相关字段 |
| space | 空间表 | 多租户空间管理，支持私有和团队空间 |
| space_user | 空间用户关联表 | 管理用户与空间的多对多关系及角色权限 |
| picture | 图片表 | 核心业务实体，支持分片存储 |
| category | 分类表 | 支持树形结构的图片分类 |
| picture_interaction | 图片交互表 | 记录用户对图片的点赞和收藏行为 |
| comment | 评论表 | 支持嵌套回复的评论系统 |
| comment_notification | 评论通知表 | 评论相关的通知消息 |
| download_logs | 下载日志表 | 追踪图片下载记录 |
| vip_memberships | VIP会员表 | 管理VIP会员信息 |
| vip_redemption_codes | VIP兑换码表 | 管理VIP兑换码 |
| scheduled_task | 定时任务表 | 系统定时任务配置 |

## 关键关系说明

### 1. 用户与空间 (多对多)
- 通过 `space_user` 表实现
- 支持三种角色：`viewer`（查看者）、`editor`（编辑者）、`admin`（管理员）
- 用户可以创建自己的空间，也可以加入其他用户的团队空间

### 2. 图片的多维度关联
- **用户维度**：每张图片由一个用户上传（`picture.userId`）
- **空间维度**：图片可以属于某个空间或公共图库（`picture.spaceId` 为 null 表示公共）
- **分类维度**：图片可以关联到分类（`picture.categoryId`）
- **交互维度**：通过 `picture_interaction` 表记录点赞和收藏

### 3. 分类的层级结构
- `category` 表通过 `parentId` 自关联实现树形结构
- `parentId = 0` 表示顶层分类

### 4. 评论的嵌套回复
- `comment` 表通过 `parentId` 和 `rootId` 实现多层级回复
- `parentId` 为 null 表示一级评论
- `rootId` 指向根评论，用于快速查询整个评论树

### 5. VIP会员体系
- `user` 表中包含 VIP 相关字段（`vipSign`、`vipExpireTime`）
- `vip_memberships` 表记录详细的会员信息
- `vip_redemption_codes` 表管理兑换码的生成和使用

## 数据库设计特点

### 1. 分片策略
项目使用 **ShardingSphere** 按 `spaceId` 对 `picture` 表进行动态分片，提高了系统的可扩展性和查询性能。

### 2. 逻辑删除
所有主要表都实现了逻辑删除（`isDelete` 字段），保证数据的可恢复性。

### 3. 审核机制
`picture` 表包含完整的审核流程字段：
- `reviewStatus`：审核状态（0-待审核、1-通过、2-拒绝）
- `reviewMessage`：审核信息
- `reviewerId`：审核人ID
- `reviewTime`：审核时间

### 4. 统计数据实时更新
`picture` 表包含多个统计字段，支持实时更新：
- `viewQuantity`：查看数量
- `likeQuantity`：点赞数量
- `collectQuantity`：收藏数量
- `downloadQuantity`：下载数量
- `shareQuantity`：分享数量
- `recommendScore`：推荐综合得分

### 5. 完善的索引设计
为高频查询字段建立了索引，包括：
- **用户表**：userAccount、userPhone、userName
- **空间表**：userId、spaceName、spaceLevel、spaceType
- **图片表**：name、category、tags、userId、reviewStatus、spaceId
- **评论表**：pictureId、userId、parentId、rootId、spaceId

## 字段约定

| 字段 | 说明 |
|-----|------|
| `isDelete` | 逻辑删除标记（0-正常，1-删除） |
| `createTime` | 创建时间 |
| `updateTime` | 更新时间（自动更新） |
| `editTime` | 编辑时间 |
| `spaceId` | 空间ID（null 表示公共空间） |

## 查看ER图

上面的 Mermaid 代码可以在支持 Mermaid 的 Markdown 编辑器中渲染，例如：
- GitHub
- GitLab
- VS Code（安装 Mermaid 插件）
- Typora
- 在线 Mermaid 编辑器：https://mermaid.live/

---

**生成时间**: 2025-11-07
**数据库版本**: MySQL 8.0.27
**项目**: jdjm-picture-backend