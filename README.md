# Legacy MCP Java 8 Starter

面向 Java 8 与 Spring Boot 2 旧项目的轻量 MCP 协议适配器。

[English](README.en.md)

## 项目定位

很多存量 Java 服务仍运行在 Java 8、Spring Boot 2、Spring MVC 和同步调用模型上，短期内无法升级到 Spring AI、Java 17、WebFlux、Reactor 或 Jakarta API。本项目提供一个小而明确的 MCP 接入层，让这些旧服务可以把已有业务能力暴露给 Codex、Claude、Hermes、WorkBuddy 等支持 MCP 的 agent。

这个项目不尝试完整复刻 Spring AI MCP。它优先解决旧项目最常见的场景：列出工具、调用工具、做 API key 鉴权、限制 agent 可调用工具、区分读写操作，并通过 HTTP 或 stdio bridge 与不同 agent 对接。

本项目不是 OpenAI、Anthropic、Spring、Model Context Protocol、Hermes、WorkBuddy 或任何 agent / 平台厂商的官方项目。文档中的产品名仅用于说明兼容接入方式，相关商标归各自权利人所有。

## 当前能力

- Java 8 字节码，适合旧项目和公司内部制品库分发。
- Spring MVC HTTP endpoint，默认路径为 `/mcp`。
- Spring Boot starter 自动装配，默认关闭，按配置启用。
- 支持多 agent 配置，每个 agent 独立 API key、工具白名单和写工具白名单。
- 支持读写工具隔离：写操作需要工具声明 `writeOperation() == true`，同时全局与 agent 白名单均允许。
- 支持基础审计事件，默认输出到日志，也可以替换为自定义 `McpAuditPublisher`。
- 提供 stdio-to-HTTP bridge，适配只支持启动 stdio MCP server 的客户端。
- 提供 Boot2 简单示例和 Boot2 + Spring Security 示例。

## MCP 协议范围

第一阶段支持：

- `initialize`
- `notifications/initialized`
- `ping`
- `tools/list`
- `tools/call`

暂不支持：

- resources
- prompts
- sampling
- elicitation
- OAuth
- streaming responses

更详细的边界说明见 [docs/protocol-scope.md](docs/protocol-scope.md)。

## 模块说明

| 模块 | 说明 |
| --- | --- |
| `legacy-mcp-bom` | 依赖版本 BOM，方便下游统一管理版本。 |
| `legacy-mcp-core` | MCP JSON-RPC 模型、dispatcher、工具注册表、工具接口、安全上下文和审计 SPI。 |
| `legacy-mcp-spring-webmvc` | Spring MVC HTTP 传输层。主代码不直接依赖 Servlet API，便于更高版本 Boot 项目做兼容性验证。 |
| `legacy-mcp-spring-boot2-autoconfigure` | Spring Boot 自动配置，提供 Boot2 `spring.factories` 和 Boot3/Boot4 `AutoConfiguration.imports` 索引。 |
| `legacy-mcp-spring-boot2-starter` | 给业务项目使用的 starter 聚合依赖。 |
| `legacy-mcp-stdio-bridge` | stdio 到 HTTP 的桥接程序，适配 stdio-only agent。 |
| `samples/boot2-java8-simple` | 最小 Boot2 示例。 |
| `samples/boot2-security-sample` | Boot2 + Spring Security 示例。 |

## 快速开始

### 1. 选择接入方式

#### 方式 A：通过 Maven 依赖接入，推荐

Spring Boot 2.x 项目优先添加 starter：

```xml
<dependency>
  <groupId>io.github.wxganzhanfan</groupId>
  <artifactId>legacy-mcp-spring-boot2-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

当前主支持目标是 Java 8 + Spring Boot 2。Boot3/Boot4 项目如果需要通过依赖方式接入，建议优先只依赖 `legacy-mcp-core`，并在业务项目中使用自身 Spring MVC 版本手写 `/mcp` endpoint：

```xml
<dependency>
  <groupId>io.github.wxganzhanfan</groupId>
  <artifactId>legacy-mcp-core</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

`legacy-mcp-spring-boot2-starter` 保留了 Boot3/Boot4 自动配置索引，便于兼容性验证；但它仍会围绕 Boot2 依赖模型发布，不作为 Boot3/Boot4 的默认生产接入承诺。Boot3/Boot4 使用前需要在下游项目验证启动、`tools/list` 和关键工具调用，并确认 Spring 依赖版本没有被降级。

如果还没有发布到 Maven 私服，可以先在本机执行：

