# Agent 接入指南

[English](agent-integration-guide.en.md)

本文说明 Codex、Claude、Hermes、WorkBuddy 或其他 MCP agent 如何接入基于本项目构建的 MCP 服务。

本文中的 agent 和平台名称仅用于说明接入方式。本项目不是这些产品或厂商的官方项目，也不代表其官方支持、认证或背书。

## 接入前提

业务项目需要先完成 MCP 服务端接入，并确认 HTTP endpoint 可用：

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
      claude:
        api-key: ${MCP_CLAUDE_API_KEY:claude-dev-key}
        allowed-tools:
          - demo.echo
```

建议每个 agent 使用独立配置：

- 独立 `api-key`
- 独立 `allowed-tools`
- 独立 `write-allowed-tools`
- 独立审计身份，例如 `codex`、`claude`、`hermes`、`workbuddy`

这样可以在审计日志中区分不同调用来源，也能避免某个 agent 拿到过大的工具权限。

## 接入方式选择

| agent 能力 | 推荐接入方式 | 说明 |
| --- | --- | --- |
| 支持 HTTP MCP server | 直连业务项目 `/mcp` endpoint | 配置简单，少一层进程。 |
| 只支持 stdio MCP server | 使用 `legacy-mcp-stdio-bridge` | agent 启动 bridge，bridge 再转发到 HTTP endpoint。 |
| 不支持标准 MCP，只支持自定义插件或 HTTP tool | 调用 HTTP JSON-RPC endpoint | 需要 agent 平台允许配置请求头和 JSON 请求体。 |

本项目服务端实际暴露的是 HTTP JSON-RPC endpoint。stdio bridge 只是为了兼容只能启动本地 stdio MCP server 的 agent。

## 方式一：HTTP 直连接入

如果 agent 支持配置远程 HTTP MCP server，可以直接配置：

```text
URL: http://localhost:8080/mcp
Header: X-MCP-API-Key: dev-key
Transport: HTTP JSON-RPC
```

先用命令验证服务端：

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/mcp `
  -Headers @{ 'X-MCP-API-Key' = 'dev-key' } `
  -ContentType 'application/json' `
  -Body '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}'
```

如果返回 `result.tools`，说明 agent 侧可以继续接入。

## 方式二：stdio bridge 接入

如果 agent 只支持 stdio MCP server，则先构建 bridge：

```powershell
mvn -pl legacy-mcp-stdio-bridge -am package -DskipTests
```

然后让 agent 启动这个 jar：

```powershell
java -jar <repo>\legacy-mcp-stdio-bridge\target\legacy-mcp-stdio-bridge-0.1.0.jar `
  --url http://localhost:8080/mcp `
  --api-key dev-key `
  --api-key-header X-MCP-API-Key `
  --timeout-ms 30000
```

参数说明：

| 参数 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `--url` | 是 | 无 | 业务项目 MCP HTTP endpoint，例如 `http://localhost:8080/mcp`。 |
| `--api-key` | 否 | 无 | 传给业务项目的 agent API key。 |
| `--api-key-header` | 否 | `X-MCP-API-Key` | API key 请求头名称，需要与 `legacy.mcp.security.auth-header` 一致。 |
| `--timeout-ms` | 否 | `30000` | HTTP 转发超时时间。 |

bridge 行为：

- stdin：每行读取一个 JSON-RPC 请求。
- stdout：只输出 JSON-RPC 响应。
- stderr：输出诊断日志。

不要把调试日志写到 stdout，否则会污染 MCP 协议消息流。

## Codex 接入示例

如果 Codex 环境支持 HTTP MCP server，优先使用 HTTP 直连：

```json
{
  "name": "legacy-mcp-demo",
  "transport": "http",
  "url": "http://localhost:8080/mcp",
  "headers": {
    "X-MCP-API-Key": "dev-key"
  }
}
```

如果 Codex 环境只支持 stdio MCP server，则使用 bridge：

```json
{
  "name": "legacy-mcp-demo",
  "transport": "stdio",
  "command": "java",
  "args": [
    "-jar",
    "C:\\path\\to\\legacy-mcp-java8-starter\\legacy-mcp-stdio-bridge\\target\\legacy-mcp-stdio-bridge-0.1.0.jar",
    "--url",
    "http://localhost:8080/mcp",
    "--api-key",
    "dev-key",
    "--api-key-header",
    "X-MCP-API-Key"
  ]
}
```

