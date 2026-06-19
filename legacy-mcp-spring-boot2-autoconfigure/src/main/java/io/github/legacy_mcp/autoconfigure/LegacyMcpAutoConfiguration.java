package io.github.legacy_mcp.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.legacy_mcp.core.LegacyMcpTool;
import io.github.legacy_mcp.core.LoggingMcpAuditPublisher;
import io.github.legacy_mcp.core.McpAuditPublisher;
import io.github.legacy_mcp.core.McpDispatcher;
import io.github.legacy_mcp.core.McpPrincipalInstaller;
import io.github.legacy_mcp.core.McpServerInfo;
import io.github.legacy_mcp.core.McpToolRegistry;
import io.github.legacy_mcp.core.NoopMcpPrincipalInstaller;
import io.github.legacy_mcp.webmvc.HeaderApiKeyMcpAgentResolver;
import io.github.legacy_mcp.webmvc.McpAgentRegistration;
import io.github.legacy_mcp.webmvc.McpAgentResolver;
import io.github.legacy_mcp.webmvc.McpHttpController;
import io.github.legacy_mcp.webmvc.McpHttpProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AutoConfiguration
@EnableConfigurationProperties(LegacyMcpProperties.class)
@ConditionalOnProperty(prefix = "legacy.mcp", name = "enabled", havingValue = "true")
public class LegacyMcpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public McpToolRegistry mcpToolRegistry(ObjectProvider<LegacyMcpTool> tools) {
        McpToolRegistry registry = new McpToolRegistry();
        for (LegacyMcpTool tool : tools) {
            registry.register(tool);
        }
        return registry;
    }

    @Bean
    @ConditionalOnMissingBean
    public McpDispatcher mcpDispatcher(LegacyMcpProperties properties,
                                       McpToolRegistry registry,
                                       McpAuditPublisher auditPublisher,
                                       McpPrincipalInstaller principalInstaller) {
        return new McpDispatcher(
                new McpServerInfo(properties.getServerName(), properties.getServerVersion()),
                registry,
                auditPublisher,
                principalInstaller,
                properties.getSecurity().isWriteToolsEnabled()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public McpAuditPublisher mcpAuditPublisher() {
        return new LoggingMcpAuditPublisher();
    }

    @Bean
    @ConditionalOnMissingBean
    public McpPrincipalInstaller mcpPrincipalInstaller() {
        return new NoopMcpPrincipalInstaller();
    }

    @Bean
    @ConditionalOnMissingBean
    public McpHttpProperties mcpHttpProperties(LegacyMcpProperties properties) {
        return new McpHttpProperties(properties.getEndpoint(), properties.getLimits().getMaxRequestBytes());
    }

    @Bean
    @ConditionalOnMissingBean
    public McpAgentResolver mcpAgentResolver(LegacyMcpProperties properties) {
        List<McpAgentRegistration> registrations = new ArrayList<McpAgentRegistration>();
        for (Map.Entry<String, LegacyMcpProperties.Agent> entry : properties.getAgents().entrySet()) {
            LegacyMcpProperties.Agent agent = entry.getValue();
            registrations.add(new McpAgentRegistration(entry.getKey(),
                    agent.getApiKey(),
                    agent.getAllowedTools(),
                    agent.getWriteAllowedTools()));
        }
        return new HeaderApiKeyMcpAgentResolver(properties.getSecurity().getAuthHeader(), registrations);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper legacyMcpObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public McpHttpController mcpHttpController(McpDispatcher dispatcher,
                                               ObjectMapper objectMapper,
                                               McpAgentResolver agentResolver,
                                               McpHttpProperties httpProperties) {
        return new McpHttpController(dispatcher, objectMapper, agentResolver, httpProperties);
    }
}
