---
module: AI 工具
generated: 2026-02-28
---

# AI 工具模块文档

## 1. 模块概述

AI 工具模块提供工具（Function Calling / Tool Use）的全生命周期管理，包括工具创建、调试、发布到工具广场、收藏、版本管理和 MCP 协议支持。工具可被助手和工作流节点调用，扩展 AI 的能力边界。

## 2. 后端 API 清单

### ToolBoxController (`/tool`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/tool/create` | - | 创建工具 |
| POST | `/tool/temporary` | - | 创建临时工具（调试用） |
| POST | `/tool/update` | - | 更新工具 |
| GET | `/tool/list` | - | 获取工具列表（分页） |
| GET | `/tool/detail` | - | 获取工具详情 |
| GET | `/tool/default-icon` | - | 获取工具默认图标 |
| POST | `/tool/delete` | - | 删除工具 |
| POST | `/tool/debug-v2` | - | 调试工具 V2 |
| POST | `/tool/list-square` | - | 获取工具广场列表 |
| POST | `/tool/favorite` | - | 收藏/取消收藏工具 |
| GET | `/tool/version` | - | 获取工具版本列表 |
| GET | `/tool/latest-version` | - | 获取工具最新版本 |
| POST | `/tool/operate-history` | - | 添加工具操作历史 |
| POST | `/tool/feedback` | - | 工具反馈 |
| POST | `/tool/publish-square` | - | 发布到工具广场 |
| GET | `/tool/export` | - | 导出工具 |
| POST | `/tool/import` | - | 导入工具 |

### McpController (`/mcp`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/mcp/parse` | - | 解析 MCP 服务端点 |
| POST | `/mcp/tools` | - | 获取 MCP 工具列表 |
| POST | `/mcp/save` | - | 保存 MCP 配置 |

## 3. 数据模型

### 关键 DTO

**ToolBoxDto 核心字段**:
```java
private Long id;               // 工具ID
private String name;           // 工具名称
private String description;    // 工具描述
private String avatarIcon;     // 头像图标
private String avatarColor;    // 头像颜色
private String toolId;         // 工具唯一ID
private String endPoint;       // API 端点
private String method;         // HTTP 方法 (GET/POST/DELETE/PATCH)
private String webSchema;      // Web Schema（OpenAPI 格式）
private Integer status;        // 状态
private String version;        // 版本号
```

**工具状态**:
```
ToolStatus:
  - DRAFT: 草稿
  - PUBLISHED: 已发布到广场
```

## 4. 后端核心类

| 类名 | 路径 | 职责 |
|------|------|------|
| ToolBoxController | `hub/controller/tool/ToolBoxController.java` | 工具 CRUD + 调试 + 广场 |
| McpController | `hub/controller/tool/McpController.java` | MCP 协议管理 |

### 关键业务逻辑

- **工具创建**: 定义工具的 API 端点、HTTP 方法、请求/响应 Schema（OpenAPI 格式）
- **工具调试**: 临时创建工具并发送测试请求，验证工具可用性
- **工具广场**: 用户可将工具发布到广场供其他用户使用，支持收藏和反馈
- **MCP 支持**: 解析 MCP 服务端点，自动发现可用工具
- **导入导出**: 支持工具定义的 JSON 导入导出
- **版本管理**: 工具支持多版本，更新时创建新版本

## 5. 前端页面

| 页面 | 路由 | 组件路径 | 说明 |
|------|------|---------|------|
| 工具广场 | `/store/plugin` | `src/pages/store/` | 工具广场浏览和搜索 |
| 工具管理 | - | 集成在助手配置中 | 工具选择和配置 |

## 6. 前端状态管理

| Store | 路径 | 管理的状态 |
|-------|------|-----------|
| - | 页面本地 state | 工具列表、调试结果、广场数据 |

## 7. 前端 API Service

| 函数 | 路径 | 对应后端 API |
|------|------|-------------|
| createTool | `src/services/tool.ts` | POST /tool/create |
| updateTool | `src/services/tool.ts` | POST /tool/update |
| getToolList | `src/services/tool.ts` | GET /tool/list |
| getToolDetail | `src/services/tool.ts` | GET /tool/detail |
| debugTool | `src/services/tool.ts` | POST /tool/debug-v2 |
| getToolSquare | `src/services/tool.ts` | POST /tool/list-square |
| favoriteTool | `src/services/tool.ts` | POST /tool/favorite |
| parseMcp | `src/services/tool.ts` | POST /mcp/parse |
| getMcpTools | `src/services/tool.ts` | POST /mcp/tools |

## 8. 模块间依赖

- **依赖**: 空间管理（空间级工具）
- **被依赖**: Bot 管理（助手关联工具）、工作流（工具节点）、聊天（Function Calling）
