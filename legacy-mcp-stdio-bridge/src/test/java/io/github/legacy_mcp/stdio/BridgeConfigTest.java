package io.github.legacy_mcp.stdio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BridgeConfigTest {

    @Test
    void parsesUrlApiKeyAndHeaderName() {
        BridgeConfig config = BridgeConfig.parse(new String[]{
                "--url", "http://localhost:8080/mcp",
                "--api-key", "dev-key",
                "--api-key-header", "X-MCP-API-Key"
        });

        assertEquals("http://localhost:8080/mcp", config.getUrl());
        assertEquals("dev-key", config.getApiKey());
        assertEquals("X-MCP-API-Key", config.getApiKeyHeader());
    }
}
