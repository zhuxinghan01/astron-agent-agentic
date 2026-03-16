---
change: resource-points-open-source-port-frontend
kind: open-source-sync
source_mrs:
  - backend: "!816"
  - frontend: "!1115"
created: 2026-03-13
status: draft
---

# 资源积分前端页面开源移植文档

本文是后端资源积分主链文档的前端配套文档，面向链路为：

- 后端 diff：`xingchen_pro_webservice !816`
- 后端文档：`requirement.md`
- 前端 diff：`xingchen_pro_webapp !1115`
- 前端文档：本文
- 后端代码：开源 `console/backend/hub`
- 前端代码：后续待补

本文只描述“前端积分相关页面如何承接后端资源能力”，不重复后端资源模型和业务规则。

---

## L0 - MR 结构化记录

## 结论

前端仓库 `hy_spark_agent_builder/xingchen_pro_webapp` 的 `!1115` 并不是一个纯积分 MR，而是一个更大的 `AstronClaw` 模块接入 MR。

其中与这次资源积分开源迁移真正相关的前端部分主要有两类：

- 积分展示与积分管理弹窗
- 套餐/订阅页面中与积分余额和套餐权益直接相关的 UI

其余 `AstronClaw` 聊天、技能、渠道、设置页面属于 SaaS 业务包装层，不应自动等价为资源积分前端开源范围。

## MR 结构化记录

| 字段 | !1115 |
|---|---|
| 项目 | `hy_spark_agent_builder/xingchen_pro_webapp` |
| MR 编号 | `!1115` |
| MR 标题 | `Merge branch 'feat/AstronClaw' of...` |
| 源分支 | `feat/AstronClaw` |
| 目标分支 | `master` |
| 文档层级 | `L1` |
| 变更类型 | `feature` |
| 变更摘要 | 新增 `AstronClaw` 前端模块，包含部署入口、已部署工作台、积分/套餐弹窗和订阅相关 UI |
| 影响范围 | `src/router`、`src/api/claw.ts`、`src/pages/claw/*`、`src/components/Sidebar`、`src/constant` |
| 文件规模 | `111 files` |
| 是否开源 | `maybe` |
| 开源进度 | `pending` |

---

## 1. 背景

后端 `!816` 给 SaaS 提供了资源积分主链，但用户实际感知到的是前端中的“剩余积分、套餐状态、积分流水、购买入口”。

如果只迁后端，不补最小前端承接，开源版虽然具备资源积分能力，但没有可见操作面和验证面。

因此，需要单独梳理前端 diff 中到底哪些页面属于“资源积分前端”，哪些只是 `AstronClaw` 业务包装。

---

## 2. 这份前端文档的定位

本文回答的问题是：

- SaaS 前端哪些页面真正消费了资源积分能力
- 前端用什么接口读取积分和套餐信息
- 哪些页面值得进入开源版前端范围
- 哪些页面虽然出现在同一个前端 MR 中，但不应跟资源积分一起迁移

本文不回答的问题是：

- 后端资源表、错误码、幂等、并发怎么实现
- `AstronClaw` 全量前端页面如何完整开源
- `claw` 订阅、订单、Bot 部署全链路如何在开源复刻

---

## 3. 前端 diff 代码真相

基于前端 MR `!1115` 的 diff，可以确认以下事实：

### 3.1 路由层面

前端新增了 `AstronClaw` 相关路由：

- `/astron-claw`
- `/astron-claw/deployed/chat`
- `/astron-claw/deployed/skills`
- `/astron-claw/deployed/channels`
- `/astron-claw/deployed/settings`
- `/astron-claw/deployed/skills/mine`

这说明 SaaS 前端把积分能力嵌入在一个完整的 `AstronClaw` 产品壳里，而不是独立的“积分中心”页面。

### 3.2 页面层面

前端 diff 中和积分直接相关的页面与组件主要是：

- `src/pages/claw/chat/components/CreditsModal.tsx`
- `src/pages/claw/chat/components/CreditsManagementModal.tsx`
- `src/pages/claw/deployed/index.tsx`
- `src/pages/claw/home/index.tsx`
- `src/pages/claw/constants.ts`

这些文件提供的能力是：

- 展示当前套餐
- 展示剩余积分
- 打开积分管理弹窗
- 展示套餐权益和购买入口
- 在部署前检查套餐状态

### 3.3 API 层面

前端资源相关 API 封装位于 `src/api/claw.ts`。

直接与资源积分相关的接口封装有：

