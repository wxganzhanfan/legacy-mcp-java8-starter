# 安全政策

[English](SECURITY.en.md)

本项目会把应用功能暴露给 agent 客户端。请把每一个工具都视为一个 API 暴露面。

生产环境必须具备以下控制措施：

- 除非某个环境明确需要 MCP，否则保持 `legacy.mcp.enabled=false`。
- 每个 agent profile 使用独立 API key。
- 通过宿主应用原有的密钥管理流程轮换 API key。
- 工具白名单保持最小权限。
- 除非业务流程已经批准 agent 触发变更，否则保持写工具禁用。
- 对每个启用 MCP 的环境审查审计事件。

安全问题请在公开披露前，通过仓库所属方或公司安全流程报告。