```powershell
mvn clean install
```

安装后，其他本机项目会从 `~/.m2/repository` 解析这个依赖。团队使用或 CI/CD 使用时，建议通过 `mvn deploy` 发布到 Nexus、Artifactory 或其他 Maven 制品库。

#### 方式 B：直接把源码放进业务项目

如果旧项目不能升级、不能单独启动 MCP 服务，也暂时不能从 Maven 私服拉取依赖，可以把本项目的核心源码内嵌到业务项目中使用。适合强管控内网、历史项目构建链路很固定、或需要先做 PoC 的场景。

最小内嵌范围：

- 复制 `legacy-mcp-core/src/main/java/io/github/legacy_mcp/core`，获得协议模型、dispatcher、工具接口、审计 SPI。
- 如果项目使用 Spring MVC HTTP endpoint，继续复制 `legacy-mcp-spring-webmvc/src/main/java/io/github/legacy_mcp/webmvc`。
- 如果希望保留 starter 风格自动装配，继续复制 `legacy-mcp-spring-boot2-autoconfigure/src/main/java/io/github/legacy_mcp/autoconfigure`，以及 `META-INF/spring.factories` 和 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。

更保守的做法是只内嵌 `legacy-mcp-core`，然后在业务项目里自己写一个 `/mcp` Controller，手动创建 `McpToolRegistry`、`McpDispatcher`、API key 校验和 agent 上下文。这种方式代码量稍多，但对旧项目侵入更可控，也便于接入现有安全体系。

源码内嵌时建议把包名迁移到公司内部命名空间，例如 `com.example.legacy_mcp`，避免以后又引入 Maven 依赖时发生类冲突。

### 2. 注册工具

推荐显式注册 `LegacyMcpTool` Bean，这样工具名称、描述、参数和写操作属性都清楚可控：

```java
@Bean
public LegacyMcpTool echoTool() {
    return new LegacyMcpTool() {
        public String name() {
            return "demo.echo";
        }

        public String title() {
            return "Echo";
        }

        public String description() {
            return "Echoes the supplied message.";
        }

        public McpJsonSchema inputSchema() {
            return McpJsonSchema.object()
                    .property("message", McpJsonSchema.string("Message to echo."))
                    .required("message");
        }

        public Object call(Map<String, Object> arguments, McpAgentContext context) {
            return arguments.get("message");
        }
    };
}
```

写操作工具需要额外声明：

```java
public boolean writeOperation() {
    return true;
}
```

也可以使用 `AnnotatedToolRegistrar` 注册带 `@LegacyMcpToolDefinition` 和 `@LegacyMcpParam` 的方法。Java 8 项目中必须显式写参数名，不要依赖编译器参数名元数据。详细说明见 [docs/tool-authoring-guide.md](docs/tool-authoring-guide.md)。

### 3. 开启 MCP endpoint

```yaml
legacy:
  mcp:
    enabled: true
    endpoint: /mcp
    server-name: legacy-mcp-server
    server-version: 0.1.0
    security:
      auth-header: X-MCP-API-Key
      write-tools-enabled: false
    agents:
      codex:
        api-key: ${MCP_CODEX_API_KEY:dev-key}
        allowed-tools:
          - demo.echo
```

写工具需要同时开启全局写权限和 agent 写工具白名单：

```yaml
legacy:
  mcp:
    security:
      write-tools-enabled: true
    agents:
      codex:
        write-allowed-tools:
          - demo.notice.publish
```

### 4. 调用 HTTP MCP endpoint

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/mcp `
  -Headers @{ 'X-MCP-API-Key' = 'dev-key' } `
  -ContentType 'application/json' `
  -Body '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}'
```

调用工具：

```powershell
$body = @{
  jsonrpc = '2.0'
  id = 2
  method = 'tools/call'
  params = @{
    name = 'demo.echo'
    arguments = @{ message = 'hello' }
  }
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Method Post -Uri http://localhost:8080/mcp `
  -Headers @{ 'X-MCP-API-Key' = 'dev-key' } `
  -ContentType 'application/json; charset=utf-8' `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
```

## stdio bridge

一些 agent 只支持通过 stdio 启动 MCP server。此时可以让业务系统继续提供 HTTP `/mcp`，再在本机运行 bridge：

```powershell
java -jar legacy-mcp-stdio-bridge\target\legacy-mcp-stdio-bridge-0.1.0-SNAPSHOT.jar `
  --url http://localhost:8080/mcp `
  --api-key dev-key
