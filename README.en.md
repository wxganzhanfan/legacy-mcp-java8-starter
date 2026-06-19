# Legacy MCP Java 8 Starter

A lightweight MCP protocol adapter for Java 8 and Spring Boot 2 legacy applications.

[简体中文](README.md)

## Purpose

Many production Java services still run on Java 8, Spring Boot 2, Spring MVC, and synchronous request handling. They often cannot move to Spring AI, Java 17, WebFlux, Reactor, or Jakarta APIs in the short term. This project provides a small and explicit MCP integration layer so existing business capabilities can be exposed to MCP-capable agents such as Codex, Claude, Hermes, and WorkBuddy.

This project does not try to replicate the full Spring AI MCP stack. It focuses on the most common legacy-service needs: listing tools, calling tools, API-key authentication, per-agent tool allowlists, read/write operation separation, and HTTP or stdio bridge integration.

This project is not an official project of OpenAI, Anthropic, Spring, Model Context Protocol, Hermes, WorkBuddy, or any agent or platform vendor. Product names in the documentation are used only to describe interoperability scenarios, and all trademarks belong to their respective owners.

## Features

- Java 8 bytecode for legacy services and internal Maven repository distribution.
- Spring MVC HTTP endpoint, `/mcp` by default.
- Spring Boot starter auto-configuration, disabled by default and enabled by configuration.
- Multiple agent profiles, each with its own API key, tool allowlist, and write-tool allowlist.
- Read/write tool isolation: write operations require `writeOperation() == true`, global write enablement, and agent-level permission.
- Basic audit events with a logging default and a replaceable `McpAuditPublisher` SPI.
- Stdio-to-HTTP bridge for clients that can only launch stdio MCP servers.
- Boot2 simple sample and Boot2 + Spring Security sample.

## MCP Protocol Scope

Supported in the first release:

- `initialize`
- `notifications/initialized`
- `ping`
- `tools/list`
- `tools/call`

Out of scope for now:

- resources
- prompts
- sampling
- elicitation
- OAuth
- streaming responses

See [docs/protocol-scope.en.md](docs/protocol-scope.en.md) for more details.

## Modules

| Module | Description |
| --- | --- |
| `legacy-mcp-bom` | Dependency BOM for downstream version management. |
| `legacy-mcp-core` | MCP JSON-RPC model, dispatcher, registry, tool API, security context, and audit SPI. |
| `legacy-mcp-spring-webmvc` | Spring MVC HTTP transport. Main code does not directly depend on Servlet API, which helps compatibility validation on newer Boot versions. |
| `legacy-mcp-spring-boot2-autoconfigure` | Spring Boot auto-configuration with Boot2 `spring.factories` and Boot3/Boot4 `AutoConfiguration.imports` metadata. |
| `legacy-mcp-spring-boot2-starter` | Starter dependency for application projects. |
| `legacy-mcp-stdio-bridge` | Stdio-to-HTTP bridge for stdio-only agents. |
| `samples/boot2-java8-simple` | Minimal Boot2 sample. |
| `samples/boot2-security-sample` | Boot2 + Spring Security sample. |

## Quick Start

### 1. Choose an integration mode

#### Mode A: Add the Maven dependency, recommended

For Spring Boot 2.x applications, prefer the starter:

```xml
<dependency>
  <groupId>io.github.wxganzhanfan</groupId>
  <artifactId>legacy-mcp-spring-boot2-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

The primary support target is Java 8 + Spring Boot 2. For Boot3/Boot4 projects that still want dependency-based integration, prefer depending on `legacy-mcp-core` and implementing the `/mcp` endpoint inside the application with its own Spring MVC version:

```xml
<dependency>
  <groupId>io.github.wxganzhanfan</groupId>
  <artifactId>legacy-mcp-core</artifactId>
  <version>0.1.0</version>
