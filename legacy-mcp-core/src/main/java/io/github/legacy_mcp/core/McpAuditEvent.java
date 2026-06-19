package io.github.legacy_mcp.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class McpAuditEvent {
    private final String agentId;
    private final String toolName;
    private final boolean success;
    private final String errorMessage;
    private final Map<String, Object> arguments;
    private final long startedAtEpochMs;
    private final long endedAtEpochMs;

    public McpAuditEvent(String agentId,
                         String toolName,
                         boolean success,
                         String errorMessage,
                         Map<String, Object> arguments,
                         long startedAtEpochMs,
                         long endedAtEpochMs) {
        this.agentId = agentId;
        this.toolName = toolName;
        this.success = success;
        this.errorMessage = errorMessage;
        this.arguments = arguments == null
                ? Collections.<String, Object>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<String, Object>(arguments));
        this.startedAtEpochMs = startedAtEpochMs;
        this.endedAtEpochMs = endedAtEpochMs;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getToolName() {
        return toolName;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public long getStartedAtEpochMs() {
        return startedAtEpochMs;
    }

    public long getEndedAtEpochMs() {
        return endedAtEpochMs;
    }
}
