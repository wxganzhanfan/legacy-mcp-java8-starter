package io.github.legacy_mcp.core;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoggingMcpAuditPublisher implements McpAuditPublisher {
    private static final Logger LOGGER = Logger.getLogger(LoggingMcpAuditPublisher.class.getName());

    public void publish(McpAuditEvent event) {
        Level level = event.isSuccess() ? Level.INFO : Level.WARNING;
        LOGGER.log(level,
                "mcp tool call agent={0} tool={1} success={2} error={3}",
                new Object[]{event.getAgentId(), event.getToolName(), event.isSuccess(), event.getErrorMessage()});
    }
}
