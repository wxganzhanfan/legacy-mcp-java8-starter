package io.github.legacy_mcp.webmvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class McpAgentRegistration {
    private final String agentId;
    private final String apiKey;
    private final List<String> allowedTools;
    private final List<String> writeAllowedTools;

    public McpAgentRegistration(String agentId, String apiKey, List<String> allowedTools) {
        this(agentId, apiKey, allowedTools, Collections.<String>emptyList());
    }

    public McpAgentRegistration(String agentId, String apiKey, List<String> allowedTools, List<String> writeAllowedTools) {
        this.agentId = agentId;
        this.apiKey = apiKey;
        this.allowedTools = allowedTools == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<String>(allowedTools));
        this.writeAllowedTools = writeAllowedTools == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<String>(writeAllowedTools));
    }

    public String getAgentId() {
        return agentId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public List<String> getAllowedTools() {
        return allowedTools;
    }

    public List<String> getWriteAllowedTools() {
        return writeAllowedTools;
    }
}
