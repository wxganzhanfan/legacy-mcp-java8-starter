package io.github.legacy_mcp.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class McpDispatcher {
    private static final int METHOD_NOT_FOUND = -32601;
    private static final int INVALID_PARAMS = -32602;

    private final McpServerInfo serverInfo;
    private final McpToolRegistry toolRegistry;
    private final McpAuditPublisher auditPublisher;
    private final McpPrincipalInstaller principalInstaller;
    private final SensitiveFieldMasker sensitiveFieldMasker;
    private final boolean writeToolsEnabled;

    public McpDispatcher(McpServerInfo serverInfo, McpToolRegistry toolRegistry) {
        this(serverInfo, toolRegistry, new LoggingMcpAuditPublisher(), new NoopMcpPrincipalInstaller(), false);
    }

    public McpDispatcher(McpServerInfo serverInfo,
                         McpToolRegistry toolRegistry,
                         McpAuditPublisher auditPublisher,
                         McpPrincipalInstaller principalInstaller,
                         boolean writeToolsEnabled) {
        this.serverInfo = serverInfo;
        this.toolRegistry = toolRegistry;
        this.auditPublisher = auditPublisher == null ? new LoggingMcpAuditPublisher() : auditPublisher;
        this.principalInstaller = principalInstaller == null ? new NoopMcpPrincipalInstaller() : principalInstaller;
        this.sensitiveFieldMasker = new SensitiveFieldMasker();
        this.writeToolsEnabled = writeToolsEnabled;
    }

    public JsonRpcResponse dispatch(JsonRpcRequest request, McpAgentContext context) {
        if ("notifications/initialized".equals(request.getMethod()) && request.isNotification()) {
            return null;
        }
        if ("initialize".equals(request.getMethod())) {
            return JsonRpcResponse.result(request.getId(), initializeResult());
        }
        if ("ping".equals(request.getMethod())) {
            return JsonRpcResponse.result(request.getId(), Collections.emptyMap());
        }
        if ("tools/list".equals(request.getMethod())) {
            return JsonRpcResponse.result(request.getId(), toolsListResult(context));
        }
        if ("tools/call".equals(request.getMethod())) {
            return JsonRpcResponse.result(request.getId(), callTool(request.getParams(), context));
        }
        return JsonRpcResponse.error(request.getId(), METHOD_NOT_FOUND, "Method not found: " + request.getMethod());
    }

    private Map<String, Object> initializeResult() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("protocolVersion", McpProtocolVersion.DEFAULT);

        Map<String, Object> capabilities = new LinkedHashMap<String, Object>();
        capabilities.put("tools", Collections.emptyMap());
        result.put("capabilities", capabilities);

        Map<String, Object> server = new LinkedHashMap<String, Object>();
        server.put("name", serverInfo.getName());
        server.put("version", serverInfo.getVersion());
        result.put("serverInfo", server);
        return result;
    }

    private Map<String, Object> toolsListResult(McpAgentContext context) {
        List<Map<String, Object>> tools = new ArrayList<Map<String, Object>>();
        for (LegacyMcpTool tool : toolRegistry.listAllowed(context)) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("name", tool.name());
            item.put("title", tool.title());
            item.put("description", tool.description());
            item.put("inputSchema", tool.inputSchema().toMap());
            tools.add(item);
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("tools", tools);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callTool(Map<String, Object> params, McpAgentContext context) {
        Object rawName = params.get("name");
        if (!(rawName instanceof String)) {
            return errorToolResult("Missing tool name");
        }
        String name = (String) rawName;
        if (context != null && !context.canUseTool(name)) {
            return errorToolResult("Tool not allowed for agent " + context.getAgentId() + ": " + name);
        }
        LegacyMcpTool tool = toolRegistry.get(name);
        if (tool == null) {
            Map<String, Object> arguments = Collections.<String, Object>emptyMap();
            publishAudit(context, name, false, "Unknown MCP tool: " + name, arguments, System.currentTimeMillis());
            return errorToolResult("Unknown MCP tool: " + name);
        }
        Object rawArguments = params.get("arguments");
        Map<String, Object> arguments = rawArguments instanceof Map
                ? (Map<String, Object>) rawArguments
                : Collections.<String, Object>emptyMap();
        if (tool.writeOperation() && !writeToolsEnabled) {
            String message = "Write tool is disabled by server policy: " + name;
            publishAudit(context, name, false, message, arguments, System.currentTimeMillis());
            return errorToolResult(message);
        }
        if (tool.writeOperation() && context != null && !context.canWriteTool(name)) {
            String message = "Agent is not allowed to write with tool " + name + ": " + context.getAgentId();
            publishAudit(context, name, false, message, arguments, System.currentTimeMillis());
            return errorToolResult(message);
        }
        long startedAt = System.currentTimeMillis();
        McpAgentContext effectiveContext = context == null ? McpAgentContext.anonymous() : context;
        try {
            principalInstaller.install(new McpPrincipal(effectiveContext.getAgentId()));
            Object value = tool.call(arguments, effectiveContext);
            publishAudit(effectiveContext, name, true, null, arguments, startedAt);
            return textToolResult(String.valueOf(value), false);
        } catch (RuntimeException ex) {
            publishAudit(effectiveContext, name, false, ex.getMessage(), arguments, startedAt);
            return errorToolResult(ex.getMessage());
        } finally {
            principalInstaller.clear();
        }
    }

    private void publishAudit(McpAgentContext context,
                              String toolName,
                              boolean success,
                              String errorMessage,
                              Map<String, Object> arguments,
                              long startedAt) {
        McpAgentContext effectiveContext = context == null ? McpAgentContext.anonymous() : context;
        auditPublisher.publish(new McpAuditEvent(
                effectiveContext.getAgentId(),
                toolName,
                success,
                errorMessage,
                sensitiveFieldMasker.mask(arguments),
                startedAt,
                System.currentTimeMillis()
        ));
    }

    private Map<String, Object> errorToolResult(String message) {
        return textToolResult(message, true);
    }

    private Map<String, Object> textToolResult(String text, boolean error) {
        Map<String, Object> textContent = new LinkedHashMap<String, Object>();
        textContent.put("type", "text");
        textContent.put("text", text);

        List<Map<String, Object>> content = new ArrayList<Map<String, Object>>();
        content.add(textContent);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("content", content);
        result.put("isError", error);
        return result;
    }
}
