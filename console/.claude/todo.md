主要问题

最大的问题是扫描范围和真实代码结构不一致。你的 skill 基本都默认只看 console/backend/hub/...，见 requirement.md (line 9)、spec.md (line 12)、doc-module.md (line 14)。但 AI 工具和不少工作流接口实际在 console/backend/toolkit/...，比如 ToolBoxController.java (line 20) 和 WorkflowController.java (line 346)。这会直接导致 AI 读文档时漏模块、漏接口、写错路径。
docsForAi 已经出现了明确漂移，而且不是小漂移。AI 工具 文档里写的是 /tool/create、/tool/update、/tool/list，并且把控制器写在 hub/controller/tool/...，见 ai-tools/module.md (line 14)。但真实接口是 /tool/create-tool、/tool/update-tool、/tool/list-tools，并且控制器在 toolkit，见 ToolBoxController.java (line 21)。
AI 工具 文档里的前端服务映射也不准。文档把 createTool、updateTool 等都写在 src/services/tool.ts，见 ai-tools/module.md (line 99)。但真实代码里这些能力主要在 plugin.ts (line 6)，而 tool.ts (line 22) 主要是广场列表、收藏、详情。
工作流 文档明显偏“摘要化”，不足以支撑 agent 直接改代码。文档只列了少量聊天和模板接口，见 workflow/module.md (line 12)，但真实前端实际在用大量 /workflow/* 能力，包括创建、保存、构建、发布、导入导出、MCP 调试等，见 flow.ts (line 3)。
工作流 文档里的状态管理路径也不准确。它写的是 src/store/，见 workflow/module.md (line 85)，但实际工作流页直接依赖的是 index.tsx (line 16) 里 @/components/workflow/store/use-flows-manager 和 use-flow-store。
这些文档时间并不旧，module.md 是 2026-02-28 生成的，overview.md (line 1) 也是同批；而快速参考在 2026-03-02 还更新过，见 QUICK_REFERENCE.md (line 283)。两三天内就有这种漂移，说明问题不在“文档太久没更新”，而在“生成规则和真实扫描范围不够对”。
可优化的地方

第一优先级是把 skill 的扫描范围从 hub 扩到 hub + toolkit + commons。否则 AI 工具、工作流 这类跨模块能力，AI 天然就会读偏。
给 module.md 增加自动化漂移校验。最实用的是做一个脚本，从 Controller 注解和前端 service 函数里抽接口清单，再和文档比对，把“路径不一致/方法不一致/文件不存在”直接报出来。
把 docsForAi 分成两类用途。module.md 负责“稳定的系统地图”，{feature-name}/ 负责“这次改动的执行轨迹”。现在你主要做成了前者，后者还没真正进入日常开发闭环。
让 tasks.md 更适合 agent 执行。建议至少增加 status、owner、verification command、done when，最好再有一个机器可读的 JSON/YAML 版本，这样 agent 才能真正按任务逐步推进，而不是只看 Markdown。
给模块文档加“改这个模块先看哪里”。目前文档更像库存清单，适合认识模块；但 agent 更需要“常改文件、扩展点、坑点、禁改点、参考实现”。这类 change-oriented 信息比 API 列表更值钱。
统一命名。现在前端有 tool.ts 和 plugin.ts，后端有 tool、前端页面又叫 plugin-store。对人还能适应，对 agent 检索非常不友好。
如果你想把它升级成更强的 agentic coding，最好把 .claude/skills/*.md 从“提示词模板”再推进一步，做成“提示词 + 校验脚本 + 输出结构”的组合。现在它们定义得很好，但约束主要靠自觉执行。
一句话总结：你已经把 console 的 AI coding 做成了“有流程、有模板、有上下文库”的体系化基础设施；下一步最该补的是“文档和真实代码的一致性”以及“从文档规划走到自动闭环执行”的能力。这样它才会从“AI 辅助开发流程”真正进化成“agentic coding”。


🔧 可优化的地方（按优先级）
高优先级 (P0):

文档更新自动化 - 添加 Git hooks 检测文档是否需要更新
跨模块依赖可视化 - 在 overview.md 中添加 Mermaid 依赖图
Skills 可发现性 - 在 console/README.md 中添加快速入口
中优先级 (P1):
4. 测试设计 skill - 创建 /test-design skill
5. 代码示例补充 - 在设计文档中增加代码骨架
6. 前端 CLAUDE.md 完善 - 补充架构细节

低优先级 (P2):
7. 性能基准文档
8. 复杂模块拆分
9. 版本管理增强
10. AI 提示词迭代优化

💡 关键发现
你们的 AI coding 流程非常合理且完善，主要优势：

✅ 文档先行，避免盲目编码
✅ 强制参考现有代码，保持一致性
✅ 文档简洁原则，避免过度文档化
✅ 代码与文档同步机制


整体上，这条流程是合理的，适合中到大型需求，尤其是跨前后端、跨模块的改动。它的优点是先收敛需求，再收敛设计，最后再实现，能明显降低 AI 直接跳代码带来的返工风险。你现在在 WORKFLOW.md 里定义的链路本身没有根本性问题。

但它现在更像“完整重流程”，不是所有任务都适合走满。真正需要优化的，不是顺序本身，而是“分层适用”和“执行闭环”。

结论

对中大型功能：这条链路合理，建议保留。
对小功能和局部改动：偏重，容易文档成本高于开发收益。
当前最大风险：不是流程太长，而是上游文档一旦不准，后面 /spec、/tasks、设计、实现都会被带偏。这个问题你在 todo.md 里已经点到了。
需要优化的点

stories 不必强制。对简单 CRUD、明确需求、单角色场景，/requirement → /spec 就够了。stories 更适合复杂交互、多角色、多验收场景。
backend-design + frontend-design 不必双开。只改后端就只出后端设计，只改前端就只出前端设计；否则会产生空设计文档。
实现阶段需要更强的执行约束。现在是“按 tasks 实现”，但 tasks 还不够 agent-ready。建议每个任务增加 status、verification command、done when。
在 /requirement 前加一个“上下文校验”步骤。先确认相关 module.md 是否可信；如果模块文档已经漂移，应先修文档或直接读代码，否则整个链路会建立在错误上下文上。
/doc-module 后面最好再加一层“文档回写校验”。否则流程写着“代码后更新文档”，但没有机制保证文档和真实代码一致。
我建议的优化版

大功能：context-check → /requirement → /stories(按需) → /spec → /tasks → /backend-design|/frontend-design(按需) → 实现(逐任务验证) → /doc-module → drift-check
小功能：context-check → /requirement(简版) → /spec → /tasks(轻量) → 实现 → /doc-module
Bug：issue分析 → 定位代码 → 修复 → 验证 → /doc-module，只有涉及重构或多模块时再走完整链路
最关键的一条

你现在最该优先优化的，不是再加新步骤，而是把流程输入做准：把扫描范围从 hub 扩到 hub + toolkit + commons，并给 module.md 加自动漂移检查。否则流程再完整，也只是把错误上下文系统化放大。




优化 Agentic Coding 流程
Context
当前 .claude 和 docsForAi 已经建立了完善的 AI coding 基础设施，包括 8 个结构化 skills 和 11 个模块文档。但存在以下问题：

流程适用性问题：现有流程是"完整重流程"，适合中大型功能，但对小功能和 Bug 修复偏重
上下文可信度问题：skills 扫描范围不完整（只扫描 hub/，遗漏 toolkit/ 和 commons/），导致文档漂移
执行闭环缺失：tasks.md 缺少 status、verification command、done when 等执行约束
文档同步机制缺失：没有自动化校验文档与代码一致性的机制
优化目标：

分层适用：区分大功能、小功能、Bug 修复的不同流程
上下文校验：在需求分析前先验证模块文档可信度
按需生成：stories、设计文档不强制，根据实际需要生成
执行闭环：任务增加状态跟踪和验证机制
文档回写校验：确保文档与代码同步
优化方案
1. 扩展 Skills 扫描范围（P0）
问题：当前 skills 只扫描 console/backend/hub/，遗漏了 toolkit/ 和 commons/

修改文件：

console/.claude/skills/requirement.md (line 10)
console/.claude/skills/spec.md (line 13)
console/.claude/skills/backend-design.md (扫描范围)
console/.claude/skills/doc-module.md (扫描范围)
修改内容：


# 修改前
- 后端: 查找相关的 Controller、Service、Entity（`console/backend/hub/src/main/java/com/iflytek/astron/console/hub/`）

# 修改后
- 后端: 查找相关的 Controller、Service、Entity
  - Hub 模块: `console/backend/hub/src/main/java/com/iflytek/astron/console/hub/`
  - Toolkit 模块: `console/backend/toolkit/src/main/java/com/iflytek/astron/console/toolkit/`
  - Commons 模块: `console/backend/commons/src/main/java/com/iflytek/astron/console/commons/`
2. 新增上下文校验 Skill（P0）
新建文件：console/.claude/skills/context-check.md

功能：在 /requirement 前执行，校验相关模块文档的可信度

执行步骤：

根据用户需求，识别涉及的模块（bot/workflow/ai-tools 等）
读取对应的 docsForAi/{module}/module.md
从实际代码中抽取关键信息（Controller 路径、Entity 字段、前端 service 函数）
对比文档与代码，标记不一致项
输出校验报告，建议是否需要先修复文档
输出：console/docsForAi/{module}/context-check-report.md（临时文件，校验后可删除）

3. 优化 WORKFLOW.md 分层流程（P0）
修改文件：console/.claude/WORKFLOW.md

新增章节：在现有"一、新功能开发流程"前插入"流程选择指南"

内容：


## 流程选择指南

### 大功能（完整链路）
**适用场景**：
- 新增数据表
- 新增前端页面
- 跨模块改动
- 多角色交互场景

**流程**：
context-check → /requirement → /stories → /spec → /tasks → /backend-design|/frontend-design → 实现 → /doc-module → drift-check

### 小功能（快速链路）
**适用场景**：
- 单模块改动
- 明确需求
- 简单 CRUD
- 单角色场景

**流程**：
context-check → /requirement（简版） → /spec → /tasks（轻量） → 实现 → /doc-module

### Bug 修复
**简单 Bug**：
- 单文件、单方法修复
- 流程：issue 分析 → 定位代码 → 修复 → 验证 → /doc-module（如涉及业务逻辑变更）

**复杂 Bug**：
- 需要重构或多模块改动
- 流程：走完整链路或快速链路
4. 增强 tasks.md 执行约束（P1）
修改文件：console/.claude/skills/tasks.md

修改输出模板（line 24-43）：


## 任务概览

| 编号 | 阶段 | 任务 | 复杂度 | 依赖 | 状态 | 负责人 |
|------|------|------|--------|------|------|--------|
| T1 | 数据层 | {描述} | S | - | pending | @claude |
| T2 | 后端 | {描述} | M | T1 | pending | @claude |
| T3 | 前端 | {描述} | L | T2 | pending | @claude |
| T4 | 测试 | {描述} | M | T2,T3 | pending | @claude |

**状态说明**：
- pending: 待开始
- in_progress: 进行中
- completed: 已完成
- blocked: 被阻塞
新增任务详情字段（line 48-60）：


### T1: {任务标题} [S]

**依赖**: 无
**状态**: pending
**验证命令**:
```bash
# 数据库迁移验证
make check-java
mvn flyway:validate
完成标准:

 Flyway 迁移脚本通过验证
 Entity 类编译通过
 Mapper 接口测试通过
文件:

新建: console/backend/hub/src/main/resources/db/migration/V{version}__{description}.sql
新建: console/backend/hub/src/main/java/com/iflytek/astron/console/hub/entity/{Entity}.java
新建: console/backend/hub/src/main/java/com/iflytek/astron/console/hub/mapper/{Mapper}.java


### 5. 新增文档回写校验 Skill（P1）

**新建文件**：`console/.claude/skills/drift-check.md`

**功能**：在 `/doc-module` 后执行，校验文档与代码是否同步

**执行步骤**：
1. 读取更新后的 `module.md`
2. 从实际代码中抽取 API 端点、Entity 字段、前端路由
3. 对比文档与代码，标记不一致项
4. 如果发现不一致，自动修正文档或提示人工确认

**输出**：更新 `module.md` 或生成 `drift-report.md`

### 6. 优化 Skills 按需生成逻辑（P1）

**修改文件**：`console/.claude/WORKFLOW.md`

**修改"阶段 2: 用户故事"章节**（line 40-60）：
```markdown
### 阶段 2: 用户故事 → `/stories`（按需）

**前置条件**: `requirement.md` 已完成

**何时需要**：
- ✅ 多角色交互场景（管理员、普通用户、访客）
- ✅ 复杂验收标准（多个 Given-When-Then）
- ✅ 需要优先级排序的多个子功能

**何时跳过**：
- ❌ 单角色场景
- ❌ 简单 CRUD
- ❌ 明确的技术需求（如性能优化、Bug 修复）

**执行**:
```bash
/stories


**修改"阶段 5: 技术设计"章节**（line 111-133）：
```markdown
### 阶段 5: 技术设计 → `/backend-design` + `/frontend-design`（按需）

**前置条件**: `spec.md` + `tasks.md` 已完成

**何时需要**：
- ✅ 后端：新增 Service 层、复杂业务逻辑、数据库迁移
- ✅ 前端：新增页面、复杂状态管理、多组件交互

**何时跳过**：
- ❌ 只改后端 → 只执行 `/backend-design`
- ❌ 只改前端 → 只执行 `/frontend-design`
- ❌ 简单修改（单文件、单方法）→ 两者都跳过

**执行**:
```bash
/backend-design  # 仅当涉及后端改动
/frontend-design # 仅当涉及前端改动


### 7. 更新 QUICK_REFERENCE.md（P2）

**修改文件**：`console/.claude/QUICK_REFERENCE.md`

**新增章节**：在文件开头插入"快速决策树"

**内容**：
```markdown
## 快速决策树

用户需求
├─ 新功能？
│   ├─ 大功能（新表/新页面/跨模块）
│   │   └─ /context-check → /requirement → /stories → /spec → /tasks → /backend-design + /frontend-design → 实现 → /doc-module → /drift-check
│   └─ 小功能（单模块/简单 CRUD）
│       └─ /context-check → /requirement → /spec → /tasks → 实现 → /doc-module
└─ Bug 修复？
├─ 简单 Bug（单文件）
│   └─ 分析 → 定位 → 修复 → 验证 → /doc-module（如需）
└─ 复杂 Bug（重构/多模块）
└─ 走完整链路或快速链路


关键文件清单
需要修改的文件
Skills 定义（扩展扫描范围）：

console/.claude/skills/requirement.md (line 10)
console/.claude/skills/spec.md (line 13)
console/.claude/skills/backend-design.md
console/.claude/skills/doc-module.md
工作流程（分层流程）：

console/.claude/WORKFLOW.md (插入"流程选择指南"，修改 stories 和 design 章节)
任务模板（执行约束）：

console/.claude/skills/tasks.md (line 24-60)
快速参考（决策树）：

console/.claude/QUICK_REFERENCE.md (文件开头)
需要新建的文件
上下文校验 Skill：

console/.claude/skills/context-check.md
文档回写校验 Skill：

console/.claude/skills/drift-check.md
验证方式
1. 扫描范围验证

# 测试 /requirement 是否能识别 toolkit 模块
cd console
# 创建测试需求："优化工作流执行性能"
# 预期：requirement.md 应包含 toolkit/controller/workflow/ 路径
2. 上下文校验验证

# 测试 /context-check 是否能发现文档漂移
cd console
# 执行 /context-check，指定 ai-tools 模块
# 预期：报告应标记 /tool/create vs /tool/create-tool 的不一致
3. 分层流程验证

# 测试小功能是否能跳过 stories
cd console
# 创建简单需求："添加 Bot 描述字段长度限制"
# 预期：WORKFLOW.md 指导跳过 /stories，直接 /spec
4. 任务执行约束验证

# 测试 tasks.md 是否包含验证命令
cd console
# 执行 /tasks
# 预期：每个任务包含"验证命令"和"完成标准"字段
5. 文档回写校验验证

# 测试 /drift-check 是否能发现文档与代码不一致
cd console
# 修改代码但不更新文档，然后执行 /drift-check
# 预期：报告应标记不一致项
实施顺序
Phase 1（P0 - 立即修复）：

扩展 skills 扫描范围（4 个文件）
新增 context-check skill
优化 WORKFLOW.md 分层流程
Phase 2（P1 - 短期优化）：

增强 tasks.md 执行约束
新增 drift-check skill
优化 skills 按需生成逻辑
Phase 3（P2 - 长期改进）：

更新 QUICK_REFERENCE.md 决策树
创建自动化脚本（从代码抽取接口清单）
风险与注意事项
向后兼容：现有 skills 调用方式不变，只是增强功能
文档迁移：现有 docsForAi/ 下的文档无需修改，新流程自动适配
学习成本：新增 2 个 skills（context-check、drift-check），需要更新文档说明
执行效率：context-check 和 drift-check 会增加执行时间，但能显著降低返工风险
预期收益
降低返工率：上下文校验 + 文档回写校验，确保基于正确上下文开发
提升执行效率：分层流程 + 按需生成，避免小功能走完整重流程
增强可追溯性：任务状态跟踪 + 验证命令，agent 可自动执行和验证
保持文档同步：drift-check 机制，确保文档与代码长期一致
