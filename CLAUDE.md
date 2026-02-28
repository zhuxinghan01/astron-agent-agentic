# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目简介

Astron Agent 是一个**企业级 AI Agent 开发平台**，集成了 AI 工作流编排、模型管理、AI 和 MCP 工具集成、RPA 自动化和团队协作功能。平台支持高可用部署，帮助组织快速构建可扩展的、生产就绪的智能 Agent 应用。

## 架构概览

Astron Agent 采用**微服务架构**，前后端分离，支持多语言开发：

```
astron-agent/
├── console/                    # 控制台子系统
│   ├── backend/               # Java Spring Boot 服务
│   │   ├── hub/              # 主业务逻辑模块
│   │   ├── commons/          # 公共工具模块
│   │   └── toolkit/          # 工具包模块
│   └── frontend/             # React TypeScript SPA
├── core/                      # 核心平台服务
│   ├── agent/                # Agent 执行引擎 (Python)
│   ├── knowledge/            # 知识库服务 (Python)
│   ├── workflow/             # 工作流编排 (Python)
│   └── tenant/               # 多租户服务 (Go)
├── docs/                      # 文档
└── makefiles/                 # 构建系统组件
```

## 技术栈

### 前端 (Console Frontend)
- **框架**: React 18 + TypeScript + Vite
- **UI 库**: Ant Design 5.19.1
- **状态管理**: Zustand + Recoil
- **路由**: React Router v6
- **国际化**: i18next
- **认证**: Casdoor JS SDK (OAuth2/OIDC)

详见: [console/frontend/CLAUDE.md](console/frontend/CLAUDE.md)

### 后端控制台 (Console Backend)
- **语言**: Java 21
- **框架**: Spring Boot 3.5.4
- **ORM**: MyBatis Plus 3.5.7
- **安全**: Spring Security + OAuth2
- **缓存**: Redisson (Redis)
- **数据库迁移**: Flyway
- **工具**: Lombok + MapStruct

详见: [console/backend/CLAUDE.md](console/backend/CLAUDE.md)

### 核心服务
- **Agent/Workflow/Knowledge**: Python + FastAPI
- **Tenant Service**: Go + Gin
- **消息队列**: Kafka
- **对象存储**: MinIO
- **数据库**: MySQL
- **缓存**: Redis

## 开发环境要求

在开始开发前，请确保安装以下工具：

- **Java 21+** (后端服务)
- **Maven 3.8+** (Java 项目管理)
- **Node.js 18+** (前端开发)
- **Python 3.9+** (核心服务)
- **Go 1.21+** (租户服务)
- **Docker & Docker Compose** (容器化服务)
- **Git** (版本控制)

## 统一开发命令 (Makefile)

项目提供了统一的 Makefile 工具链，支持智能检测和多语言项目管理：

### 核心命令

```bash
make setup      # 🛠️  一键环境配置 (工具+hooks+分支策略)
make check      # 🔍  代码质量检查 (智能检测活跃项目)
make test       # 🧪  运行测试 (智能检测活跃项目)
make build      # 📦  构建项目 (智能检测活跃项目)
make push       # 📤  安全推送到远程 (带预检查)
make clean      # 🧹  清理构建产物
```

### 专业命令

```bash
make status     # 📊  显示详细项目状态
make info       # ℹ️   显示工具和依赖信息
make lint       # 🔧  运行代码检查 (check 的别名)
make ci         # 🤖  完整 CI 流程 (check+test+build)
make hooks      # ⚙️  Git hooks 管理菜单
```

### 语言特定命令

```bash
# Java
make fmt-java       # 格式化 Java 代码
make check-java     # Java 质量检查
make test-java      # 运行 Java 测试
make build-java     # 构建 Java 项目

# TypeScript
make fmt-ts         # 格式化 TypeScript 代码
make check-ts       # TypeScript 质量检查
make test-ts        # 运行 TypeScript 测试
make build-ts       # 构建 TypeScript 项目

# Python
make fmt-python     # 格式化 Python 代码
make check-python   # Python 质量检查
make test-python    # 运行 Python 测试

# Go
make fmt-go         # 格式化 Go 代码
make check-go       # Go 质量检查
make test-go        # 运行 Go 测试
make build-go       # 构建 Go 项目
```

## 分支管理规范

### 分支命名约定

| 分支类型 | 格式 | 示例 | 用途 |
|---------|------|------|------|
| Feature | `feature/feature-name` | `feature/user-auth` | 新功能开发 |
| Bugfix | `bugfix/issue-name` | `bugfix/login-error` | Bug 修复 |
| Hotfix | `hotfix/patch-name` | `hotfix/security-patch` | 紧急修复 |
| Documentation | `doc/doc-name` | `doc/api-guide` | 文档更新 |