- `getCreditsBalance() -> /xingchen-api/claw/balance`
- `queryResourceFlow() -> /xingchen-api/claw/flow/query`
- `getCurrentPackage() -> /xingchen-api/claw/package`
- `getOrderList() -> /xingchen-api/claw/order/List`
- `checkUnpayOrder() -> /xingchen-api/claw/has-unpay`

其中真正对应后端资源主链的只有：

- 余额查询
- 流水查询

套餐和订单接口属于 `claw` 订阅包装层。

### 3.4 类型层面

前端在 `src/api/claw.ts` 中定义了：

- `ResourceBalanceVO`
- `ResourceFlowVO`
- `ResourceQueryRequest`
- `PackageData`

其中 `ResourceBalanceVO` 和 `ResourceFlowVO` 明显与后端资源接口一一对应。

`PackageData` 则是前端对订阅态的额外抽象，不属于资源积分主链本身。

---

## 4. 前端与后端接口映射

这里以“后端接口能力”倒推“前端最小可迁页面”。

| 前端能力 | 前端封装 | SaaS API 路径 | 后端资源主链关系 |
|---|---|---|---|
| 查询剩余积分 | `getCreditsBalance` | `/xingchen-api/claw/balance` | 对应 `/resource/balance` |
| 查询积分流水 | `queryResourceFlow` | `/xingchen-api/claw/flow/query` | 对应 `/resource/flow/query` |
| 查询当前套餐 | `getCurrentPackage` | `/xingchen-api/claw/package` | `claw` 包装层 |
| 查询订单列表 | `getOrderList` | `/xingchen-api/claw/order/List` | `claw` 包装层 |
| 查询未支付订单 | `checkUnpayOrder` | `/xingchen-api/claw/has-unpay` | `claw` 包装层 |

关键判断：

- 前端对资源系统的“读能力”依赖非常清晰，就是余额和流水
- 前端没有直接接写接口来调用 `grant / deduct / refund / invalidate`
- 写接口主要由订阅、订单、任务消费等后端链路触发，前端只消费结果

因此，开源前端首轮如果只想验证后端 `P0`，理论上只需要补：

- 余额展示
- 流水展示

不需要首轮就把套餐购买、续费、未支付订单处理等都带进来。

---

## 5. 积分相关前端页面拆解

### 5.1 `CreditsModal`

组件：`src/pages/claw/chat/components/CreditsModal.tsx`

职责：

- 展示当前套餐名称
- 展示剩余积分 / 总积分
- 展示简单进度条
- 提供“积分管理”入口，拉起 `CreditsManagementModal`

特点：

- 这是一个轻量读面板
- 非常适合作为开源首轮前端验证入口
- 它只依赖余额数据和套餐状态数据

### 5.2 `CreditsManagementModal`

组件：`src/pages/claw/chat/components/CreditsManagementModal.tsx`

职责：

- 查询积分余额
- 查询套餐状态
- 展示套餐权益和升级入口
- 展示购买记录
- 预留积分流水查询能力

重要观察：

- 组件内部存在 `queryResourceFlow` 的接入
- 但“积分明细查询”当前在 diff 中被注释，且有“限时优惠期间暂不请求积分明细”的说明

这意味着：

- 前端积分管理弹窗是资源积分前端的主页面
- 但其中“消费明细页签”在 SaaS 侧也不是完全稳定的最终态

对开源迁移的含义：

- 可以先迁余额总览部分
- 流水查询 UI 可以做，但没必要照搬 SaaS 当前复杂弹窗结构
- 购买记录和套餐升级属于次级能力，应与资源主链解耦

### 5.3 `DeployedLayout`

页面：`src/pages/claw/deployed/index.tsx`

职责：

- 在已部署工作台右侧浮动菜单中提供积分入口
- 首次加载时查询积分余额和套餐状态
- 点击底部积分按钮时弹出 `CreditsModal`

它实际上承担的是“积分入口容器”角色，而不是积分业务本身。

### 5.4 `HomePage` 中的套餐区域

页面：`src/pages/claw/home/index.tsx`

职责：

- 展示套餐卡片和价格
- 检查用户当前套餐状态
- 若用户是 `FREE` 套餐，则引导升级后再部署
- 使用 `checkUnpayOrder` 和购买链接承接订阅下单

这部分虽然和“积分”强相关，但更准确地说属于：

- 订阅销售页
- 套餐营销页
- `claw` 专属部署前置页

它不应作为资源积分前端 `P0` 的强依赖。

---

## 6. 开源前端建议范围

### 6.1 首轮建议纳入

如果目标是给开源后端 `P0` 提供一个能验证的前端承接面，建议首轮只做：

- 一个积分入口组件
- 一个余额展示组件
- 一个流水查询页面或弹窗

建议保留的前端元素：

