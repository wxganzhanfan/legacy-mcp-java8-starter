package io.github.legacy_mcp.webmvc;

public final class McpHttpProperties {
    private final String endpoint;
    private final int maxRequestBytes;

    public McpHttpProperties(String endpoint, int maxRequestBytes) {
        this.endpoint = endpoint == null ? "/mcp" : endpoint;
        this.maxRequestBytes = maxRequestBytes <= 0 ? 65536 : maxRequestBytes;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public int getMaxRequestBytes() {
        return maxRequestBytes;
    }
}

