package io.github.legacy_mcp.samples.simple;

import io.github.legacy_mcp.core.LegacyMcpTool;
import io.github.legacy_mcp.core.McpAgentContext;
import io.github.legacy_mcp.core.McpJsonSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
public class DemoToolConfiguration {

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
                return "Echoes the supplied message.";
            }

            public McpJsonSchema inputSchema() {
                return McpJsonSchema.object()
                        .property("message", McpJsonSchema.string("Message to echo."))
                        .required("message");
            }

            public Object call(Map<String, Object> arguments, McpAgentContext context) {
                return arguments.get("message");
            }
        };
    }

    @Bean
    public LegacyMcpTool userSearchTool() {
        return new LegacyMcpTool() {
            public String name() {
                return "demo.user.search";
            }

            public String title() {
                return "Search users";
            }

            public String description() {
                return "Searches demo users by keyword.";
            }

            public McpJsonSchema inputSchema() {
                return McpJsonSchema.object()
                        .property("keyword", McpJsonSchema.string("Keyword to search."))
                        .required("keyword");
            }

            public Object call(Map<String, Object> arguments, McpAgentContext context) {
                String keyword = String.valueOf(arguments.get("keyword"));
                List<String> users = Arrays.asList("alice", "bob", "charlie");
                return users.toString() + " keyword=" + keyword;
            }
        };
    }

    @Bean
    public LegacyMcpTool noticePublishTool() {
        return new LegacyMcpTool() {
            public String name() {
                return "demo.notice.publish";
            }

            public String title() {
                return "Publish notice";
            }

            public String description() {
                return "Publishes a demo notice.";
            }

            public McpJsonSchema inputSchema() {
                return McpJsonSchema.object()
                        .property("message", McpJsonSchema.string("Notice message."))
                        .required("message");
            }

            public boolean writeOperation() {
                return true;
            }

            public Object call(Map<String, Object> arguments, McpAgentContext context) {
                return "published: " + arguments.get("message");
            }
        };
    }
}
