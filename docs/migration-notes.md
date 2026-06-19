# Boot2 项目迁移说明

[English](migration-notes.en.md)

1. 添加 `legacy-mcp-spring-boot2-starter` 依赖。
2. 只把确实需要被 agent 调用的业务操作注册为 `LegacyMcpTool` Bean。
3. 默认配置中保持 MCP 关闭，只在需要的部署环境中开启。
4. 为每个 agent 配置独立 API key 和工具白名单。
5. 对会修改业务数据的操作实现 `writeOperation() == true`。
6. 只有在业务流程确认允许 agent 触发写操作后，才开启全局写权限。
7. 如果项目启用了 Spring Security，在安全过滤链中放行 MCP endpoint，并由 MCP API key 保护该 endpoint。
8. 增加集成测试，分别覆盖有效 API key 和无效 API key 调用 `/mcp` 的场景。

这个 starter 的主要目标仍然是 Java 8 + Spring Boot 2 旧项目。WebMVC 传输层不在主代码中直接依赖 Servlet API，并同时提供 Boot2 `spring.factories` 与 Boot3/Boot4 `AutoConfiguration.imports` 自动配置索引，因此在没有直接使用 `javax.servlet`/`jakarta.servlet` 类型的场景下，可以在更高版本 Boot 项目中做兼容性验证。若新项目需要深度使用 Jakarta Servlet API，建议单独增加 Boot3/Boot4 专用适配模块并补充集成测试。
