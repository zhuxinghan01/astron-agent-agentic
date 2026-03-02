---
module: Bot 管理
generated: 2026-02-28
---

# Bot 管理模块文档

## 1. 模块概述

Bot 管理是平台核心模块，负责 AI 助手的全生命周期管理，包括创建、配置、复制、删除、人设设置、语音训练、收藏等功能。支持 AI 辅助创建（一句话创建、AI 生成头像/开场白）。

## 2. 后端 API 清单

### BotController (`/workflow`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/workflow/base-save` | - | 保存助手基本信息（创建或更新） |
| POST | `/workflow/copy-bot` | @SpacePreAuth | 复制助手 |
| POST | `/workflow/maas-update` | - | 更新 MaaS 工作流信息 |

### BotCreateController (`/bot`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/bot/create` | @RateLimit | 创建助手 |
| POST | `/bot/update` | @RateLimit | 更新助手 |
| POST | `/bot/delete` | @RateLimit | 删除助手 |
| GET | `/bot/list` | - | 获取助手列表 |
| GET | `/bot/detail` | - | 获取助手详情 |
| POST | `/bot/ai-avatar-gen` | - | AI 生成助手头像 |
| POST | `/bot/ai-prologue-gen` | - | AI 生成开场白 |
| POST | `/bot/ai-sentence-gen` | - | 一句话创建助手 |
| POST | `/bot/generate-input-example` | - | 生成输入示例 |
| GET | `/bot/bot-model` | - | 获取模型列表 |
| POST | `/bot/type-list` | - | 获取助手类型列表 |
| POST | `/bot/template` | - | 获取模板数据 |

### BotFavoriteController (`/bot/favorite`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/bot/favorite/list` | - | 获取收藏助手列表 |
| POST | `/bot/favorite/create` | - | 添加收藏 |
| POST | `/bot/favorite/delete` | - | 取消收藏 |

### PersonalityController (`/personality`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/personality/aiGenerate` | - | AI 生成人设描述 |
| POST | `/personality/aiPolishing` | - | AI 润色人设描述 |
| GET | `/personality/getCategory` | - | 获取人设分类列表 |
| GET | `/personality/getRole` | - | 获取分类对应的人设角色 |

### SpeakerTrainController (`/speaker/train`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/speaker/train/create` | - | 创建发音人训练任务 |
| POST | `/speaker/train/list` | - | 获取发音人列表 |
| POST | `/speaker/train/delete` | - | 删除发音人 |
| POST | `/speaker/train/audition` | - | 试听发音人 |

### TalkAgentController (`/talk-agent`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/talk-agent/create` | - | 创建对话助手 |
| POST | `/talk-agent/update` | - | 更新对话助手 |
| GET | `/talk-agent/detail` | - | 获取对话助手详情 |

### VoiceApiController (`/voice`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/voice/list` | - | 获取语音列表 |
| POST | `/voice/audition` | - | 语音试听 |

## 3. 数据模型

### 关键 Entity

Bot 相关实体主要在 commons 模块中定义，通过 DTO 在 hub 模块中使用。

**BotInfoDto 核心字段**:
```java
private Integer botId;          // 助手ID
private String botName;         // 助手名称
private String botDesc;         // 助手描述
private String botAvatar;       // 助手头像
private String personality;     // 人设描述
private String prologue;        // 开场白
private Integer botType;        // 助手类型
private String modelId;         // 使用的模型ID
private Long spaceId;           // 所属空间
private String createUser;      // 创建者
```

## 4. 后端核心类

| 类名 | 路径 | 职责 |
|------|------|------|
| BotController | `hub/controller/bot/BotController.java` | 助手基础操作（保存、复制） |
| BotCreateController | `hub/controller/bot/BotCreateController.java` | 助手 CRUD + AI 辅助创建 |
| BotFavoriteController | `hub/controller/bot/BotFavoriteController.java` | 收藏管理 |
| PersonalityController | `hub/controller/bot/PersonalityController.java` | 人设配置 |
| SpeakerTrainController | `hub/controller/bot/SpeakerTrainController.java` | 语音训练 |
| TalkAgentController | `hub/controller/bot/TalkAgentController.java` | 对话助手 |
| VoiceApiController | `hub/controller/bot/VoiceApiController.java` | 语音 API |

### 关键业务逻辑

- **创建助手**: 支持普通创建和 AI 一句话创建，创建时可关联数据集、选择模型、配置人设
- **复制助手**: 深拷贝助手配置到目标空间，需要 @SpacePreAuth 权限
- **AI 辅助**: 调用大模型生成头像、开场白、人设描述，支持润色优化
- **语音训练**: 上传音频样本训练自定义发音人，支持试听

## 5. 前端页面

| 页面 | 路由 | 组件路径 | 说明 |
|------|------|---------|------|
| 助手列表 | `/home` | `src/pages/home/` | 首页展示助手列表 |
| 创建助手 | - | `src/pages/home/components/CreateBot/` | 创建助手弹窗 |
| 助手详情 | `/management/bot-api` | `src/pages/management/` | 助手管理详情 |
| 发布管理 | `/management/release` | `src/pages/management/` | 助手发布管理 |

## 6. 前端状态管理

| Store | 路径 | 管理的状态 |
|-------|------|-----------|
| global-store | `src/store/global-store.ts` | 全局助手列表、当前助手信息 |

## 7. 前端 API Service

| 函数 | 路径 | 对应后端 API |
|------|------|-------------|
| createBot | `src/services/bot.ts` | POST /bot/create |
| updateBot | `src/services/bot.ts` | POST /bot/update |
| deleteBot | `src/services/bot.ts` | POST /bot/delete |
| getBotList | `src/services/bot.ts` | GET /bot/list |
| getBotDetail | `src/services/bot.ts` | GET /bot/detail |

## 8. 模块间依赖

- **依赖**: 模型管理（选择模型）、知识库（关联数据集）、空间管理（空间上下文）
- **被依赖**: 聊天（基于 Bot 发起对话）、发布（发布 Bot）、工作流（工作流 Bot）
