# Protocol Scope

[简体中文](protocol-scope.md)

The first release only supports MCP tool capabilities and covers the following methods:

- `initialize`
- `notifications/initialized`
- `ping`
- `tools/list`
- `tools/call`

The first release does not implement resources, prompts, sampling, elicitation, OAuth, or streaming responses.

HTTP transport behavior:

- `POST /mcp` accepts JSON-RPC requests.
- JSON-RPC responses return HTTP 200.
- `notifications/initialized` returns HTTP 202 with an empty response body.
- `GET /mcp` returns HTTP 405.
- Request body size is limited by `legacy.mcp.limits.max-request-bytes`.

Tool call results are returned as MCP text content:

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