### 创建分支

```bash
# 使用 Makefile 命令创建规范分支
make new-feature name=user-authentication
make new-bugfix name=login-timeout
make new-hotfix name=security-vulnerability
```

## 提交规范

### 提交消息格式

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### 提交类型

- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式调整 (不影响功能)
- `refactor`: 代码重构
- `perf`: 性能优化
- `test`: 测试相关
- `build`: 构建系统或依赖更新
- `ci`: CI 配置更新
- `chore`: 其他杂项

### 提交示例

```bash
feat(agent): add user authentication support
fix(workflow): resolve workflow execution timeout
docs(readme): update deployment guide
refactor(link): optimize kafka send
```

## 代码质量标准

项目对所有语言实施严格的代码质量控制：

### Go
- **格式化**: gofmt + goimports + gofumpt
- **检查工具**: golangci-lint + staticcheck
- **标准**: Go 标准格式，圈复杂度 ≤10

### Java
- **格式化**: Spotless (Google Java Format)
- **检查工具**: Checkstyle + PMD + SpotBugs
- **标准**: Google Java Style Guide，圈复杂度 ≤10，行长度 ≤120

### Python
- **格式化**: black + isort
- **检查工具**: flake8 + mypy + pylint
- **标准**: PEP 8，圈复杂度 ≤10

### TypeScript
- **格式化**: prettier
- **检查工具**: eslint + tsc
- **标准**: ESLint 规则，严格类型检查

### 质量要求

所有代码必须通过以下检查：

- ✅ 自动代码格式化
- ✅ 无 Linting 错误或警告
- ✅ 严格类型检查 (TypeScript/Python)
- ✅ 圈复杂度 ≤10
- ✅ 无安全漏洞 (静态分析)

## Pre-commit Hooks (推荐)

项目使用 [pre-commit](https://pre-commit.com/) 进行自动化代码质量检查：

```bash
# 安装 pre-commit
pip install pre-commit

# 安装 hooks
pre-commit install
pre-commit install --hook-type commit-msg
```

Pre-commit 会在每次提交时自动运行：
- 代码格式检查 (Black, Prettier, gofmt, Spotless)
- Linter 检查 (flake8, ESLint, golangci-lint, Checkstyle)
- 类型检查 (mypy, TypeScript)
- 密钥扫描 (gitleaks)
- 提交消息格式验证

## 快速开始

### 1. 克隆仓库

```bash
git clone https://github.com/iflytek/astron-agent.git
cd astron-agent
```

### 2. 一键环境配置

```bash
make setup
```

这将自动：
- 安装语言特定的开发工具
- 配置 Git hooks
- 设置分支命名规范
- 安装项目依赖

### 3. 开发工作流

```bash
# 创建功能分支
make new-feature name=my-feature

# 开发过程中检查代码质量
make check

# 运行测试
make test

# 构建项目
make build

# 安全推送 (自动进行质量检查)
make push
```

## Docker 部署

### 使用 Docker Compose (推荐快速开始)

```bash
# 进入 Docker 目录
cd docker/astronAgent

# 复制环境配置
cp .env.example .env

# 编辑环境变量
vim .env

# 启动所有服务 (包括 Casdoor 认证)
docker compose -f docker-compose-with-auth.yaml up -d
```

### 服务访问地址

- **Casdoor 管理界面**: http://localhost:8000 (默认账号: admin/123)
- **应用前端**: http://localhost/

详见: [docs/DEPLOYMENT_GUIDE_WITH_AUTH.md](docs/DEPLOYMENT_GUIDE_WITH_AUTH.md)

## 子项目文档

- [Console Frontend](console/frontend/CLAUDE.md) - React TypeScript 前端
- [Console Backend](console/backend/CLAUDE.md) - Java Spring Boot 后端

## 相关文档

- [🚀 部署指南](docs/DEPLOYMENT_GUIDE.md)
- [🔧 配置说明](docs/CONFIGURATION.md)
- [📘 开发指南](https://www.xfyun.cn/doc/spark/Agent03-%E5%BC%80%E5%8F%91%E6%8C%87%E5%8D%97.html)
- [💡 最佳实践](https://www.xfyun.cn/doc/spark/AgentNew-%E6%8A%80%E6%9C%AF%E5%AE%9E%E8%B7%B5%E6%A1%88%E4%BE%8B.html)
- [❓ FAQ](https://www.xfyun.cn/doc/spark/Agent06-FAQ.html)

## 贡献指南

欢迎贡献！请查看 [CONTRIBUTING.md](CONTRIBUTING.md) 了解详细的贡献流程。

## 许可证

本项目采用 [Apache 2.0 License](LICENSE)，允许自由使用、修改、分发和商业使用。
