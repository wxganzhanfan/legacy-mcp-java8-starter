# 贡献指南

[English](CONTRIBUTING.en.md)

感谢你考虑参与本项目。这个项目的目标是为 Java 8 与 Spring Boot 2 存量系统提供尽量小、清楚、可审计的 MCP 接入层。

## 提交前检查

提交 issue 或 pull request 前，请尽量确认：

- 问题或需求属于 Java 8 / Spring MVC / Spring Boot 2 存量项目接入 MCP 的范围。
- 新增协议能力时同步更新 `docs/protocol-scope.md` 和 `docs/protocol-scope.en.md`。
- 新增工具开发能力时同步更新 `docs/tool-authoring-guide.md` 和 `docs/tool-authoring-guide.en.md`。
- 新增 agent 接入方式时同步更新 `docs/agent-integration-guide.md` 和 `docs/agent-integration-guide.en.md`。
- 代码保持 Java 8 兼容，不引入 Java 9+ API、Reactor、WebFlux 或 Jakarta-only API。

## 本地验证

推荐在提交前运行：

```powershell
mvn clean verify
```

如果你需要验证 Java 8 字节码兼容性，可以设置 `JAVA8_HOME` 后运行：

```powershell
$env:JAVA8_HOME = '<path-to-jdk8>'
powershell -ExecutionPolicy Bypass -File scripts\verify-java8.ps1
```

## 依赖与许可证

新增依赖前请说明：

- 为什么需要新增依赖。
- 是否可以继续保持 Java 8 兼容。
- 许可证是否允许 Apache-2.0 项目使用和再分发。
- shaded jar 是否需要更新 `THIRD-PARTY-NOTICES.md`。

不要提交公司内部代码、真实业务数据、密钥、令牌、Cookie、私有仓库地址或无法公开的文档片段。