</dependency>
```

`legacy-mcp-spring-boot2-starter` keeps Boot3/Boot4 auto-configuration metadata for compatibility validation, but it is still published around the Boot2 dependency model and is not the default production commitment for Boot3/Boot4. Before using it on Boot3/Boot4, verify startup, `tools/list`, critical tool calls, and confirm that Spring dependency versions are not downgraded.

If the artifact has not been published to a Maven repository yet, install it locally first:

```powershell
mvn clean install
```

After installation, other projects on the same machine can resolve it from `~/.m2/repository`. For team and CI/CD usage, publish it to Nexus, Artifactory, or another Maven repository with `mvn deploy`.

#### Mode B: Embed the source code into the application

If a legacy project cannot upgrade, cannot run a separate MCP service, and cannot yet consume artifacts from a Maven repository, you can embed the project source directly into the business application. This is useful for tightly controlled internal networks, fixed historical build pipelines, or early PoC work.

Minimal source set:

- Copy `legacy-mcp-core/src/main/java/io/github/legacy_mcp/core` for protocol models, dispatcher, tool API, and audit SPI.
- If the project exposes a Spring MVC HTTP endpoint, also copy `legacy-mcp-spring-webmvc/src/main/java/io/github/legacy_mcp/webmvc`.
- If you want starter-style auto-configuration, also copy `legacy-mcp-spring-boot2-autoconfigure/src/main/java/io/github/legacy_mcp/autoconfigure`, plus `META-INF/spring.factories` and `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

A more conservative option is to embed only `legacy-mcp-core` and implement the `/mcp` Controller inside the business application. In that setup, the application creates `McpToolRegistry`, `McpDispatcher`, API-key validation, and agent context handling manually. This requires more local code, but gives stronger control over security integration in older systems.

When embedding source code, consider moving the package to an internal namespace such as `com.example.legacy_mcp`. That avoids class conflicts if the application later switches to the Maven dependency.

### 2. Register a tool

The recommended approach is to register explicit `LegacyMcpTool` beans. This keeps the tool name, description, input schema, and write-operation flag clear and reviewable:

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

Write tools must explicitly declare:

```java
public boolean writeOperation() {
    return true;
}
```

You may also use `AnnotatedToolRegistrar` with methods annotated by `@LegacyMcpToolDefinition` and `@LegacyMcpParam`. In Java 8 projects, parameter names must be declared explicitly through `@LegacyMcpParam`; do not rely on compiler parameter metadata. See [docs/tool-authoring-guide.en.md](docs/tool-authoring-guide.en.md).

### 3. Enable the MCP endpoint

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

Write tools require both global write enablement and an agent-level write allowlist:

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

### 4. Call the HTTP MCP endpoint

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/mcp `
  -Headers @{ 'X-MCP-API-Key' = 'dev-key' } `
  -ContentType 'application/json' `
  -Body '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}'
```

Call a tool:

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

## Stdio Bridge

Some agents only support launching MCP servers over stdio. In that case, keep the business application exposing HTTP `/mcp` and run the bridge locally:

```powershell
java -jar legacy-mcp-stdio-bridge\target\legacy-mcp-stdio-bridge-0.1.0.jar `
  --url http://localhost:8080/mcp `
  --api-key dev-key
