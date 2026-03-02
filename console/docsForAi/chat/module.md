---
module: 聊天
generated: 2026-02-28
---

# 聊天模块文档

## 1. 模块概述

聊天模块是平台的核心交互入口，提供基于 SSE (Server-Sent Events) 的流式聊天能力。支持多轮对话、文件上传、工作流执行、聊天历史管理、对话树结构等功能。

## 2. 后端 API 清单

### ChatMessageController (`/chat-message`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/chat-message/chat` | 需认证 | 流式聊天（SSE），返回 `text/event-stream` |
| POST | `/chat-message/re-answer` | 需认证 | 重新回答某个问题 |
| POST | `/chat-message/bot-debug` | 需认证 | 调试机器人（SSE） |
| POST | `/chat-message/stop` | 需认证 | 中止当前流式生成 |
| GET | `/chat-message/clear` | 需认证 | 清除聊天历史并创建新对话 |

### ChatListController (`/chat-list`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/chat-list/all-chat-list` | - | 获取用户所有聊天记录 |
| POST | `/chat-list/v1/create-chat-list` | - | 创建新的聊天会话 |
| POST | `/chat-list/v1/del-chat-list` | - | 逻辑删除聊天记录 |
| GET | `/chat-list/v1/get-bot-info` | - | 获取机器人配置信息 |

### ChatHistoryController (`/chat-history`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/chat-history/all/{chatId}` | - | 获取指定 chatId 的完整聊天历史 |

### ChatEnhanceController (`/chat-enhance`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/chat-enhance/save-file` | - | 上传并绑定文件到聊天 |
| POST | `/chat-enhance/unbind-file` | - | 从聊天中解绑文件 |

### ChatRestartController (`/chat-restart`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/chat-restart/restart` | - | 基于现有 chatId 创建新的对话树 |

### ChatWorkflowController (`/workflow/web`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/workflow/web/info` | - | 获取机器人工作流配置 |

## 3. 数据模型

### 表结构

| 表名 | Entity 类 | 说明 |
|------|----------|------|
| chat_list | ChatList | 聊天会话列表 |
| chat_tree_index | ChatTreeIndex | 对话树索引（多轮对话关系） |
| chat_file_user | ChatFileUser | 用户上传的聊天文件 |
| chat_file_req | ChatFileReq | 文件与聊天请求的关联 |

### 关键字段

**ChatList**:
```java
@TableName("chat_list")
private Long id;              // 主键
private String chatId;        // 聊天会话ID
private Integer botId;        // 关联的助手ID
private String uid;           // 用户ID
private String title;         // 聊天标题
private Integer deleted;      // 逻辑删除
private LocalDateTime createTime;
private LocalDateTime updateTime;
```

**ChatTreeIndex**:
```java
@TableName("chat_tree_index")
private Long id;
private String rootChatId;    // 根对话ID
private String parentChatId;  // 父对话ID
private String childChatId;   // 子对话ID
private Integer round;        // 对话轮次
```

## 4. 后端核心类

| 类名 | 路径 | 职责 |
|------|------|------|
| ChatMessageController | `hub/controller/chat/ChatMessageController.java` | SSE 流式聊天入口 |
| ChatListController | `hub/controller/chat/ChatListController.java` | 聊天列表 CRUD |
| ChatHistoryController | `hub/controller/chat/ChatHistoryController.java` | 聊天历史查询 |
| ChatEnhanceController | `hub/controller/chat/ChatEnhanceController.java` | 文件上传/解绑 |
| ChatRestartController | `hub/controller/chat/ChatRestartController.java` | 对话重启 |

### 关键业务逻辑

- **SSE 流式聊天**: 使用 SseEmitter 实现服务端推送，支持实时流式输出
- **对话树结构**: 通过 ChatTreeIndex 维护多轮对话的树形关系（root → parent → child）
- **文件绑定**: 文件先上传到 S3（MinIO），再通过 ChatFileReq 关联到具体聊天请求
- **停止生成**: 通过 streamId 标识当前流，调用 stop 接口中止
- **聊天历史**: 返回多模态问答历史，包含溯源信息（ChatTraceSource）和推理记录（ChatReasonRecords）

## 5. 前端页面

| 页面 | 路由 | 组件路径 | 说明 |
|------|------|---------|------|
| 聊天主页 | `/chat/:botId/:version?` | `src/pages/chat/` | 聊天交互界面 |
| 聊天列表 | - | `src/pages/chat/components/ChatList/` | 左侧聊天列表 |
| 消息区域 | - | `src/pages/chat/components/MessageArea/` | 消息展示区 |
| 输入框 | - | `src/pages/chat/components/InputArea/` | 消息输入区 |

## 6. 前端状态管理

| Store | 路径 | 管理的状态 |
|-------|------|-----------|
| chat-store | `src/store/chat-store.ts` | 当前聊天会话、消息列表、流式状态 |

## 7. 前端 API Service

| 函数 | 路径 | 对应后端 API |
|------|------|-------------|
| chatStream | `src/services/chat.ts` | POST /chat-message/chat (SSE) |
| getChatList | `src/services/chat.ts` | POST /chat-list/all-chat-list |
| createChatList | `src/services/chat.ts` | POST /chat-list/v1/create-chat-list |
| deleteChatList | `src/services/chat.ts` | POST /chat-list/v1/del-chat-list |
| getChatHistory | `src/services/chat.ts` | GET /chat-history/all/{chatId} |
| uploadFileBindChat | `src/services/chat.ts` | POST /chat-enhance/save-file |
| stopGenerate | `src/services/chat.ts` | POST /chat-message/stop |

## 8. 模块间依赖

- **依赖**: Bot 管理（获取助手配置）、工作流（工作流聊天）、知识库（RAG 检索）
- **被依赖**: 无（聊天是终端消费模块）
