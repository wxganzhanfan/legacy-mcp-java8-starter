package io.github.legacy_mcp.core;

public final class McpPrincipal {
    private final String agentId;

    public McpPrincipal(String agentId) {
        this.agentId = agentId == null ? "anonymous" : agentId;
    }

    public String getAgentId() {
        return agentId;
    }
}
