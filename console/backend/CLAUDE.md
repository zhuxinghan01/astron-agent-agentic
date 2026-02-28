# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目简介

Astron Console Backend 是 Astron Agent 平台的**后端控制台服务**，提供 AI Agent 管理、工作流编排、模型管理、用户认证等核心 API 功能。采用 Java 21 + Spring Boot 3.5.4 构建，支持高可用部署和企业级安全特性。

## 技术栈

### 核心框架
- **Java**: 21 (LTS)
- **Spring Boot**: 3.5.4
- **Spring Security**: OAuth2 Resource Server
- **MyBatis Plus**: 3.5.7 (ORM 框架)

### 数据层
- **数据库**: MySQL 8.0+
- **缓存**: Redis (Redisson 3.30.0)
- **数据库迁移**: Flyway 10.21.0

### 工具库
- **Lombok**: 1.18.32 (减少样板代码)
- **MapStruct**: 1.5.5 (对象映射)
- **Hutool**: 5.8.27 (Java 工具库)
- **FastJSON2**: 2.0.51 (JSON 处理)
- **OkHttp**: 4.12.0 (HTTP 客户端)
- **SpringDoc**: 2.8.5 (OpenAPI 文档)

### 代码质量工具
- **Spotless**: 2.46.1 (代码格式化)
- **Checkstyle**: 3.6.0 (代码风格检查)
- **SpotBugs**: 4.9.4.0 (静态分析)
- **PMD**: 3.27.0 (代码质量分析)

## 开发命令

### Maven 命令

```bash
# 清理并安装所有模块
mvn clean install

# 跳过测试构建
mvn clean install -DskipTests

# 运行 Hub 模块 (主服务)
cd hub
mvn spring-boot:run

# 运行测试
mvn test

# 运行特定模块的测试
mvn test -pl hub

# 代码格式化 (Spotless)
mvn spotless:apply

# 代码质量检查
mvn checkstyle:check
mvn pmd:check
mvn spotbugs:check
```

### Makefile 命令 (推荐)

```bash
# 格式化 Java 代码
make fmt-java

# 代码质量检查 (Checkstyle + PMD + SpotBugs)
make check-java

# 运行测试
make test-java

# 构建项目
make build-java
```

## Maven 模块结构

项目采用 Maven 多模块结构：

```
console/backend/
├── pom.xml                    # 父 POM (依赖管理)
├── hub/                       # 主 API 服务模块
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/         # Java 源代码
│       │   └── resources/    # 配置文件
│       └── test/             # 测试代码
├── commons/                   # 公共模块
│   ├── pom.xml
│   └── src/
│       ├── main/java/        # DTO、工具类
│       └── test/
└── toolkit/                   # 工具包模块
    ├── pom.xml
    └── src/
```

### 模块说明

#### hub (主服务模块)
- **端口**: 8080
- **职责**: 提供核心业务 API
- **依赖**: commons, toolkit
- **启动类**: `com.iflytek.astron.console.hub.HubApplication`

#### commons (公共模块)
- **职责**: 共享 DTO、工具类、常量定义
- **被依赖**: hub, toolkit

#### toolkit (工具包模块)
- **职责**: 工具服务、第三方集成
- **依赖**: commons

## 代码架构 (Hub 模块)

采用经典的三层架构模式：

```
hub/src/main/java/com/iflytek/astron/console/hub/
├── controller/               # REST API 控制器层
│   ├── AgentController.java
│   ├── WorkflowController.java
│   └── ModelController.java
├── service/                  # 业务逻辑层
│   ├── AgentService.java
│   ├── WorkflowService.java
│   └── impl/                # 服务实现
├── mapper/                   # MyBatis 数据访问层
│   ├── AgentMapper.java
│   └── WorkflowMapper.java
├── entity/                   # 数据库实体类
│   ├── Agent.java
│   └── Workflow.java
├── dto/                      # 数据传输对象
│   ├── request/             # 请求 DTO
│   └── response/            # 响应 DTO
├── config/                   # 配置类
│   ├── SecurityConfig.java
│   ├── RedisConfig.java
│   └── MyBatisPlusConfig.java
├── util/                     # 工具类
├── exception/                # 异常定义
└── HubApplication.java       # 启动类
```

### 架构层次说明

1. **Controller 层**: 处理 HTTP 请求，参数校验，调用 Service
2. **Service 层**: 业务逻辑处理，事务管理
3. **Mapper 层**: 数据库访问，SQL 映射
4. **Entity 层**: 数据库表映射实体
5. **DTO 层**: API 请求/响应对象

## 配置文件

### 主配置文件

- `application.yml` - 主配置
- `application-dev.yml` - 开发环境
- `application-test.yml` - 测试环境
- `application-prod.yml` - 生产环境
- `application-toolkit.yml` - Toolkit 模块配置

### 关键配置项

#### 服务器配置
```yaml
server:
  port: 8080
  servlet:
    context-path: /
```

#### 数据源配置
```yaml
spring:
  datasource:
    url: ${MYSQL_URL}
    username: ${MYSQL_USER}
    password: ${MYSQL_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
```

