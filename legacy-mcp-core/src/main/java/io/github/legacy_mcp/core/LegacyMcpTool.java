package io.github.legacy_mcp.core;

import java.util.Map;

public interface LegacyMcpTool {
    String name();

    String title();

    String description();

    McpJsonSchema inputSchema();

    default boolean writeOperation() {
        return false;
    }

    Object call(Map<String, Object> arguments, McpAgentContext context);
}
