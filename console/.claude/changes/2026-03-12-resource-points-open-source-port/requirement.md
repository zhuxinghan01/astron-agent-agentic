---
change: resource-points-open-source-port
kind: open-source-sync
source_mrs:
  - "!816"
  - "!817"
  - "!818"
created: 2026-03-12
status: draft
---

# 资源积分管理系统开源移植文档

本文合并 `L0 + L1 + L2`，作为 `!816 / !817 / !818` 这一批 MR 的开源衔接总文档。

---

## L0 - MR 结构化记录

## 结论

本批 MR 中，只有 `!816` 的资源积分主干进入开源候选范围；`!817`、`!818` 都属于 `claw` 订阅补丁，不纳入本轮开源迭代。

## MR 结构化记录

| 字段 | !816 | !817 | !818 |
|---|---|---|---|
| MR 编号 | `!816` | `!817` | `!818` |
| MR 标题 | `Feature points` | `update` | `feature-claw-type` |
| 源分支 | `feature-points` | `feature-points` | `feature-claw-type` |
| 目标分支 | `master` | `master` | `master` |
| 文档层级 | `L2` | `L0` | `L0` |
| 变更类型 | `feature` | `refactor` | `feature` |
| 变更摘要 | 新增资源积分主干：资源账户、流水、幂等请求、扣减明细、资源接口 | `claw` 控制器拆分，新增内部接口 | `claw` 套餐续订和积分发放补丁 |
| 影响范围 | `xingchen-model`、`xingchen-service`、`xingchen-z-authorize` | `xingchen-z-pro`、`xingchen-z-authorize` | `xingchen-service`、`xingchen-z-authorize` |
| 文件规模 | `57 files (+2491 -4)` | `3 files (+31 -1)` | `2 files (+43 -8)` |
| 是否开源 | `yes` | `no` | `no` |
| 开源进度 | `in_progress` | `not_needed` | `not_needed` |

## 开源范围判断

### 纳入本轮开源

- `!816` 中与资源账户核心直接相关的代码
- `CommonResCode` 资源错误码
- `entity.point` 下的核心实体、DTO、VO、校验器
- `Resource*Mapper`
- `ResourceService` / `ResourceServiceImpl`
- `ResourceController`

### 暂不纳入本轮开源

- 所有 `claw` 订阅链路
- `ClawSubscription`、`ClawSubscriptionMapper`
- `ClawSubscriptionService` / `ClawSubscriptionServiceImpl`
- `ClawController`、`ClawInnerController`
- `OrderService` / `OrderServiceImpl`
- `!817`、`!818` 的全部改动

### 延后评估

- `SwitchResource*`
- `BatchResource*`
- 原因是不影响积分账户主闭环，且当前实现与注释存在不一致

---

## L1 - 详细规格说明

## 1. 背景

SaaS 版在 `!816` 引入了一套统一资源账户能力，用来承载积分类资源的发放、消费、失效和退还。同批 MR 还顺手接入了 `claw` 订阅，导致“资源系统核心能力”和“某个 SaaS 业务入口”混在了一起。

开源版本轮目标不是复制整批 MR，而是抽出其中可复用的资源积分主干，形成可快速迭代的基础设施。

## 2. 本轮开源范围

### 2.1 P0 必须纳入

- 4 张核心表对应的领域模型
- 资源错误码
- 资源主服务：
  - 发放 `grant`
  - 扣减 `deduct`
  - 人工扣减 `manual-deduct`
  - 余额查询 `balance`
  - 流水查询 `flow/query`
  - 失效 `invalidate-member`
  - 覆盖 `override`
  - 退还 `refund`

### 2.2 P1 可选后置

- 批量发放 `BatchResourceService`
- 开关资源 `SwitchResourceService`

### 2.3 明确不纳入

- `claw` 订阅模型、控制器、服务、订单查询
- `claw` 套餐到积分额度的映射
- `claw` 内部续费接口与批量续费接口

## 3. 代码真相

### 3.1 当前真正落地的资源类型

按当前代码实现，`ResourceType` 只定义了 `POINT`，不是注释里宣称的 `POINT/ENERGY/RESOURCE`。

这意味着：

