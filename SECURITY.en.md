# Security Policy

[简体中文](SECURITY.md)

This project exposes application functionality to agent clients. Treat every tool as an API surface.

Required production controls:

- Keep `legacy.mcp.enabled=false` unless an environment explicitly needs MCP.
- Use one API key per agent profile.
- Rotate API keys through the host application's normal secret-management path.
- Keep tool allowlists minimal.
- Keep write tools disabled unless the business workflow is approved for agent-triggered changes.
- Review audit events for every enabled environment.

Report security issues through the owning repository or company security process before public disclosure.
