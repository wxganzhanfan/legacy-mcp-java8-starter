package io.github.legacy_mcp.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class McpToolRegistryTest {

    @Test
    void duplicateToolNamesAreRejected() {
        McpToolRegistry registry = new McpToolRegistry();
        registry.register(new NoopTool("demo.echo"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, new Executable() {
            public void execute() {
                registry.register(new NoopTool("demo.echo"));
            }
        });

        assertEquals("MCP tool already registered: demo.echo", error.getMessage());
    }

    @Test
    void invalidToolNamesAreRejected() {
        McpToolRegistry registry = new McpToolRegistry();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, new Executable() {
            public void execute() {
                registry.register(new NoopTool("bad name"));
            }
        });

        assertEquals("MCP tool name must match [a-zA-Z0-9_.-]+: bad name", error.getMessage());
    }

    private static final class NoopTool implements LegacyMcpTool {
        private final String name;

        private NoopTool(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public String title() {
            return "Noop";
        }

        public String description() {
            return "No operation.";
        }

        public McpJsonSchema inputSchema() {
            return McpJsonSchema.object();
        }

        public Object call(Map<String, Object> arguments, McpAgentContext context) {
            return Collections.emptyMap();
        }
    }
}
