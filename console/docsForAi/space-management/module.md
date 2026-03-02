---
module: 空间管理
generated: 2026-02-28
---

# 空间管理模块文档

## 1. 模块概述

空间管理是平台多租户体系的核心，支持个人空间和企业空间两种模式。个人空间为用户私有工作区，企业空间为团队协作工作区。所有业务操作都在空间上下文中进行，通过请求头 `space-id` 传递。

## 2. 后端 API 清单

### SpaceController (`/space`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/space/personal-list` | - | 获取个人空间列表 |
| GET | `/space/corporate-list` | - | 获取企业空间列表 |
| POST | `/space/create-personal-space` | - | 创建个人空间 |
| POST | `/space/create-corporate-space` | - | 创建企业空间 |
| DELETE | `/space/delete-corporate-space` | - | 删除企业空间 |

### SpaceUserController (`/space/user`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/space/user/list` | @SpacePreAuth | 获取空间成员列表 |
| POST | `/space/user/add` | @SpacePreAuth(ADMIN) | 添加空间成员 |
| POST | `/space/user/remove` | @SpacePreAuth(ADMIN) | 移除空间成员 |
| POST | `/space/user/update-role` | @SpacePreAuth(ADMIN) | 更新成员角色 |

### SpacePermissionController (`/space/permission`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/space/permission/check` | - | 检查当前用户在空间中的权限 |

## 3. 数据模型

### 关键字段

**Space 核心概念**:
```
SpaceType:
  - PERSONAL: 个人空间
  - CORPORATE: 企业空间

RoleType:
  - OWNER: 空间所有者
  - ADMIN: 管理员
  - MEMBER: 普通成员
```

## 4. 后端核心类

| 类名 | 路径 | 职责 |
|------|------|------|
| SpaceController | `hub/controller/space/SpaceController.java` | 空间 CRUD |
| SpaceUserController | `hub/controller/space/SpaceUserController.java` | 空间成员管理 |
| SpacePermissionController | `hub/controller/space/SpacePermissionController.java` | 权限检查 |
| SpaceBizService | `hub/service/space/SpaceBizService.java` | 空间业务逻辑 |

### 关键业务逻辑

- **空间切换**: 前端通过 space-store 管理当前空间上下文，切换时更新请求头
- **权限控制**: @SpacePreAuth 注解在 AOP 层拦截，校验用户在当前空间的角色
- **个人空间**: 用户注册后自动创建，不可删除
- **企业空间**: 需要先创建企业（团队），再创建企业空间

## 5. 前端页面

| 页面 | 路由 | 组件路径 | 说明 |
|------|------|---------|------|
| 空间管理 | `/space` | `src/pages/space/` | 空间设置和成员管理 |
| 空间切换 | - | `src/layouts/` | 顶部导航栏空间切换器 |

## 6. 前端状态管理

| Store | 路径 | 管理的状态 |
|-------|------|-----------|
| space-store | `src/store/space-store.ts` | 当前空间ID、空间类型、空间列表、角色信息 |

**Store 核心结构**:
```typescript
interface SpaceStore {
  spaceId: string;
  spaceType: SpaceType;       // 'personal' | 'corporate'
  roleType: RoleType;         // 'owner' | 'admin' | 'member'
  personalSpaces: Space[];
  corporateSpaces: Space[];
  switchSpace: (spaceId: string) => void;
}
```

## 7. 前端 API Service

| 函数 | 路径 | 对应后端 API |
|------|------|-------------|
| getPersonalSpaces | `src/services/space.ts` | GET /space/personal-list |
| getCorporateSpaces | `src/services/space.ts` | GET /space/corporate-list |
| createPersonalSpace | `src/services/space.ts` | POST /space/create-personal-space |
| createCorporateSpace | `src/services/space.ts` | POST /space/create-corporate-space |

## 8. 模块间依赖

- **依赖**: 用户管理（用户信息）、企业管理（企业空间关联企业）
- **被依赖**: 所有模块（所有业务操作都在空间上下文中）