#### Redis 配置
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      database: ${REDIS_DATABASE_CONSOLE}
      password: ${REDIS_PASSWORD}
```

#### OAuth2 配置
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${OAUTH2_ISSUER_URI}
          jwk-set-uri: ${OAUTH2_JWK_SET_URI}
          audiences:
            - ${OAUTH2_AUDIENCE}
```

## 环境变量

### 必需环境变量

```bash
# 数据库配置
MYSQL_URL=jdbc:mysql://localhost:3306/astron_console
MYSQL_USER=astron
MYSQL_PASSWORD=your-password

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DATABASE_CONSOLE=0
REDIS_PASSWORD=

# OAuth2 认证配置
OAUTH2_ISSUER_URI=http://localhost:8000
OAUTH2_JWK_SET_URI=http://localhost:8000/.well-known/jwks
OAUTH2_AUDIENCE=your-oauth2-client-id

# Flyway 配置
FLYWAY_ENABLED=true
FLYWAY_VALIDATE_ON_MIGRATE=false
```

### 开发环境配置

在 IDE 中配置环境变量，或使用 `.env` 文件 (需要插件支持)。

## 代码质量工具

### Spotless (代码格式化)

使用 Google Java Format 进行代码格式化：

```bash
# 格式化所有代码
mvn spotless:apply

# 检查格式 (不修改)
mvn spotless:check
```

### Checkstyle (代码风格检查)

基于 Google Java Style Guide：

```bash
# 运行 Checkstyle
mvn checkstyle:check

# 生成报告
mvn checkstyle:checkstyle
```

配置文件: `config/checkstyle.xml`

### SpotBugs (静态分析)

检测潜在的 Bug 和代码问题：

```bash
# 运行 SpotBugs
mvn spotbugs:check

# 生成 GUI 报告
mvn spotbugs:gui
```

排除规则: `config/spotbugs-exclude.xml`

### PMD (代码质量分析)

检测代码异味和潜在问题：

```bash
# 运行 PMD
mvn pmd:check

# 生成报告
mvn pmd:pmd
```

规则集: `config/pmd-ruleset.xml`

### 质量标准

- **行长度**: ≤120 字符
- **圈复杂度**: 函数 ≤10，类 ≤40
- **方法长度**: ≤50 行
- **参数数量**: ≤7 个
- **嵌套深度**: ≤3 层

## 数据库迁移 (Flyway)

### 迁移脚本位置

```
hub/src/main/resources/db/migration/
├── V1.0__init_schema.sql
├── V1.1__add_user_table.sql
└── V1.2__add_workflow_table.sql
```

### 命名规范

```
V{version}__{description}.sql
```

- `V`: 版本前缀 (必需)
- `{version}`: 版本号 (如 1.0, 1.1, 2.0)
- `__`: 双下划线分隔符 (必需)
- `{description}`: 描述 (使用下划线连接单词)

### 迁移执行

Flyway 在应用启动时自动执行：

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 1.0
```

### 手动执行迁移

```bash
# 查看迁移状态
mvn flyway:info

# 执行迁移
mvn flyway:migrate

