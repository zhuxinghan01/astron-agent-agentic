---
module: 用户管理
generated: 2026-02-28
---

# 用户管理模块文档

## 1. 模块概述

用户管理模块负责用户信息维护和用户维度的助手管理。包括获取当前用户信息、更新个人资料、用户协议确认，以及"我的助手"列表管理。认证由 Casdoor SSO 处理，本模块只管理用户业务数据。

## 2. 后端 API 清单

### UserInfoController (`/user-info`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/user-info/me` | 需认证 | 获取当前用户信息 |
| POST | `/user-info/update` | 需认证 | 更新用户基本信息（昵称、头像） |
| POST | `/user-info/agreement` | 需认证 | 同意用户协议 |

### MyBotController (`/my-bot`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/my-bot/list` | @SpacePreAuth | 获取用户创建的智能体列表（分页） |
| POST | `/my-bot/delete` | @SpacePreAuth | 删除用户的智能体 |
| POST | `/my-bot/bot-detail` | @SpacePreAuth | 获取智能体详细信息 |

## 3. 数据模型

### 关键字段

**UserInfo**:
```java
private Long id;               // 用户ID
private String uid;            // 用户唯一标识
private String username;       // 用户名
private String avatar;         // 头像URL
private String nickname;       // 昵称
private String mobile;         // 手机号
private Integer accountStatus; // 账户状态
private Integer userAgreement; // 用户协议状态（0=未同意, 1=已同意）
private LocalDateTime createTime;
private LocalDateTime updateTime;
```

## 4. 后端核心类

| 类名 | 路径 | 职责 |
|------|------|------|
| UserInfoController | `hub/controller/user/UserInfoController.java` | 用户信息 CRUD |
| MyBotController | `hub/controller/user/MyBotController.java` | 我的助手管理 |
| UserBotService | `hub/service/user/UserBotService.java` | 用户助手业务逻辑 |

### 关键业务逻辑

- **用户信息获取**: 从 JWT Token 中解析 uid，查询用户业务数据
- **用户协议**: 首次登录需同意用户协议，状态持久化到数据库
- **我的助手**: 按空间维度查询用户创建的助手列表，支持分页

## 5. 前端页面

| 页面 | 路由 | 组件路径 | 说明 |
|------|------|---------|------|
| 个人设置 | - | `src/pages/space/` | 用户信息编辑 |

## 6. 前端状态管理

| Store | 路径 | 管理的状态 |
|-------|------|-----------|
| user-store | `src/store/user-store.tsx` | 用户信息、登录状态、空间角色 |

**Store 核心结构**:
```typescript
interface User {
  id: number;
  uid: string;
  username: string;
  avatar: string;
  nickname: string;
  mobile: string;
  accountStatus: number;
  userAgreement: number;
  spaceType?: SpaceType;
  roleType?: RoleType;
  spaceId?: string;
}
```

## 7. 前端 API Service

| 函数 | 路径 | 对应后端 API |
|------|------|-------------|
| getCurrentUser | `src/services/user.ts` | GET /user-info/me |
| updateUserInfo | `src/services/user.ts` | POST /user-info/update |
| agreeAgreement | `src/services/user.ts` | POST /user-info/agreement |
| getMyBotList | `src/services/user.ts` | POST /my-bot/list |

## 8. 模块间依赖

- **依赖**: 无（基础模块）
- **被依赖**: 空间管理、企业管理、Bot 管理（用户上下文）
