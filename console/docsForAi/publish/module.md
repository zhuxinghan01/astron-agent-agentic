---
module: 发布管理
generated: 2026-02-28
---

# 发布管理模块文档

## 1. 模块概述

发布管理模块负责助手/工作流的多渠道发布，支持发布到应用市场（Market）、API 接口、微信公众号、MCP 等渠道。包含版本管理、发布统计、API Key 管理等功能。

## 2. 后端 API 清单

### BotPublishController (`/publish`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/publish/bots` | - | 获取发布的助手列表（分页、过滤） |
| GET | `/publish/bots/{botId}` | - | 获取助手发布详情 |
| POST | `/publish/create` | @SpacePreAuth | 创建发布 |
| POST | `/publish/update` | @SpacePreAuth | 更新发布配置 |
| POST | `/publish/delete` | @SpacePreAuth | 删除发布 |
| GET | `/publish/channels` | - | 获取可用发布渠道 |
| GET | `/publish/stats/{botId}` | - | 获取发布统计数据 |
| POST | `/publish/version/create` | @SpacePreAuth | 创建新版本 |
| GET | `/publish/version/list/{botId}` | - | 获取版本列表 |

### PublishApiController (`/publish/api`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/publish/api/create-key` | @SpacePreAuth | 创建 API Key |
| POST | `/publish/api/delete-key` | @SpacePreAuth | 删除 API Key |
| GET | `/publish/api/keys/{botId}` | - | 获取 API Key 列表 |

## 3. 数据模型

### 关键概念

**发布渠道**:
```
PublishChannel:
  - MARKET: 应用市场
  - API: API 接口
  - WECHAT: 微信公众号
  - MCP: MCP 协议
```

**发布状态**:
```
PublishStatus:
  - DRAFT: 草稿
  - PUBLISHED: 已发布
  - OFFLINE: 已下线
```

## 4. 后端核心类

| 类名 | 路径 | 职责 |
|------|------|------|
| BotPublishController | `hub/controller/publish/BotPublishController.java` | 发布管理入口 |
| PublishApiController | `hub/controller/publish/PublishApiController.java` | API 发布管理 |
| WorkflowReleaseService | `hub/service/workflow/WorkflowReleaseService.java` | 工作流发布逻辑 |

### 关键业务逻辑

- **多渠道发布**: 同一助手可发布到多个渠道，每个渠道独立配置
- **版本管理**: 每次发布创建版本快照，支持版本回滚
- **API Key**: 为 API 渠道生成访问密钥，支持多 Key 管理
- **发布统计**: 统计调用次数、活跃用户等数据

## 5. 前端页面

| 页面 | 路由 | 组件路径 | 说明 |
|------|------|---------|------|
| 发布管理 | `/management/release` | `src/pages/management/` | 发布配置和管理 |
| API 管理 | `/management/bot-api` | `src/pages/management/` | API Key 和调用管理 |

## 6. 前端状态管理

| Store | 路径 | 管理的状态 |
|-------|------|-----------|
| - | 页面本地 state | 发布配置、版本列表、API Key 列表 |

## 7. 前端 API Service

| 函数 | 路径 | 对应后端 API |
|------|------|-------------|
| getPublishBots | `src/services/publish.ts` | GET /publish/bots |
| createPublish | `src/services/publish.ts` | POST /publish/create |
| getPublishChannels | `src/services/publish.ts` | GET /publish/channels |
| createApiKey | `src/services/publish.ts` | POST /publish/api/create-key |
| getApiKeys | `src/services/publish.ts` | GET /publish/api/keys/{botId} |

## 8. 模块间依赖

- **依赖**: Bot 管理（发布的助手）、工作流（发布工作流）
- **被依赖**: 无（终端输出模块）
