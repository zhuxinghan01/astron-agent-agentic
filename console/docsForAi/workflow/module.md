---
module: 工作流
generated: 2026-02-28
---

# 工作流模块文档

## 1. 模块概述

工作流模块提供可视化的 AI 工作流编排能力，支持从模板创建、节点拖拽编排、流式执行、版本管理和发布。基于 ReactFlow 实现前端编排界面，后端通过 SSE 与 Agent 引擎交互执行工作流。

## 2. 后端 API 清单

### WorkflowChatController (`/api/v1/workflow`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/v1/workflow/chat/stream` | 需认证 | 启动工作流聊天流（SSE） |
| POST | `/api/v1/workflow/chat/resume` | 需认证 | 恢复中断的工作流聊天 |
| POST | `/api/v1/workflow/chat/stop/{streamId}` | 需认证 | 停止工作流聊天流 |
| GET | `/api/v1/workflow/chat/status` | 需认证 | 查询工作流聊天状态 |
| GET | `/api/v1/workflow/health` | - | 健康检查 |

### WorkflowBotController (`/workflow/bot`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/workflow/bot/templateGroup` | - | 获取工作流模板组 |
| POST | `/workflow/bot/createFromTemplate` | - | 从模板创建工作流助手 |
| POST | `/workflow/bot/templateList` | - | 获取工作流模板列表 |
| POST | `/workflow/bot/get-inputs-type` | - | 获取工作流输入类型 |

### ChatWorkflowController (`/workflow/web`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/workflow/web/info` | - | 获取工作流信息（含工具配置） |

## 3. 数据模型

### 表结构

| 表名 | Entity 类 | 说明 |
|------|----------|------|
| workflow_template_group | WorkflowTemplateGroup | 工作流模板组 |

### 关键字段

**WorkflowTemplateGroup**:
```java
@TableName("workflow_template_group")
private Integer id;
private String createUser;     // 创建用户
private String groupName;      // 组名称
private Integer sortOrder;     // 排序
private Integer status;        // 状态
private LocalDateTime createTime;
private LocalDateTime updateTime;
```

## 4. 后端核心类

| 类名 | 路径 | 职责 |
|------|------|------|
| WorkflowChatService | `hub/service/workflow/WorkflowChatService.java` | 工作流聊天流处理、SSE 管理 |
| WorkflowReleaseService | `hub/service/workflow/WorkflowReleaseService.java` | 工作流发布、版本管理 |
| WorkflowTemplateGroupService | `hub/service/workflow/WorkflowTemplateGroupService.java` | 模板组管理 |
| BotMaasService | `hub/service/workflow/BotMaasService.java` | MaaS 模板创建、查询 |
| BotChainService | `hub/service/workflow/BotChainService.java` | Bot 链式处理 |

### 关键业务逻辑

- **流式执行**: 通过 SseEmitter 与 Agent 引擎交互，实时推送工作流节点执行状态
- **模板创建**: 从预定义模板组中选择模板，一键创建工作流助手
- **版本管理**: 工作流支持多版本，发布时创建新版本快照
- **恢复执行**: 支持中断后恢复工作流执行（resume 接口）

## 5. 前端页面

| 页面 | 路由 | 组件路径 | 说明 |
|------|------|---------|------|
| 工作流编排 | `/work_flow/:id/arrange` | `src/pages/workflow/` | ReactFlow 可视化编排 |
| 工作流聊天 | `/chat/:botId/:version?` | `src/pages/chat/` | 工作流执行聊天 |

## 6. 前端状态管理

| Store | 路径 | 管理的状态 |
|-------|------|-----------|
| workflow store | `src/store/` (Zustand + Recoil) | 工作流节点、连线、执行状态 |

## 7. 前端 API Service

| 函数 | 路径 | 对应后端 API |
|------|------|-------------|
| getTemplateGroup | `src/services/flow.ts` | GET /workflow/bot/templateGroup |
| createFromTemplate | `src/services/flow.ts` | POST /workflow/bot/createFromTemplate |
| getTemplateList | `src/services/flow.ts` | POST /workflow/bot/templateList |
| workflowChatStream | `src/services/flow.ts` | POST /api/v1/workflow/chat/stream |
| getWorkflowInfo | `src/services/flow.ts` | GET /workflow/web/info |

## 8. 模块间依赖

- **依赖**: Bot 管理（工作流助手基于 Bot）、模型管理（节点使用模型）、AI 工具（节点调用工具）、知识库（RAG 节点）
- **被依赖**: 聊天（工作流聊天）、发布（发布工作流）
