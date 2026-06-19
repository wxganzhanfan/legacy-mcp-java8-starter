# 第三方开源声明

[English](THIRD-PARTY-NOTICES.en.md)

本文档列出本项目主产物直接使用或可能随二进制一起分发的第三方开源组件。完整、实时的依赖版本以各模块 `pom.xml` 和 Maven dependency tree 为准。

## 运行时依赖

| 组件 | 用途 | 许可证 |
| --- | --- | --- |
| Jackson Databind / Core / Annotations | JSON 序列化、反序列化和 JSON-RPC 消息处理 | Apache License 2.0 |
| Spring Framework / Spring Web MVC | HTTP endpoint 和 Spring MVC 集成 | Apache License 2.0 |
| Spring Boot Autoconfigure | Spring Boot 2 自动配置支持 | Apache License 2.0 |

## shaded jar 说明

`legacy-mcp-stdio-bridge` 会生成一个可执行 shaded jar。该 jar 会包含本项目代码以及 Jackson 相关 class，用于在仅支持 stdio 的 MCP agent 中转发 HTTP MCP 请求。

发布 shaded jar 时需要保留本项目 `LICENSE`、`NOTICE` 和本文档。Maven Shade 插件已配置 Apache LICENSE / NOTICE 合并处理，但发布前仍建议检查最终 jar 中的 `META-INF/LICENSE` 和 `META-INF/NOTICE`。

## 测试与示例依赖

JUnit、AssertJ、Spring Boot Test、Spring Security、Logback 等主要用于测试或示例模块，不属于核心库的最小运行时分发范围。若单独发布 sample 应用或二进制包，请按实际打包内容重新生成第三方声明。
