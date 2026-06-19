# 安全模型

[English](security-model.en.md)

Spring Boot 集成模块默认不暴露 MCP 能力。

每个 agent 都应该使用独立的 API key 和工具白名单。工具必须显式注册，本项目不会从任意 Controller 或 Service 中自动暴露方法。

默认策略：

- `legacy.mcp.enabled=false`
- API key 请求头默认为 `X-MCP-API-Key`。
- WebMVC 解析器同时接受 `Authorization: Bearer <key>`。
- 写工具默认全局禁用，除非配置 `legacy.mcp.security.write-tools-enabled=true`。
- 即使全局允许写操作，每个 agent 仍必须在 `write-allowed-tools` 中单独声明允许调用的写工具。
- 每次工具调用都会产生一条 `McpAuditEvent` 审计事件。
- 审计事件会对 `password`、`secret`、`token`、`apiKey`、`authorization` 等敏感参数字段做脱敏处理。

Spring Security 说明：

如果宿主应用启用了 Spring Security，建议在 Spring Security 层放行 MCP endpoint，然后交由本 starter 的 MCP API key 机制保护该 endpoint。参考 `samples/boot2-security-sample`。
