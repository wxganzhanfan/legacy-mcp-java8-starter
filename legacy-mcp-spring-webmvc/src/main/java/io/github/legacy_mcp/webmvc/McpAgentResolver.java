package io.github.legacy_mcp.webmvc;

import io.github.legacy_mcp.core.McpAgentContext;
import org.springframework.http.HttpHeaders;

public interface McpAgentResolver {
    McpAgentContext resolve(HttpHeaders headers);
}
