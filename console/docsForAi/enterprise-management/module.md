---
module: 企业管理
generated: 2026-02-28
---

# 企业管理模块文档

## 1. 模块概述

企业管理模块负责团队（企业）的创建和管理，包括团队信息维护、成员管理、邀请机制、申请审批和权限控制。企业是企业空间的上层组织概念，一个企业可以拥有多个企业空间。

## 2. 后端 API 清单

### EnterpriseController (`/enterprise`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/enterprise/create` | - | 创建团队 |
| POST | `/enterprise/update-name` | @EnterprisePreAuth(ADMIN) | 更新团队名称 |
| POST | `/enterprise/update-logo` | @EnterprisePreAuth(ADMIN) | 更新团队 Logo |
| GET | `/enterprise/check-need-create-team` | - | 检查是否需要创建团队 |
| GET | `/enterprise/info` | - | 获取企业信息 |

### EnterpriseUserController (`/enterprise/user`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/enterprise/user/list` | @EnterprisePreAuth | 获取企业成员列表 |
| POST | `/enterprise/user/remove` | @EnterprisePreAuth(ADMIN) | 移除企业成员 |
| POST | `/enterprise/user/update-role` | @EnterprisePreAuth(ADMIN) | 更新成员角色 |

### EnterprisePermissionController (`/enterprise/permission`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/enterprise/permission/check` | - | 检查企业权限 |

### InviteRecordController (`/invite`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/invite/create` | @EnterprisePreAuth(ADMIN) | 创建邀请链接 |
| POST | `/invite/accept` | - | 接受邀请 |
| GET | `/invite/info` | - | 获取邀请信息 |

### ApplyRecordController (`/apply`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/apply/create` | - | 提交加入申请 |
| POST | `/apply/approve` | @EnterprisePreAuth(ADMIN) | 审批申请 |
| POST | `/apply/list` | @EnterprisePreAuth(ADMIN) | 获取申请列表 |

## 3. 数据模型

### 关键字段

**Enterprise 核心概念**:
```
EnterpriseRoleType:
  - OWNER: 企业所有者
  - ADMIN: 管理员
  - MEMBER: 普通成员

InviteStatus:
  - PENDING: 待接受
  - ACCEPTED: 已接受
  - EXPIRED: 已过期

ApplyStatus:
  - PENDING: 待审批
  - APPROVED: 已通过
  - REJECTED: 已拒绝
```

## 4. 后端核心类

| 类名 | 路径 | 职责 |
|------|------|------|
| EnterpriseController | `hub/controller/space/EnterpriseController.java` | 企业 CRUD |
| EnterpriseUserController | `hub/controller/space/EnterpriseUserController.java` | 企业成员管理 |
| EnterprisePermissionController | `hub/controller/space/EnterprisePermissionController.java` | 权限检查 |
| InviteRecordController | `hub/controller/space/InviteRecordController.java` | 邀请管理 |
| ApplyRecordController | `hub/controller/space/ApplyRecordController.java` | 申请审批 |
| EnterpriseBizService | `hub/service/space/EnterpriseBizService.java` | 企业业务逻辑 |

### 关键业务逻辑

- **创建团队**: 创建企业后自动创建默认企业空间，创建者成为 OWNER
- **邀请机制**: 管理员生成邀请链接，被邀请人点击链接加入团队
- **申请审批**: 用户主动申请加入，管理员审批通过/拒绝
- **权限控制**: @EnterprisePreAuth 注解校验用户在企业中的角色

## 5. 前端页面

| 页面 | 路由 | 组件路径 | 说明 |
|------|------|---------|------|
| 企业空间 | `/enterprise/:enterpriseId` | `src/pages/space/enterprise/` | 企业空间管理 |
| 团队设置 | - | `src/pages/space/enterprise/components/` | 团队信息和成员管理 |

## 6. 前端状态管理

| Store | 路径 | 管理的状态 |
|-------|------|-----------|
| enterprise-store | `src/store/enterprise-store.ts` | 当前企业ID、企业信息、成员列表 |

## 7. 前端 API Service

| 函数 | 路径 | 对应后端 API |
|------|------|-------------|
| createEnterprise | `src/services/enterprise.ts` | POST /enterprise/create |
| getEnterpriseInfo | `src/services/enterprise.ts` | GET /enterprise/info |
| getEnterpriseUsers | `src/services/enterprise.ts` | POST /enterprise/user/list |
| createInvite | `src/services/enterprise.ts` | POST /invite/create |
| acceptInvite | `src/services/enterprise.ts` | POST /invite/accept |

## 8. 模块间依赖

- **依赖**: 用户管理（用户信息）
- **被依赖**: 空间管理（企业空间关联企业）