不同 Codex 版本或部署形态的 MCP 配置文件位置可能不同，以上 JSON 表达的是需要配置的核心信息：server 名称、transport、命令或 URL、认证请求头。

## Claude 接入示例

Claude Desktop 常见方式是配置 stdio MCP server。可以把 bridge 配到 `mcpServers`：

```json
{
  "mcpServers": {
    "legacy-mcp-demo": {
      "command": "java",
      "args": [
        "-jar",
        "C:\\path\\to\\legacy-mcp-java8-starter\\legacy-mcp-stdio-bridge\\target\\legacy-mcp-stdio-bridge-0.1.0.jar",
        "--url",
        "http://localhost:8080/mcp",
        "--api-key",
        "claude-dev-key",
        "--api-key-header",
        "X-MCP-API-Key"
      ]
    }
  }
}
```

业务项目中对应配置：

```yaml
legacy:
  mcp:
    agents:
      claude:
        api-key: ${MCP_CLAUDE_API_KEY:claude-dev-key}
        allowed-tools:
          - demo.echo
```

## Hermes / WorkBuddy 接入示例

如果平台支持 HTTP 工具或 HTTP MCP server，优先配置 HTTP：

```text
POST http://localhost:8080/mcp
Content-Type: application/json
X-MCP-API-Key: hermes-dev-key
```

请求体仍然是标准 JSON-RPC：

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/list",
  "params": {}
}
```

如果平台只支持本地进程型 MCP server，则按 stdio bridge 接入，并给每个平台配置独立 API key：

```yaml
legacy:
  mcp:
    agents:
      hermes:
        api-key: ${MCP_HERMES_API_KEY:hermes-dev-key}
        allowed-tools:
          - demo.echo
      workbuddy:
        api-key: ${MCP_WORKBUDDY_API_KEY:workbuddy-dev-key}
        allowed-tools:
          - demo.echo
```

## 写工具接入

写工具需要三层同时允许：

1. 工具代码返回 `writeOperation() == true`。
2. 全局配置开启 `legacy.mcp.security.write-tools-enabled=true`。
3. 当前 agent 的 `write-allowed-tools` 包含该工具名。

示例：

```yaml
legacy:
  mcp:
    security:
      write-tools-enabled: true
    agents:
      codex:
        api-key: ${MCP_CODEX_API_KEY:dev-key}
        allowed-tools:
          - demo.echo
          - demo.notice.publish
        write-allowed-tools:
          - demo.notice.publish
```

建议生产环境只给明确需要写能力的 agent 开启写工具，并在业务侧记录操作人、参数摘要、业务 ID 和执行结果。

## 联调顺序

1. 启动业务项目，确认 `/mcp` endpoint 已启用。
2. 使用 HTTP 命令调用 `tools/list`。
3. 使用 HTTP 命令调用一个只读工具。
4. 如果 agent 走 stdio，单独启动 bridge，确认它能转发请求。
5. 配置 agent。
6. 在 agent 中刷新或重启 MCP server。
7. 让 agent 列出工具。
8. 先调用只读工具，再验证写工具。

## 常见问题

### `tools/list` 返回 401

通常是 API key 缺失或不匹配。检查：

- 请求头名称是否与 `legacy.mcp.security.auth-header` 一致。
- agent 配置中的 API key 是否与 `legacy.mcp.agents.<agent>.api-key` 一致。
- 如果使用 `Authorization: Bearer <key>`，确认当前版本允许 bearer token。

### `tools/call` 返回工具不存在

检查：

- 工具是否注册为 Spring Bean。
- 工具名称是否与请求中的 `params.name` 完全一致。
- 当前 agent 的 `allowed-tools` 是否包含该工具。

### 写工具被拒绝

检查三层条件：

- 工具是否声明 `writeOperation() == true`。
- 是否配置 `legacy.mcp.security.write-tools-enabled=true`。
- 当前 agent 的 `write-allowed-tools` 是否包含该工具。

### stdio agent 没有任何响应

检查：

- bridge jar 路径是否正确。
- Java 命令是否可用。
- `--url` 是否能从 agent 所在机器访问。
- bridge 是否把诊断日志写到了 stdout。正确行为是 stdout 只写 JSON-RPC 响应。

### 中文内容乱码

Windows PowerShell 默认编码可能导致显示乱码。建议：

- 请求体用 UTF-8 字节发送。
- Java 应用和日志统一使用 UTF-8。
- agent 配置文件保存为 UTF-8。