```

bridge 从 stdin 读取每行一个 JSON-RPC 请求，转发到 HTTP MCP endpoint，并只把 JSON-RPC 响应写到 stdout。诊断日志写到 stderr，避免污染 MCP 消息流。

## 安全建议

- 默认不要启用 MCP endpoint，只在明确需要的环境开启。
- 不要把 API key 写死在源码中，使用环境变量或配置中心。
- 为不同 agent 配置不同 API key。
- 使用 `allowed-tools` 做最小权限控制。
- 写工具必须同时满足 `writeOperation() == true`、全局写权限开启、agent 写工具白名单允许。
- 生产环境建议只暴露在内网、网关或 VPN 后面。
- 对真实业务写操作补充审计记录和回滚策略。

更详细的安全模型见 [docs/security-model.md](docs/security-model.md)。

## 兼容性矩阵

| Runtime | Spring Boot | 状态 |
| --- | --- | --- |
| JDK 8 | Boot 2.3.x | 目标场景，建议在下游项目验证。 |
| JDK 8 | Boot 2.5.x | 目标场景，建议在下游项目验证。 |
| JDK 8 | Boot 2.7.18 | 已由本仓库 sample 验证。 |
| JDK 11 | Boot 2.7.x | 预期可用，产物仍保持 Java 8 字节码。 |
| JDK 17 | Boot 2.7.x | 预期可用，产物仍保持 Java 8 字节码。 |
| JDK 21 | Boot 2.7.x | 非主要目标，上线前需要下游验证。 |
| JDK 17 | Boot 3.x | 建议优先使用 `legacy-mcp-core` 并在下游手写 endpoint；starter 仅作为兼容性验证路径。 |
| JDK 21 | Boot 3.x | 建议优先使用 `legacy-mcp-core` 并在下游手写 endpoint；starter 仅作为兼容性验证路径。 |
| JDK 21 | Boot 4.x | 建议优先使用 `legacy-mcp-core` 并在下游手写 endpoint；starter 仅作为兼容性验证路径。 |

本项目的核心目标仍然是 Java 8 + Spring Boot 2。Boot3/Boot4 可以复用 core 协议层；如果要直接使用 starter，需要由下游项目的 dependency management 明确控制 Spring Boot / Spring Framework 版本，并补充项目级集成测试。必要时建议增加专用 Boot3/Boot4 适配模块，而不是把 Boot2 starter 视为通用承诺。

## 构建与验证

使用 Java 8 构建：

```powershell
$env:JAVA8_HOME = '<path-to-jdk8>'
powershell -ExecutionPolicy Bypass -File scripts\verify-java8.ps1
```

如果当前 shell 已经使用 JDK 8，也可以直接执行：

```powershell
mvn clean verify
```

## 发布到公司 Maven 私服

开发期可以使用：

```powershell
mvn clean install
```

正式给多个项目使用时，建议：

1. 将版本从 `0.1.0-SNAPSHOT` 调整为内部发布版本，例如 `0.1.0`。
2. 在 Maven `settings.xml` 中配置公司制品库凭据。
3. 在项目 `distributionManagement` 中配置 releases 和 snapshots 仓库。
4. 执行 `mvn clean deploy`。
5. 业务项目只保留正常 Maven dependency，不再依赖本地源码路径。

## 文档

- [协议范围](docs/protocol-scope.md)
- [安全模型](docs/security-model.md)
- [工具开发指南](docs/tool-authoring-guide.md)
- [Agent 接入指南](docs/agent-integration-guide.md)
- [迁移说明](docs/migration-notes.md)
- [更新日志](CHANGELOG.md)
- [安全政策](SECURITY.md)
- [第三方开源声明](THIRD-PARTY-NOTICES.md)
- [商标与非官方声明](TRADEMARKS.md)
- [贡献指南](CONTRIBUTING.md)
- [行为准则](CODE_OF_CONDUCT.md)

## 贡献

欢迎提交 issue 和 pull request。详细流程见 [CONTRIBUTING.md](CONTRIBUTING.md)。建议新增功能时同时补充：

- 协议范围说明
- 工具或传输层测试
- sample 验证
- Java 8 字节码验证

## License

当前仓库使用 [Apache License 2.0](LICENSE)。这对企业内部复用、二次开发、商业使用和开源传播都比较友好，同时包含专利授权条款。

第三方依赖和商标说明分别见 [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md) 与 [TRADEMARKS.md](TRADEMARKS.md)。