- `ResourceBalanceVO` 展示
- `ResourceFlowVO` 列表
- 时间范围筛选
- 简单分页

### 6.2 首轮建议不纳入

以下前端部分虽然出现在 `!1115` 中，但不建议作为首轮资源积分前端迁移：

- 整套 `AstronClaw` 路由壳
- Bot 部署页
- Bot 聊天页
- 技能页
- 渠道页
- 设置页
- 套餐购买链接和订单查询页
- 协议勾选和升级弹窗

原因：

- 这些都依赖 `claw` 产品语义，不是资源积分基础能力
- 它们会把开源前端范围从“积分验证页面”扩大成“订阅产品前端”
- 当前开源后端也没有 `claw/package/order` 这层对应能力

---

## 7. 前端实现缺口与风险

### 7.1 SaaS 前端路径并不直接调用 `/resource/*`

前端当前接的是：

- `/xingchen-api/claw/balance`
- `/xingchen-api/claw/flow/query`

而不是后端资源控制器里的 `/resource/balance` 和 `/resource/flow/query`。

这说明中间存在一层 SaaS 业务包装或转发适配。

开源前端如果直接接开源后端，需要重新定义前端 API 层，不应照搬 SaaS 的 `claw.ts` 路径。

### 7.2 套餐状态和资源余额是两套概念

前端 UI 里混合出现了：

- 套餐：`FREE / PERSONAL / MAJOR`
- 积分余额：`memberBalance / buyBalance / activityBalance`

这两者在 SaaS 里一起出现，但并不是同一件事。

开源迁移时必须拆开：

- 资源积分前端
- 订阅套餐前端

否则后面会把“资源能力”与“销售包装”绑死。

### 7.3 积分流水页在 SaaS 中也未完全稳定

`CreditsManagementModal` 虽然保留了 `queryResourceFlow` 的接入，但当前 diff 里有明确注释说明暂时不请求积分明细。

因此，不应把 SaaS 当前弹窗结构当成必须复刻的最终方案。

---

## 8. 建议迁移 diff 范围

### 8.1 建议保留用于参考的前端 diff

- `src/api/claw.ts`
- `src/pages/claw/chat/components/CreditsModal.tsx`
- `src/pages/claw/chat/components/CreditsManagementModal.tsx`
- `src/pages/claw/deployed/index.tsx`

这些文件用于回答：

- 前端展示了哪些资源数据
- 余额和流水是怎么进入页面的
- 积分入口是放在什么交互位置

### 8.2 建议只做设计参考，不直接迁移的 diff

- `src/pages/claw/home/index.tsx`
- `src/pages/claw/constants.ts`
- `src/constant/index.js`
- `src/router/index.tsx`
- `src/components/Sidebar/index.tsx`

这些文件虽然包含积分和套餐元素，但更多是：

- `AstronClaw` 产品入口
- 订阅营销壳
- 侧边栏导航
- 路由组织方式

它们适合帮助理解 SaaS 产品形态，不适合作为开源资源积分前端的直接复制对象。

---

## 9. 前端开源迁移建议

建议采用“薄前端”方式，不要把 SaaS 的 `AstronClaw` 壳直接带进来。

推荐顺序：

1. 在开源前端新增资源积分 API 层
2. 直接对接开源后端 `/resource/balance`
3. 直接对接开源后端 `/resource/flow/query`
4. 做一个简单的积分余额卡片
5. 做一个简单的积分流水列表页
6. 最后再决定是否需要套餐/会员包装层

推荐页面形态：

- `PointsCard`
- `PointsFlowPage`

而不是首轮就做：

- `AstronClawHomePage`
- `CreditsManagementModal` 全量复刻版
- 套餐购买和未支付订单恢复逻辑

---

## 10. 验收标准

前端资源积分首轮迁移完成后，至少应满足：

1. 能查询当前用户 `POINT` 余额
2. 能展示总余额与来源拆分余额
3. 能按时间范围查询积分流水
4. 能显示流水方向、数量、来源、时间
5. 页面不依赖 `claw/package/order` 这些 SaaS 专属接口

如果继续往套餐 UI 扩展，再额外验收：

1. 套餐状态是否与积分余额严格解耦
2. 购买入口是否被明确标为 SaaS 专属或替换为开源实现

---

## 总结

前端 `!1115` 里真正和资源积分开源迁移相关的，不是整套 `AstronClaw` 页面，而是其中承接“余额 + 流水 + 积分入口”的小部分。

一句话结论：

后端资源积分 `P0` 对应的前端首轮应该迁“积分读界面”，而不是迁“`AstronClaw` 订阅产品壳”。
