package io.github.legacy_mcp.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class McpToolRegistry {
    private static final String NAME_PATTERN = "[a-zA-Z0-9_.-]+";
    private final Map<String, LegacyMcpTool> tools = new LinkedHashMap<String, LegacyMcpTool>();

    public void register(LegacyMcpTool tool) {
        if (tool == null) {
            throw new IllegalArgumentException("MCP tool must not be null");
        }
        String name = tool.name();
        if (name == null || !name.matches(NAME_PATTERN)) {
            throw new IllegalArgumentException("MCP tool name must match " + NAME_PATTERN + ": " + name);
        }
        if (tools.containsKey(name)) {
            throw new IllegalArgumentException("MCP tool already registered: " + name);
        }
        tools.put(name, tool);
    }

    public LegacyMcpTool get(String name) {
        return tools.get(name);
    }

    public List<LegacyMcpTool> listAllowed(McpAgentContext context) {
        Collection<LegacyMcpTool> values = tools.values();
        List<LegacyMcpTool> allowed = new ArrayList<LegacyMcpTool>();
        for (LegacyMcpTool tool : values) {
            if (context == null || context.canUseTool(tool.name())) {
                allowed.add(tool);
            }
        }
        return allowed;
    }
}

