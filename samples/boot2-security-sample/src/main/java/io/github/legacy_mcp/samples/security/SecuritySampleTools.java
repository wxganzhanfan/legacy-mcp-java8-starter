package io.github.legacy_mcp.samples.security;

import io.github.legacy_mcp.core.LegacyMcpTool;
import io.github.legacy_mcp.core.McpAgentContext;
import io.github.legacy_mcp.core.McpJsonSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SecuritySampleTools {
    @Bean
    public LegacyMcpTool echoTool() {
        return new LegacyMcpTool() {
            public String name() {
                return "demo.echo";
            }

            public String title() {
                return "Echo";
            }

            public String description() {
                return "Echoes a message.";
            }

            public McpJsonSchema inputSchema() {
                return McpJsonSchema.object()
                        .property("message", McpJsonSchema.string("Message to echo."));
            }

            public Object call(Map<String, Object> arguments, McpAgentContext context) {
                return arguments.get("message");
            }
        };
    }
}