- 资源账户主链已经落地
- 首轮实际可用的数量型资源只有积分
- 多资源类型更适合在开源版作为后续扩展点，而不是本轮既定能力

### 3.2 当前可复用的通用规则

- 来源类型：`MEMBER`、`BUY`、`ACTIVITY`、`MANUAL`
- 请求类型：当前只有 `AGENT`
- 操作类型：`ADD`、`DEDUCT`、`MANUAL_DEDUCT`、`OVERRIDE`、`INVALIDATE`、`REFUND`
- 流水方向：`ADD(1)`、`DEDUCT(2)`、`FAILURE(3)`、`REFUND(4)`

## 4. 核心数据模型

### 4.1 `resource_record`

用途：资源批次账户。每次发放都会形成一条批次记录。

关键字段：

- `uid`
- `resource_type`
- `source_type`
- `total_amount`
- `used_amount`
- `remain_amount`
- `grant_time`
- `expire_time`
- `status`
- `version`

关键语义：

- `version` 用于乐观锁
- 过期不是靠状态流转，而是靠 `expire_time > now()` 判断
- `status` 当前只有 `VALID / USED_UP / INVALID`

### 4.2 `resource_flow`

用途：资源流水与审计轨迹。

关键字段：

- `flow_no`
- `uid`
- `resource_type`
- `record_id`
- `direction`
- `amount`
- `request_id`
- `operator_name`
- `reason`
- `remark`
- `visible`

关键语义：

- 用户可见流水通过 `visible != 0` 查询
- 失效和覆盖旧资源时会写不可见流水，用于后台审计

### 4.3 `resource_request`

用途：幂等请求账本。

关键字段：

- `uid`
- `resource_type`
- `request_id`
- `request_type`
- `biz_id`
- `operation_type`
- `status`

开源实现必须补齐唯一约束：

- `resource_type + request_type + request_id`

### 4.4 `resource_deduct_detail`

用途：记录一次扣减具体消耗了哪些批次，为精确退还提供依据。

关键字段：

- `flow_id`
- `record_id`
- `uid`
- `resource_type`
- `amount`

这张表不能省，否则无法按原批次退还。

## 5. 对外接口面

