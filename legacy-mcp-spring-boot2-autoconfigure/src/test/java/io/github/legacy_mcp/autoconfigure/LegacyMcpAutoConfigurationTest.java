package io.github.legacy_mcp.autoconfigure;

import io.github.legacy_mcp.core.LegacyMcpTool;
import io.github.legacy_mcp.core.McpAgentContext;
import io.github.legacy_mcp.core.McpDispatcher;
import io.github.legacy_mcp.core.McpJsonSchema;
import io.github.legacy_mcp.core.McpAuditPublisher;
import io.github.legacy_mcp.core.McpPrincipalInstaller;
import io.github.legacy_mcp.core.JsonRpcRequest;
import io.github.legacy_mcp.core.JsonRpcResponse;
import io.github.legacy_mcp.webmvc.McpHttpController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LegacyMcpAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(DemoToolConfiguration.class)
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(LegacyMcpAutoConfiguration.class));

    @Test
    void autoConfigurationIsDisabledByDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(McpDispatcher.class);
            assertThat(context).doesNotHaveBean(McpHttpController.class);
        });
    }

    @Test
    void enabledAutoConfigurationRegistersMcpBeans() {
        contextRunner
                .withPropertyValues(
                        "legacy.mcp.enabled=true",
                        "legacy.mcp.server-name=test-service",
                        "legacy.mcp.agents.codex.api-key=dev-key",
                        "legacy.mcp.agents.codex.allowed-tools[0]=demo.echo"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(McpDispatcher.class);
                    assertThat(context).hasSingleBean(McpAuditPublisher.class);
                    assertThat(context).hasSingleBean(McpPrincipalInstaller.class);
                    assertThat(context).hasSingleBean(McpHttpController.class);
                    assertThat(context).hasSingleBean(LegacyMcpProperties.class);
                    assertThat(context.getBean(LegacyMcpProperties.class).getAgents()).containsKey("codex");
                });
    }

    @Test
    void enabledAutoConfigurationHonorsWriteToolPolicy() {
        contextRunner
                .withPropertyValues(
                        "legacy.mcp.enabled=true",
                        "legacy.mcp.security.write-tools-enabled=true",
                        "legacy.mcp.agents.codex.api-key=dev-key",
                        "legacy.mcp.agents.codex.allowed-tools[0]=demo.notice.publish",
                        "legacy.mcp.agents.codex.write-allowed-tools[0]=demo.notice.publish"
                )
                .run(context -> {
                    McpDispatcher dispatcher = context.getBean(McpDispatcher.class);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("name", "demo.notice.publish");
                    params.put("arguments", Collections.singletonMap("message", "hello"));

                    JsonRpcResponse response = dispatcher.dispatch(
                            new JsonRpcRequest("2.0", 1, "tools/call", params),
                            new McpAgentContext("codex",
                                    Arrays.asList("demo.notice.publish"),
                                    context.getBean(LegacyMcpProperties.class).getAgents().get("codex").getWriteAllowedTools())
                    );

                    assertThat((Boolean) ((Map<?, ?>) response.getResult()).get("isError")).isFalse();
                });
    }

    @Configuration
    static class DemoToolConfiguration {
        @Bean
        LegacyMcpTool demoEchoTool() {
            return new LegacyMcpTool() {
                public String name() {
                    return "demo.echo";
                }

                public String title() {
                    return "Echo";
                }

                public String description() {
                    return "Echo test tool.";
                }

                public McpJsonSchema inputSchema() {
                    return McpJsonSchema.object();
                }

                public Object call(Map<String, Object> arguments, McpAgentContext context) {
                    return "ok";
                }
            };
        }

        @Bean
        LegacyMcpTool writeNoticeTool() {
            return new LegacyMcpTool() {
                public String name() {
                    return "demo.notice.publish";
                }

                public String title() {
                    return "Publish notice";
                }

                public String description() {
                    return "Publishes a notice.";
                }

                public McpJsonSchema inputSchema() {
                    return McpJsonSchema.object();
                }

                public boolean writeOperation() {
                    return true;
                }

                public Object call(Map<String, Object> arguments, McpAgentContext context) {
                    return arguments.get("message");
                }
            };
        }
    }
}
