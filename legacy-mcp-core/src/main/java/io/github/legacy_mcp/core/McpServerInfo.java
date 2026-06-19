package io.github.legacy_mcp.core;

public final class McpServerInfo {
    private final String name;
    private final String version;

    public McpServerInfo(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
}