当前主体接口位于 [`ResourceController.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-z-authorize/src/main/java/com/iflytek/xingchen/controller/point/ResourceController.java)。

| 接口 | 方法 | 说明 | 开源优先级 |
|---|---|---|---|
| `/resource/grant` | `POST` | 发放积分 | P0 |
| `/resource/deduct` | `POST` | 消费扣减 | P0 |
| `/resource/manual-deduct` | `POST` | 后台人工扣减 | P0 |
| `/resource/balance` | `GET` | 查询用户余额 | P0 |
| `/resource/flow/query` | `POST` | 查询流水 | P0 |
| `/resource/invalidate-member` | `POST` | 失效某来源积分 | P0 |
| `/resource/override` | `POST` | 覆盖旧积分并重建新批次 | P0 |
| `/resource/refund` | `POST` | 按原扣减请求退还 | P0 |
| `/resource/batch-grant` | `POST` | 批量发放 | P1 |

说明：

- 当前控制器是偏内部接口风格，很多接口直接接收 `uid`
- 如果开源版走用户态接口，需要补认证上下文到 `uid` 的注入逻辑

## 6. 关键业务规则

### 6.1 幂等规则

- 幂等键：`resourceType + requestType + requestId`
- 已成功请求：直接返回
- 处理中请求：抛 `RESOURCE_REQUEST_IN_PROCESS`
- 重复插入唯一键：抛 `RESOURCE_IDEMPOTENT_DUPLICATE`

### 6.2 并发控制

- 锁粒度：`resource:balance:lock:{uid}:{resourceType}`
- 进入扣减、人工扣减、失效、覆盖、退还前先加分布式锁
- 批次更新使用乐观锁字段 `version`

### 6.3 扣减规则

- 仅从 `VALID` 且未过期、`remain_amount > 0` 的批次扣减
- 来源优先级：`ACTIVITY > MEMBER/MANUAL > BUY`
- 同优先级内按更早到期的批次优先消耗
- 如果申请扣减量大于可用余额，不报错，降级为按可用余额实际扣减

### 6.4 覆盖规则

- 先把当前有效批次整体标记为 `INVALID`
- 为旧批次写不可见失效流水
- 再新建一条新的有效批次

### 6.5 失效规则

- 当前实现按 `uid + resourceType + sourceType` 整体失效
- 失效操作也会落 `resource_request`
- 失效流水默认不可见

### 6.6 退还规则

- 只能按 `originalRequestId` 找回原始扣减流水
- 再根据 `resource_deduct_detail` 精确退回对应批次
- 找不到原扣减流水或扣减明细时直接报错

## 7. 错误码范围

资源系统新增错误码位于 [`CommonResCode.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-model/src/main/java/com/iflytek/voicecloud/iflygpt/model/dto/common/CommonResCode.java#L552)：

- `14001` 资源余额不足
- `14002` 请求处理中
- `14003` 资源并发冲突
- `14004` 幂等重复请求
- `14005` 原始扣减不存在
- `14006` 扣减明细不存在
- `14007` 资源记录不存在
- `14008` 资源退还并发冲突

## 8. 建议迁移 diff 范围

### 8.1 P0 核心 diff

- [`CommonResCode.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-model/src/main/java/com/iflytek/voicecloud/iflygpt/model/dto/common/CommonResCode.java)
- `xingchen-service/src/main/java/com/iflytek/xingchen/entity/point/`
  - 保留：`ResourceRecord`、`ResourceFlow`、`ResourceRequest`、`ResourceDeductDetail`
  - 保留：`dto/Resource*.java`
  - 保留：`enums/Direction`、`OperationType`、`RequestStatus`、`RequestType`、`ResourceStatus`、`ResourceType`、`SourceType`
  - 保留：`validation/*`
  - 保留：`vo/ResourceBalanceVO`、`vo/ResourceFlowVO`
- [`ResourceRecordMapper.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-service/src/main/java/com/iflytek/xingchen/mapper/mysql/spark/singleDbMapper/ResourceRecordMapper.java)
- [`ResourceFlowMapper.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-service/src/main/java/com/iflytek/xingchen/mapper/mysql/spark/singleDbMapper/ResourceFlowMapper.java)
- [`ResourceRequestMapper.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-service/src/main/java/com/iflytek/xingchen/mapper/mysql/spark/singleDbMapper/ResourceRequestMapper.java)
- [`ResourceDeductDetailMapper.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-service/src/main/java/com/iflytek/xingchen/mapper/mysql/spark/singleDbMapper/ResourceDeductDetailMapper.java)
- [`ResourceService.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-service/src/main/java/com/iflytek/xingchen/service/point/ResourceService.java)
- [`ResourceServiceImpl.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-service/src/main/java/com/iflytek/xingchen/service/point/impl/ResourceServiceImpl.java)
- [`ResourceController.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-z-authorize/src/main/java/com/iflytek/xingchen/controller/point/ResourceController.java)

### 8.2 P1 可选 diff

- [`BatchResourceService.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-service/src/main/java/com/iflytek/xingchen/service/point/BatchResourceService.java)
- [`BatchResourceServiceImpl.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-service/src/main/java/com/iflytek/xingchen/service/point/impl/BatchResourceServiceImpl.java)
- [`SwitchResourceService.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-service/src/main/java/com/iflytek/xingchen/service/point/SwitchResourceService.java)
- [`SwitchResourceServiceImpl.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-service/src/main/java/com/iflytek/xingchen/service/point/impl/SwitchResourceServiceImpl.java)
- [`SwitchResourceController.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-z-authorize/src/main/java/com/iflytek/xingchen/controller/point/SwitchResourceController.java)
- `dto/SwitchResource*.java`
- `vo/SwitchResourceStatusVO.java`
- `enums/SwitchResourceType.java`

### 8.3 明确排除的 diff

- [`ClawSubscription.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-model/src/main/java/com/iflytek/voicecloud/iflygpt/model/entity/ClawSubscription.java)
- `ClawSubscriptionMapper*`
- `ClawSubscriptionService*`
- [`ClawInnerController.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-z-authorize/src/main/java/com/iflytek/xingchen/controller/point/ClawInnerController.java)
- [`ClawController.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-z-pro/src/main/java/com/iflytek/xingchen/controller/claw/ClawController.java)
- `OrderService*`
- `ActAwardServiceImpl` 的相关改动
- `!817`、`!818` 的全部变更

## 9. 当前实现缺口

### 9.1 `SWITCH_*` 实现未闭合

- [`ResourceType.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-service/src/main/java/com/iflytek/xingchen/entity/point/enums/ResourceType.java) 只定义了 `POINT`
- [`ResourceTypeValidator.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-service/src/main/java/com/iflytek/xingchen/entity/point/validation/ResourceTypeValidator.java) 实际不会放行 `SWITCH_*`
- `ResourceType.isSwitchType()` 也不会识别 `SWITCH_*`

结论：开关资源相关代码更像设计稿已入仓，不是稳定可复用能力。

### 9.2 `sourceType` 默认值未真正实现

- [`ResourceInvalidateMemberRequest.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-service/src/main/java/com/iflytek/xingchen/entity/point/dto/ResourceInvalidateMemberRequest.java) 注释写不传默认 `MEMBER`
- [`ResourceServiceImpl.java`](/Users/zxh/Desktop/project/xingchen_pro_webservice/xingchen-service/src/main/java/com/iflytek/xingchen/service/point/impl/ResourceServiceImpl.java#L460) 实际会直接校验传入值

结论：开源版如果保留这个接口，应显式实现默认值，或者改成必填。

---

## L2 - ADR 决策

## 标题

从 `!816 / !817 / !818` 中抽离资源积分核心，首轮开源不包含 `claw` 订阅链路。

## 日期

2026-03-12

## 状态

Accepted

## 决策

### 1. 首轮只抽资源积分核心

开源首轮保留以下能力：

- 资源批次账户 `resource_record`
- 资源流水 `resource_flow`
- 幂等请求账本 `resource_request`
- 扣减明细 `resource_deduct_detail`
- 发放、扣减、人工扣减、查询、覆盖、失效、退还

### 2. `claw` 作为外部事件源，不进入开源主干

开源版不迁移：

- `ClawSubscription`
- `ClawSubscriptionService`
- `ClawController`
- `ClawInnerController`
- `OrderService`
- `!817`、`!818` 的全部变更

如果未来开源版存在自己的订阅、会员、充值体系，应通过新的业务适配层调用资源服务，而不是继续沿用 `claw` 模型。

### 3. 首轮按积分账户落地，不强行宣称多资源通用

- 不把 `ENERGY/RESOURCE` 写成已交付能力
- 保留 `resource_type` 字段和扩展点，为后续多资源演进预留空间

### 4. 批量发放和开关资源后置

`BatchResourceService` 与 `SwitchResourceService` 暂不作为首轮开源门槛。

原因：

- 不影响积分主闭环
- 现有 `SWITCH_*` 识别与校验逻辑不一致
- 直接迁移会把开源范围不必要地拉大

## 备选方案

### 方案 A：整批 MR 原样开源

不采纳。原因是会把 SaaS 专属订阅业务、订单模型、套餐常量一起带进开源。

### 方案 B：只做一张余额表，放弃批次与明细

不采纳。原因是无法支持来源优先级扣减、精确退还和审计。

### 方案 C：首轮就同时支持积分、能量、通用资源、开关资源

不采纳。原因是当前代码并未真正完成这部分实现，且会放大首轮复杂度。

## 影响

### 正向影响

- 开源范围更小，交付路径更清晰
- 资源系统可以作为独立基础能力复用
- 后续任何业务入口都可以走统一的发放/扣减/退还协议

### 代价

- 需要在开源版补自己的业务接入层
- 如果未来要支持多资源或开关资源，还需要二次演进
- 需要补齐数据库 DDL、唯一索引和默认值策略

## 开源版衔接

### 必须保留的设计点

- 幂等账本 `resource_request`
- 扣减明细 `resource_deduct_detail`
- 分布式锁 + 乐观锁的并发控制
- 覆盖/失效写不可见流水的审计策略

### 首轮建议迁移顺序

1. 建表与索引
2. 错误码
3. `ResourceServiceImpl`
4. `ResourceController`
5. 业务接入层