```

The bridge reads one JSON-RPC request per stdin line, forwards it to the HTTP MCP endpoint, and writes only JSON-RPC responses to stdout. Diagnostics are written to stderr to avoid polluting the MCP message stream.

## Security Recommendations

- Keep the MCP endpoint disabled by default and enable it only where needed.
- Do not hard-code API keys in source code. Use environment variables or a configuration service.
- Use a separate API key for each agent.
- Use `allowed-tools` as a minimum-permission boundary.
- Write tools must pass all checks: `writeOperation() == true`, global write enablement, and agent-level write allowlist.
- In production, expose the endpoint only behind an internal network, gateway, or VPN.
- Add audit records and rollback plans for real business write operations.

See [docs/security-model.en.md](docs/security-model.en.md) for more details.

## Compatibility Matrix

| Runtime | Spring Boot | Status |
| --- | --- | --- |
| JDK 8 | Boot 2.3.x | Target scenario; verify in downstream projects. |
| JDK 8 | Boot 2.5.x | Target scenario; verify in downstream projects. |
| JDK 8 | Boot 2.7.18 | Verified by samples in this repository. |
| JDK 11 | Boot 2.7.x | Expected to work; artifacts remain Java 8 bytecode. |
| JDK 17 | Boot 2.7.x | Expected to work; artifacts remain Java 8 bytecode. |
| JDK 21 | Boot 2.7.x | Not a primary target; verify downstream before production. |
| JDK 17 | Boot 3.x | Prefer `legacy-mcp-core` and a downstream endpoint; the starter is only a compatibility-validation path. |
| JDK 21 | Boot 3.x | Prefer `legacy-mcp-core` and a downstream endpoint; the starter is only a compatibility-validation path. |
| JDK 21 | Boot 4.x | Prefer `legacy-mcp-core` and a downstream endpoint; the starter is only a compatibility-validation path. |

The core target remains Java 8 + Spring Boot 2. Boot3/Boot4 projects can reuse the core protocol layer. If they use the starter directly, downstream dependency management must explicitly control Spring Boot / Spring Framework versions, and project-level integration tests are required. When needed, add a dedicated Boot3/Boot4 adapter module instead of treating the Boot2 starter as a general compatibility promise.

## Build and Verification

Build with Java 8:

```powershell
$env:JAVA8_HOME = '<path-to-jdk8>'
powershell -ExecutionPolicy Bypass -File scripts\verify-java8.ps1
```

If the current shell already uses JDK 8, you can also run:

```powershell
mvn clean verify
```

## Publishing to an Internal Maven Repository

For local development:

```powershell
mvn clean install
```

For reuse across multiple projects:

1. Change the version from `0.1.0-SNAPSHOT` to an internal release version such as `0.1.0`.
2. Configure credentials for the company Maven repository in Maven `settings.xml`.
3. Add release and snapshot repositories under `distributionManagement`.
4. Run `mvn clean deploy`.
5. Let application projects depend on the Maven artifact normally, without relying on local source paths.

## Publishing to Maven Central

This repository is configured with the Central Portal Maven publishing plugin. Before publishing, prepare:

1. The GitHub repository exists and the SCM URL in the POM is reachable.
2. The `io.github.wxganzhanfan` namespace is verified in Sonatype Central Portal.
3. Maven `settings.xml` contains Portal token credentials under server id `central`.
4. A GPG/PGP signing key is available on the local machine or in CI.
5. The version is changed from `0.1.0-SNAPSHOT` to a release version such as `0.1.0`.

Run these checks first:

```powershell
mvn clean verify
mvn -Prelease -Dgpg.skip=true -DskipTests package
mvn -Prelease -DskipTests verify
```

Upload the release with:

```powershell
mvn -Prelease deploy
```

The default `autoPublish=false` setting uploads the deployment for validation, then requires manual confirmation in Central Portal. See [Maven Central release guide](docs/maven-central-release.en.md) for the full workflow.

## Documentation

- [Protocol scope](docs/protocol-scope.en.md)
- [Security model](docs/security-model.en.md)
- [Tool authoring guide](docs/tool-authoring-guide.en.md)
- [Agent integration guide](docs/agent-integration-guide.en.md)
- [Migration notes](docs/migration-notes.en.md)
- [Maven Central release guide](docs/maven-central-release.en.md)
- [Changelog](CHANGELOG.en.md)
- [Security policy](SECURITY.en.md)
- [Third-party notices](THIRD-PARTY-NOTICES.en.md)
- [Trademarks and non-affiliation](TRADEMARKS.en.md)
- [Contributing](CONTRIBUTING.en.md)
- [Code of conduct](CODE_OF_CONDUCT.en.md)

## Contributing

Issues and pull requests are welcome. See [CONTRIBUTING.en.md](CONTRIBUTING.en.md) for the detailed workflow. For new capabilities, please include:

- protocol-scope documentation
- tool or transport tests
- sample validation
- Java 8 bytecode verification

## License

This repository is licensed under the [Apache License 2.0](LICENSE). It is friendly to internal reuse, commercial use, redistribution, and open-source adoption, and it includes an explicit patent grant.

Third-party dependency and trademark notes are documented in [THIRD-PARTY-NOTICES.en.md](THIRD-PARTY-NOTICES.en.md) and [TRADEMARKS.en.md](TRADEMARKS.en.md).