# 清理数据库 (谨慎使用)
mvn flyway:clean
```

## API 开发规范

### RESTful 风格

```java
@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    @GetMapping
    public ResponseEntity<List<AgentDTO>> listAgents() {
        // GET /api/v1/agents - 列表查询
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentDTO> getAgent(@PathVariable Long id) {
        // GET /api/v1/agents/{id} - 详情查询
    }

    @PostMapping
    public ResponseEntity<AgentDTO> createAgent(@RequestBody @Valid AgentCreateRequest request) {
        // POST /api/v1/agents - 创建
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgentDTO> updateAgent(
            @PathVariable Long id,
            @RequestBody @Valid AgentUpdateRequest request) {
        // PUT /api/v1/agents/{id} - 更新
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgent(@PathVariable Long id) {
        // DELETE /api/v1/agents/{id} - 删除
    }
}
```

### 统一响应格式

```java
public class ApiResponse<T> {
    private Integer code;      // 状态码
    private String message;    // 消息
    private T data;           // 数据
    private Long timestamp;   // 时间戳
}
```

### 参数校验

使用 Jakarta Validation (javax.validation):

```java
public class AgentCreateRequest {

    @NotBlank(message = "Agent name cannot be blank")
    @Size(max = 100, message = "Agent name must be less than 100 characters")
    private String name;

    @NotNull(message = "Agent type is required")
    private AgentType type;

    @Email(message = "Invalid email format")
    private String email;
}
```

### 异常处理

使用 `@ControllerAdvice` 统一处理异常：

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        // 业务异常处理
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        // 参数校验异常处理
    }
}
```

## 测试规范

### 单元测试 (JUnit 5 + Mockito)

```java
@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock
    private AgentMapper agentMapper;

    @InjectMocks
    private AgentServiceImpl agentService;

    @Test
    @DisplayName("Should create agent successfully")
    void testCreateAgent() {
        // Given
        AgentCreateRequest request = new AgentCreateRequest();
        request.setName("Test Agent");

        // When
        AgentDTO result = agentService.createAgent(request);

        // Then
        assertNotNull(result);
        assertEquals("Test Agent", result.getName());
        verify(agentMapper, times(1)).insert(any());
    }
}
```

### 集成测试 (Spring Boot Test)

```java
@SpringBootTest
@AutoConfigureMockMvc
class AgentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return agent list")
    void testListAgents() throws Exception {
        mockMvc.perform(get("/api/v1/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
```

### 测试覆盖率

- **目标**: 单元测试覆盖率 ≥70%
- **关键路径**: 覆盖率 ≥90%
- **工具**: JaCoCo

```bash
# 生成测试覆盖率报告
mvn clean test jacoco:report

# 报告位置: target/site/jacoco/index.html
```

## 常用依赖说明

### MyBatis Plus

增强的 MyBatis ORM 框架：

```java
// 继承 BaseMapper 获得 CRUD 方法
public interface AgentMapper extends BaseMapper<Agent> {
    // 自定义查询方法
}

// 使用 QueryWrapper 构建查询
QueryWrapper<Agent> wrapper = new QueryWrapper<>();
wrapper.eq("status", 1)
       .like("name", "test")
       .orderByDesc("create_time");
List<Agent> agents = agentMapper.selectList(wrapper);
```

### Hutool

Java 工具库，提供丰富的工具方法：

```java
// 字符串工具
StrUtil.isBlank(str);
StrUtil.format("Hello {}", name);

// 集合工具
CollUtil.isEmpty(list);
CollUtil.newArrayList(1, 2, 3);

// 日期工具
DateUtil.now();
DateUtil.parse("2024-01-01");

// JSON 工具
JSONUtil.toJsonStr(obj);
JSONUtil.toBean(json, User.class);
```

### Redisson

Redis 客户端，支持分布式锁、缓存等：

```java
@Autowired
private RedissonClient redissonClient;

// 分布式锁
RLock lock = redissonClient.getLock("myLock");
try {
    lock.lock();
    // 业务逻辑
} finally {
    lock.unlock();
}

// 缓存
RBucket<String> bucket = redissonClient.getBucket("key");
bucket.set("value", 10, TimeUnit.MINUTES);
```

### MapStruct

对象映射工具，编译时生成映射代码：

```java
@Mapper(componentModel = "spring")
public interface AgentConverter {

    AgentDTO toDTO(Agent entity);

    Agent toEntity(AgentCreateRequest request);

    List<AgentDTO> toDTOList(List<Agent> entities);
}
```

## 开发最佳实践

### 1. 使用 Lombok 减少样板代码

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agent {
    private Long id;
    private String name;
    private AgentType type;
}
```

### 2. 使用 MyBatis Plus 简化 CRUD

```java
// 继承 ServiceImpl 获得基础 CRUD 方法
@Service
public class AgentServiceImpl extends ServiceImpl<AgentMapper, Agent> implements AgentService {
    // 只需实现业务逻辑
}
```

### 3. 使用 @Transactional 管理事务

```java
@Service
public class AgentService {

    @Transactional(rollbackFor = Exception.class)
    public void createAgentWithWorkflow(AgentCreateRequest request) {
        // 多个数据库操作，自动事务管理
    }
}
```

### 4. 使用缓存提升性能

```java
@Service
public class AgentService {

    @Cacheable(value = "agents", key = "#id")
    public AgentDTO getAgent(Long id) {
        // 结果会被缓存
    }

    @CacheEvict(value = "agents", key = "#id")
    public void deleteAgent(Long id) {
        // 删除缓存
    }
}
```

### 5. 使用异步处理提升响应速度

```java
@Service
public class NotificationService {

    @Async
    public void sendNotification(String message) {
        // 异步执行，不阻塞主线程
    }
}
```

## 相关文档

- [项目根目录 CLAUDE.md](../../CLAUDE.md) - 整体项目说明
- [Console Frontend CLAUDE.md](../frontend/CLAUDE.md) - 前端开发指南
- [Spring Boot 文档](https://docs.spring.io/spring-boot/docs/3.5.4/reference/html/)
- [MyBatis Plus 文档](https://baomidou.com/)

## 故障排查

### 常见问题

1. **数据库连接失败**
   - 检查 `MYSQL_URL` 环境变量
   - 确认 MySQL 服务已启动
   - 验证用户名和密码

2. **Redis 连接失败**
   - 检查 `REDIS_HOST` 和 `REDIS_PORT`
   - 确认 Redis 服务已启动

3. **OAuth2 认证失败**
   - 检查 `OAUTH2_ISSUER_URI` 配置
   - 确认 Casdoor 服务可访问
   - 验证 `OAUTH2_AUDIENCE` 配置

4. **Flyway 迁移失败**
   - 检查迁移脚本语法
   - 查看 `flyway_schema_history` 表
   - 使用 `mvn flyway:repair` 修复

### 日志查看

```bash
# 查看应用日志
tail -f logs/astron-console-hub.log

# 查看 Spring Boot 启动日志
mvn spring-boot:run
```
