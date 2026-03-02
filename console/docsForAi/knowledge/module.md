---
module: 知识库
generated: 2026-02-28
---

# 知识库模块文档

## 1. 模块概述

知识库模块提供 RAG（检索增强生成）能力的数据管理，包括数据集创建、文档上传/解析、向量化存储和检索。助手和工作流可关联知识库实现基于私有数据的问答。核心处理逻辑在 core/knowledge Python 服务中，Console 负责管理界面和 API 代理。

## 2. 后端 API 清单

### DatasetController (`/dataset`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/dataset/create` | @SpacePreAuth | 创建数据集 |
| POST | `/dataset/update` | @SpacePreAuth | 更新数据集 |
| POST | `/dataset/delete` | @SpacePreAuth | 删除数据集 |
| POST | `/dataset/list` | @SpacePreAuth | 获取数据集列表 |
| GET | `/dataset/detail/{datasetId}` | - | 获取数据集详情 |

### DocumentController (`/document`)

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/document/upload` | @SpacePreAuth | 上传文档到数据集 |
| POST | `/document/delete` | @SpacePreAuth | 删除文档 |
| POST | `/document/list` | @SpacePreAuth | 获取文档列表 |
| GET | `/document/status/{documentId}` | - | 获取文档处理状态 |
| POST | `/document/reparse` | @SpacePreAuth | 重新解析文档 |

## 3. 数据模型

### 关键概念

**数据集类型**:
```
DatasetType:
  - TEXT: 文本数据集
  - QA: 问答对数据集
```

**文档处理状态**:
```
DocumentStatus:
  - UPLOADING: 上传中
  - PARSING: 解析中
  - INDEXING: 索引中
  - COMPLETED: 已完成
  - FAILED: 失败
```

**支持的文档格式**: PDF, DOCX, TXT, MD, CSV, XLSX

## 4. 后端核心类

| 类名 | 路径 | 职责 |
|------|------|------|
| DatasetController | `hub/controller/knowledge/DatasetController.java` | 数据集管理 |
| DocumentController | `hub/controller/knowledge/DocumentController.java` | 文档管理 |

### 关键业务逻辑

- **文档上传**: 文件上传到 MinIO，触发异步解析任务
- **文档解析**: 通过 Kafka 消息通知 core/knowledge 服务进行文档解析和向量化
- **检索**: 聊天时通过 core/knowledge 服务进行向量检索，返回相关文档片段
- **数据集关联**: Bot 创建时可关联多个数据集，聊天时自动检索

## 5. 前端页面

| 页面 | 路由 | 组件路径 | 说明 |
|------|------|---------|------|
| 知识库列表 | - | `src/pages/` (集成在助手配置中) | 数据集列表和管理 |
| 文档管理 | - | `src/pages/` (集成在数据集详情中) | 文档上传和状态查看 |

## 6. 前端状态管理

| Store | 路径 | 管理的状态 |
|-------|------|-----------|
| - | 页面本地 state | 数据集列表、文档列表、上传状态 |

## 7. 前端 API Service

| 函数 | 路径 | 对应后端 API |
|------|------|-------------|
| createDataset | `src/services/knowledge.ts` | POST /dataset/create |
| getDatasetList | `src/services/knowledge.ts` | POST /dataset/list |
| uploadDocument | `src/services/knowledge.ts` | POST /document/upload |
| getDocumentList | `src/services/knowledge.ts` | POST /document/list |
| getDocumentStatus | `src/services/knowledge.ts` | GET /document/status/{id} |

## 8. 模块间依赖

- **依赖**: 空间管理（空间级数据集）、core/knowledge 服务（文档解析和向量检索）
- **被依赖**: Bot 管理（关联数据集）、聊天（RAG 检索）、工作流（知识库节点）
