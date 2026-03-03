---
project: Astron Agent Console
generated: 2026-02-28
---

# Console 项目全局概览

## 项目简介

Astron Agent Console 是 Astron Agent 平台的控制台子系统，提供 AI Agent 管理、工作流编排、模型管理、知识库、AI 工具集成等功能。前后端分离架构，后端 Java 21 + Spring Boot 3.5.4，前端 React 18 + TypeScript + Vite。

## 模块索引

| 模块 | 文档路径 | 说明 |
|------|---------|------|
| [Bot 管理](bot-management/module.md) | `docsForAi/bot-management/` | 助手创建、配置、收藏、人设、语音 |
| [聊天](chat/module.md) | `docsForAi/chat/` | SSE 流式聊天、历史、文件、重启 |
| [工作流](workflow/module.md) | `docsForAi/workflow/` | 工作流模板、编排、流式执行 |
| [空间管理](space-management/module.md) | `docsForAi/space-management/` | 个人/企业空间、成员、权限 |
| [企业管理](enterprise-management/module.md) | `docsForAi/enterprise-management/` | 团队创建、成员管理、邀请 |
| [用户管理](user-management/module.md) | `docsForAi/user-management/` | 用户信息、我的助手 |
| [发布管理](publish/module.md) | `docsForAi/publish/` | 多渠道发布、API 管理、版本 |
| [模型管理](model-management/module.md) | `docsForAi/model-management/` | 模型配置、参数、供应商 |
| [知识库](knowledge/module.md) | `docsForAi/knowledge/` | 数据集、文档、向量检索 |
| [AI 工具](ai-tools/module.md) | `docsForAi/ai-tools/` | 工具创建、调试、MCP、工具广场 |

## 技术架构

```
前端 (React + TypeScript + Vite)
    ↓ Axios (Bearer Token + space-id/enterprise-id headers)
后端 Controller (Spring Boot + Spring Security OAuth2)
    ↓
业务 Service (hub 模块)
    ↓
数据 Service (commons 模块)
    ↓
Mapper (MyBatis Plus)
    ↓
MySQL + Redis (Redisson) + Kafka + MinIO
```

## 认证与多租户

- 认证: Casdoor SSO → OAuth2 JWT Bearer Token
- 多租户: 请求头 `space-id` + `enterprise-id`，后端拦截器自动注入上下文
- 权限: `@SpacePreAuth` / `@EnterprisePreAuth` 注解控制

## 关键代码路径

| 层级 | 后端路径 | 前端路径 |
|------|---------|---------|
| 入口 | `console/backend/hub/src/main/java/.../controller/` | `console/frontend/src/pages/` |
| 业务逻辑 | `console/backend/hub/src/main/java/.../service/` | `console/frontend/src/store/` |
| 数据访问 | `console/backend/hub/src/main/java/.../mapper/` | `console/frontend/src/services/` |
| 实体/类型 | `console/backend/hub/src/main/java/.../entity/` | `console/frontend/src/types/` |
| 公共模块 | `console/backend/commons/src/main/java/` | `console/frontend/src/components/` |
| 配置 | `console/backend/hub/src/main/resources/` | `console/frontend/src/config/` |
| 国际化 | - | `console/frontend/src/locales/` |

## 开发工作流程

**完整工作流程文档**: [WORKFLOW.md](../.claude/WORKFLOW.md)

### Claude Code Skills

Console 项目提供了 9 个 Claude Code Skills，用于文档驱动开发：

| Skill | 命令 | 说明 | 输出文件 |
|-------|------|------|----------|
| 上下文校验 | `/context-check` | 校验模块文档与代码一致性 | `context-check-report.md` |
| 需求文档 | `/requirement` | 将用户需求转化为结构化需求文档 | `requirement.md` |
| 用户故事 | `/stories` | 从需求提取用户故事和验收标准（按需） | `stories.md` |
| 技术规格 | `/spec` | 设计 API、数据模型、前端规格 | `spec.md` |
| 任务规划 | `/tasks` | 拆解为可执行任务，带依赖关系 | `tasks.md` |
| 后端设计 | `/backend-design` | 生成后端类设计和代码骨架（按需） | `backend-design.md` |
| 前端设计 | `/frontend-design` | 生成前端组件树和状态管理方案（按需） | `frontend-design.md` |
| 模块文档 | `/doc-module` | 从代码逆向生成模块文档 | `module.md` |
| Bug 修复 | `/bugfix` | 记录 Bug 根因分析和修复方案 | `bugfix.md` |

### 开发流程

根据任务类型选择合适的流程：

**大功能（完整链路）**：
```
/context-check → /requirement → /stories → /spec → /tasks → /backend-design + /frontend-design → 实现 → /doc-module
```

**小功能（快速链路）**：
```
/context-check → /requirement → /spec → /tasks → 实现 → /doc-module
```

**Bug 修复**：
- 简单 Bug：直接修复 → 验证 → /doc-module（如需）
- 复杂 Bug：走完整链路或快速链路

详见：[WORKFLOW.md](../.claude/WORKFLOW.md)

**新功能开发**:
```
/requirement → /stories → /spec → /tasks → /backend-design + /frontend-design → 实现 → /doc-module
```

**Bug 修复**:
- 简单 Bug: 直接修复 → 更新文档（如需要）
- 复杂 Bug: `/bugfix` → 实现 → 更新文档

详见: [开发工作流程文档](../.claude/WORKFLOW.md)
