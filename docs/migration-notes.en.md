# Boot2 Project Migration Notes

[简体中文](migration-notes.md)

1. Add the `legacy-mcp-spring-boot2-starter` dependency.
2. Register only the business operations that agents actually need to call as `LegacyMcpTool` Beans.
3. Keep MCP disabled by default and enable it only in deployment environments where it is needed.
4. Configure an independent API key and tool allowlist for each agent.
5. Implement `writeOperation() == true` for operations that modify business data.
6. Enable global write permission only after the business workflow explicitly allows agents to trigger write operations.
7. If the project uses Spring Security, permit the MCP endpoint in the security filter chain and let the MCP API-key mechanism protect that endpoint.
8. Add integration tests covering both valid and invalid API-key calls to `/mcp`.

The starter's primary target is still Java 8 + Spring Boot 2 legacy projects. The WebMVC transport does not directly depend on Servlet API in main code, and it provides both Boot2 `spring.factories` and Boot3/Boot4 `AutoConfiguration.imports` metadata. Therefore, compatibility validation is possible in newer Boot projects when the integration does not directly use `javax.servlet` or `jakarta.servlet` types. If a newer project needs deep Jakarta Servlet API integration, add a dedicated Boot3/Boot4 adapter module and integration tests.
