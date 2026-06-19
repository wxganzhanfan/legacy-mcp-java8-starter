# Agent Integration Guide

[简体中文](agent-integration-guide.md)

This document explains how Codex, Claude, Hermes, WorkBuddy, or other MCP agents can connect to an MCP service built with this project.

Agent and platform names in this document are used only to describe integration modes. This project is not an official project of those products or vendors, and it does not imply official support, certification, or endorsement.

## Prerequisites

The business application must first complete MCP server integration and expose a working HTTP endpoint:

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

Recommended per-agent isolation:

- independent `api-key`
- independent `allowed-tools`
- independent `write-allowed-tools`
- independent audit identity, such as `codex`, `claude`, `hermes`, or `workbuddy`

This makes audit logs easier to read and prevents any single agent from receiving broader tool permissions than it needs.

## Choosing an Integration Mode

| Agent capability | Recommended integration | Notes |
| --- | --- | --- |
| Supports HTTP MCP server | Connect directly to the business application's `/mcp` endpoint | Simple configuration with one fewer process. |
| Supports only stdio MCP server | Use `legacy-mcp-stdio-bridge` | The agent starts the bridge, and the bridge forwards requests to the HTTP endpoint. |
| Does not support standard MCP but supports custom HTTP tools | Call the HTTP JSON-RPC endpoint | The agent platform must allow custom headers and JSON request bodies. |

The server built by this project exposes an HTTP JSON-RPC endpoint. The stdio bridge exists only to support agents that can launch local stdio MCP servers.

## Mode 1: Direct HTTP Integration

If the agent supports remote HTTP MCP servers, configure it directly:

```text
URL: http://localhost:8080/mcp
Header: X-MCP-API-Key: dev-key
Transport: HTTP JSON-RPC
```

Verify the server first:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/mcp `
  -Headers @{ 'X-MCP-API-Key' = 'dev-key' } `
  -ContentType 'application/json' `
  -Body '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}'
```

If the response contains `result.tools`, the agent can continue with integration.

## Mode 2: Stdio Bridge Integration

If the agent only supports stdio MCP servers, build the bridge first:

```powershell
mvn -pl legacy-mcp-stdio-bridge -am package -DskipTests
```

Then configure the agent to launch this jar:

```powershell
java -jar <repo>\legacy-mcp-stdio-bridge\target\legacy-mcp-stdio-bridge-0.1.0.jar `
  --url http://localhost:8080/mcp `
  --api-key dev-key `
  --api-key-header X-MCP-API-Key `
  --timeout-ms 30000
```

Parameters:

| Parameter | Required | Default | Description |
| --- | --- | --- | --- |
| `--url` | Yes | None | Business application MCP HTTP endpoint, for example `http://localhost:8080/mcp`. |
| `--api-key` | No | None | Agent API key sent to the business application. |
| `--api-key-header` | No | `X-MCP-API-Key` | API-key header name. It must match `legacy.mcp.security.auth-header`. |
| `--timeout-ms` | No | `30000` | HTTP forwarding timeout. |

Bridge behavior:

- stdin: reads one JSON-RPC request per line.
- stdout: writes only JSON-RPC responses.
- stderr: writes diagnostics.

Do not write debug logs to stdout, because that would corrupt the MCP protocol message stream.

## Codex Integration Example

If the Codex environment supports HTTP MCP servers, prefer direct HTTP integration:

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

If the Codex environment only supports stdio MCP servers, use the bridge:

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

MCP configuration locations vary across Codex versions and deployment forms. The JSON above shows the essential values: server name, transport, command or URL, and authentication header.

## Claude Integration Example

Claude Desktop commonly uses stdio MCP servers. Configure the bridge under `mcpServers`:

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

Corresponding business application configuration:

```yaml
legacy:
  mcp:
    agents:
      claude:
        api-key: ${MCP_CLAUDE_API_KEY:claude-dev-key}
        allowed-tools:
          - demo.echo
```

## Hermes / WorkBuddy Integration Example

If the platform supports HTTP tools or HTTP MCP servers, prefer HTTP:

```text
POST http://localhost:8080/mcp
Content-Type: application/json
X-MCP-API-Key: hermes-dev-key
```

The request body remains standard JSON-RPC:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/list",
  "params": {}
}
```

If the platform only supports local process-based MCP servers, use the stdio bridge and configure a separate API key for each platform:

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

## Write Tool Integration

Write tools require all three layers to allow the call:

1. The tool code returns `writeOperation() == true`.
2. Global configuration enables `legacy.mcp.security.write-tools-enabled=true`.
3. The current agent's `write-allowed-tools` contains the tool name.

Example:

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

In production, grant write tools only to agents that explicitly need them. The business side should record operator identity, argument summaries, business IDs, and execution results.

## Integration Checklist

1. Start the business application and confirm the `/mcp` endpoint is enabled.
2. Call `tools/list` with an HTTP command.
3. Call one read-only tool with an HTTP command.
4. If the agent uses stdio, start the bridge separately and confirm it forwards requests.
5. Configure the agent.
6. Refresh or restart the MCP server in the agent.
7. Ask the agent to list tools.
8. Call a read-only tool first, then validate write tools.

## Troubleshooting

### `tools/list` returns 401

This usually means the API key is missing or mismatched. Check:

- Whether the request header name matches `legacy.mcp.security.auth-header`.
- Whether the API key in the agent configuration matches `legacy.mcp.agents.<agent>.api-key`.
- If using `Authorization: Bearer <key>`, confirm the current version accepts bearer tokens.

### `tools/call` says the tool does not exist

Check:

- Whether the tool is registered as a Spring Bean.
- Whether the tool name exactly matches `params.name`.
- Whether the current agent's `allowed-tools` contains the tool.

### Write tool is rejected

Check all three conditions:

- The tool declares `writeOperation() == true`.
- `legacy.mcp.security.write-tools-enabled=true` is configured.
- The current agent's `write-allowed-tools` contains the tool.

### Stdio agent has no response

Check:

- Whether the bridge jar path is correct.
- Whether the `java` command is available.
- Whether `--url` is reachable from the agent machine.
- Whether the bridge writes diagnostics to stdout. Correct behavior is stdout containing only JSON-RPC responses.

### Chinese text is garbled

Windows PowerShell default encoding can cause garbled display. Recommendations:

- Send request bodies as UTF-8 bytes.
- Use UTF-8 consistently for the Java application and logs.
- Save agent configuration files as UTF-8.
