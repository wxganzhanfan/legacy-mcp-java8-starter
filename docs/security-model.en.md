# Security Model

[简体中文](security-model.md)

The Spring Boot integration module does not expose MCP capabilities by default.

Each agent should use its own API key and tool allowlist. Tools must be registered explicitly. This project does not automatically expose methods from arbitrary Controllers, Services, or other application classes.

Default policy:

- `legacy.mcp.enabled=false`
- The default API key header is `X-MCP-API-Key`.
- The WebMVC resolver also accepts `Authorization: Bearer <key>`.
- Write tools are globally disabled by default unless `legacy.mcp.security.write-tools-enabled=true` is configured.
- Even when writes are globally enabled, each agent must still list allowed write tools under `write-allowed-tools`.
- Each tool call creates an `McpAuditEvent`.
- Audit events mask sensitive argument fields such as `password`, `secret`, `token`, `apiKey`, and `authorization`.

Spring Security notes:

If the host application enables Spring Security, the recommended setup is to permit the MCP endpoint in the Spring Security filter chain and let this starter protect the endpoint through its MCP API-key mechanism. See `samples/boot2-security-sample`.
