package io.github.legacy_mcp.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "legacy.mcp")
public class LegacyMcpProperties {
    private boolean enabled = false;
    private String endpoint = "/mcp";
    private String serverName = "legacy-mcp-server";
    private String serverVersion = "0.1.0";
    private Security security = new Security();
    private Limits limits = new Limits();
    private Map<String, Agent> agents = new LinkedHashMap<String, Agent>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public Limits getLimits() {
        return limits;
    }

    public void setLimits(Limits limits) {
        this.limits = limits;
    }

    public Map<String, Agent> getAgents() {
        return agents;
    }

    public void setAgents(Map<String, Agent> agents) {
        this.agents = agents;
    }

    public static class Security {
        private String authHeader = "X-MCP-API-Key";
        private boolean allowBearerToken = true;
        private boolean originCheckEnabled = true;
        private boolean writeToolsEnabled = false;
        private List<String> allowedOrigins = new ArrayList<String>();

        public String getAuthHeader() {
            return authHeader;
        }

        public void setAuthHeader(String authHeader) {
            this.authHeader = authHeader;
        }

        public boolean isAllowBearerToken() {
            return allowBearerToken;
        }

        public void setAllowBearerToken(boolean allowBearerToken) {
            this.allowBearerToken = allowBearerToken;
        }

        public boolean isOriginCheckEnabled() {
            return originCheckEnabled;
        }

        public void setOriginCheckEnabled(boolean originCheckEnabled) {
            this.originCheckEnabled = originCheckEnabled;
        }

        public boolean isWriteToolsEnabled() {
            return writeToolsEnabled;
        }

        public void setWriteToolsEnabled(boolean writeToolsEnabled) {
            this.writeToolsEnabled = writeToolsEnabled;
        }

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Limits {
        private int maxRequestBytes = 65536;
        private int maxResponseBytes = 262144;
        private long toolTimeoutMs = 30000;

        public int getMaxRequestBytes() {
            return maxRequestBytes;
        }

        public void setMaxRequestBytes(int maxRequestBytes) {
            this.maxRequestBytes = maxRequestBytes;
        }

        public int getMaxResponseBytes() {
            return maxResponseBytes;
        }

        public void setMaxResponseBytes(int maxResponseBytes) {
            this.maxResponseBytes = maxResponseBytes;
        }

        public long getToolTimeoutMs() {
            return toolTimeoutMs;
        }

        public void setToolTimeoutMs(long toolTimeoutMs) {
            this.toolTimeoutMs = toolTimeoutMs;
        }
    }

    public static class Agent {
        private String apiKey;
        private List<String> allowedTools = new ArrayList<String>();
        private List<String> writeAllowedTools = new ArrayList<String>();

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public List<String> getAllowedTools() {
            return allowedTools;
        }

        public void setAllowedTools(List<String> allowedTools) {
            this.allowedTools = allowedTools;
        }

        public List<String> getWriteAllowedTools() {
            return writeAllowedTools;
        }

        public void setWriteAllowedTools(List<String> writeAllowedTools) {
            this.writeAllowedTools = writeAllowedTools;
        }
    }
}
