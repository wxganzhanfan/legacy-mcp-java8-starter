package io.github.legacy_mcp.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class McpAgentContext {
    private final String agentId;
    private final List<String> allowedTools;
    private final List<String> writeAllowedTools;

    public McpAgentContext(String agentId, List<String> allowedTools) {
        this(agentId, allowedTools, Collections.<String>emptyList());
    }

    public McpAgentContext(String agentId, List<String> allowedTools, List<String> writeAllowedTools) {
        this.agentId = agentId == null ? "anonymous" : agentId;
        this.allowedTools = allowedTools == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<String>(allowedTools));
        this.writeAllowedTools = writeAllowedTools == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<String>(writeAllowedTools));
    }

    public static McpAgentContext anonymous() {
        return new McpAgentContext("anonymous", Collections.<String>emptyList());
    }

    public String getAgentId() {
        return agentId;
    }

    public boolean canUseTool(String toolName) {
        return allowedTools.isEmpty() || allowedTools.contains(toolName);
    }

    public boolean canWriteTool(String toolName) {
        return writeAllowedTools.contains(toolName);
    }
}
