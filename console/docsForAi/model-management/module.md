---
module: 模型管理
generated: 2026-02-28
---

# 模型管理模块文档

## 1. 模块概述

模型管理模块负责 AI 模型的配置和管理，包括模型供应商管理、模型参数配置、模型列表查询等。为 Bot 创建和工作流节点提供模型选择能力。

## 2. 后端 API 清单

### ModelController (`/model`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/model/list` | - | 获取模型列表 |
| GET | `/model/detail/{modelId}` | - | 获取模型详情 |
| POST | `/model/config/save` | @SpacePreAuth(ADMIN) | 保存模型配置 |
| GET | `/model/config/list` | - | 获取模型配置列表 |
| GET | `/model/providers` | - | 获取模型供应商列表 |

## 3. 数据模型

### 关键概念

**模型供应商**:
```
ModelProvider:
  - 讯飞星火 (iFlytek Spark)
  - OpenAI
  - 其他第三方供应商
```

**模型参数**:
```
ModelConfig:
  - temperature: 温度（创造性）
  - topP: 核采样
  - maxTokens: 最大 token 数
  - presencePenalty: 存在惩罚
  - frequencyPenalty: 频率惩罚
```

## 4. 后端核心类

| 类名 | 路径 | 职责 |
|------|------|------|
| ModelController | `hub/controller/model/ModelController.java` | 模型管理入口 |
| ModelService | `hub/service/model/` | 模型业务逻辑 |

### 关键业务逻辑

- **模型列表**: 按供应商分组展示可用模型，支持搜索过滤
- **模型配置**: 空间级别的模型参数配置，管理员可自定义默认参数
- **供应商管理**: 支持多供应商接入，统一模型调用接口

## 5. 前端页面

| 页面 | 路由 | 组件路径 | 说明 |
|------|------|---------|------|
| 模型管理 | `/management/model` | `src/pages/management/` | 模型配置和管理 |

## 6. 前端状态管理

| Store | 路径 | 管理的状态 |
|-------|------|-----------|
| - | 页面本地 state | 模型列表、当前配置 |

## 7. 前端 API Service

| 函数 | 路径 | 对应后端 API |
|------|------|-------------|
| getModelList | `src/services/model.ts` | GET /model/list |
| getModelDetail | `src/services/model.ts` | GET /model/detail/{modelId} |
| saveModelConfig | `src/services/model.ts` | POST /model/config/save |
| getModelProviders | `src/services/model.ts` | GET /model/providers |

## 8. 模块间依赖

- **依赖**: 空间管理（空间级配置）
- **被依赖**: Bot 管理（选择模型）、工作流（节点使用模型）
