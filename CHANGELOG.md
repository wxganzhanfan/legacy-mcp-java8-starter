# 更新日志

[English](CHANGELOG.en.md)

## 0.1.0 - 2026-06-20

- 增加 Java 8 MCP core，支持 `initialize`、`ping`、`tools/list` 和 `tools/call`。
- 增加显式接口和注解式工具开发 API。
- 增加 Spring MVC 传输层，以及通过 `spring.factories` 提供的 Boot2 自动配置。
- 增加 API key agent 配置、工具白名单、写工具策略、审计事件和敏感字段脱敏。
- 增加 stdio-to-HTTP bridge，用于适配只支持 stdio 的 MCP 客户端。
- 增加 Boot2 简单示例和 Spring Security 示例。
- 初始化 Java 8 基线项目骨架。
- 初始化 MCP core dispatcher 和工具注册 API。
