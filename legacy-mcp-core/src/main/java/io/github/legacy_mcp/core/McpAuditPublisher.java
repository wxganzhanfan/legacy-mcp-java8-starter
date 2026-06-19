package io.github.legacy_mcp.core;

public interface McpAuditPublisher {
    void publish(McpAuditEvent event);
}
