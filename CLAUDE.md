# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导。

## 项目概述

这是一个基于 Java 8 和 Maven 构建的 Spring Boot 图片管理后端服务（`jdjm-picture-backend`）。该应用为用户和空间提供图片存储、管理、搜索和 AI 驱动的功能。

## 开发命令

### 构建和运行
- **构建项目**: `mvn clean compile`
- **打包应用**: `mvn clean package`
- **运行应用**: `mvn spring-boot:run`
- **使用特定配置文件运行**: `mvn spring-boot:run -Dspring-boot.run.profiles=local`

### 测试
- **运行所有测试**: `mvn test`
- **运行特定测试类**: `mvn test -Dtest=ClassName`
- **构建时跳过测试**: `mvn clean package -DskipTests`

### 数据库操作
- **创建表**: 执行 `sql/create_table.sql` 中的 SQL 脚本
- **应用运行地址**: `http://localhost:8123/api`
- **API 文档**: `http://localhost:8123/api/doc.html` (Knife4j Swagger UI)

## 架构概述

### 核心技术
- **Spring Boot 2.7.6** 配合 Java 8
- **MyBatis Plus 3.5.10.1** 用于 ORM 和分页支持
- **MySQL** 作为主数据库
- **Redis** 用于会话存储和缓存
- **Sa-Token** 用于认证和授权
- **ShardingSphere** 用于数据库分片（按 spaceId 分割 picture 表）
- **Knife4j** 用于 API 文档
- **腾讯云 COS** 用于对象存储

### 核心功能
- 多租户空间管理，支持基于角色的权限控制
- 图片上传、管理和搜索（按颜色、相似图片搜索）
- AI 驱动的图像处理（阿里云 AI 集成）
- VIP 会员系统，支持下载跟踪
- 动态数据库分片以提高可扩展性

### 包结构
```
com.jdjm.jdjmpicturebackend/
├── annotation/          # 自定义注解 (@AuthCheck, @SaSpaceCheckPermission)
├── aop/                # 面向切面编程 (AuthInterceptor)
├── api/                # 外部 API 集成 (阿里云 AI, 图片搜索)
├── common/             # 通用类 (BaseResponse, ResultUtils)
├── config/             # 配置类 (CORS, MyBatis, COS, MinIO)
├── controller/         # REST API 端点
├── exception/          # 异常处理 (BusinessException, GlobalExceptionHandler)
├── manager/            # 业务逻辑管理器 (认证, 分片, 上传)
├── mapper/             # MyBatis 映射器
├── model/              # 数据模型 (entity, dto, vo, enums)
├── service/            # 服务层接口和实现
└── utils/              # 工具类
```

### 认证与授权
- 使用 **Sa-Token** 进行用户认证，配合 Redis 会话存储
- **空间级权限** 支持基于角色的访问控制
- 自定义注解 `@AuthCheck` 和 `@SaSpaceCheckPermission` 用于权限验证
- 会话超时：30 分钟

### 数据库分片
- Picture 表使用 `PictureShardingAlgorithm` 按 `spaceId` 动态分片
- 分片配置在 `application.yml` 的 `shardingsphere` 部分
- 自定义分片管理器位于 `manager/sharding/` 包中

### 存储选项
- **本地存储**: 可配置的上传目录（默认：`D:\pictures`）
- **腾讯云 COS**: 生产环境的云对象存储
- **MinIO**: 备选对象存储方案

### 配置文件
- **默认配置**: 本地开发环境，使用 localhost:3306 的 MySQL
- **生产环境**: 使用 `application-prod.yml`，配置远程数据库和 COS 凭据

## 开发注意事项

### 环境配置
1. MySQL 数据库运行在 localhost:3306（或在 application.yml 中配置）
2. Redis 服务器运行在 localhost:6379
3. Java 8+ 和 Maven 3.6+

### API 文档
- Knife4j UI 可在 `/api/doc.html` 访问
- 生产环境需要基本认证（admin/cvb147258cvb）

### 数据库架构
- 表通过 `sql/` 目录中的 SQL 脚本创建
- 所有实体使用逻辑删除（isDelete 字段）
- MyBatis Plus 处理自动 CRUD 操作

### 文件上传限制
- 最大文件大小：10MB（可在 application.yml 中配置）
- 支持本地和云存储选项