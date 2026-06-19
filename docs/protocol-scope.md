# 协议范围

[English](protocol-scope.en.md)

首个版本只支持 MCP 的工具能力，覆盖以下方法：

- `initialize`
- `notifications/initialized`
- `ping`
- `tools/list`
- `tools/call`

首个版本暂不实现 resources、prompts、sampling、elicitation、OAuth 或流式响应。

HTTP 传输行为：

- `POST /mcp` 接收 JSON-RPC 请求。
- JSON-RPC 响应返回 HTTP 200。
- `notifications/initialized` 返回 HTTP 202，响应体为空。
- `GET /mcp` 返回 HTTP 405。
- 请求体大小由 `legacy.mcp.limits.max-request-bytes` 限制。

工具调用结果以 MCP 文本内容形式返回：

```json
{
  "content": [
    {
      "type": "text",
      "text": "result"
    }
  ],
  "isError": false
}
```
