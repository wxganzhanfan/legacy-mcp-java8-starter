package io.github.legacy_mcp.webmvc;

import io.github.legacy_mcp.core.McpAgentContext;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HeaderApiKeyMcpAgentResolver implements McpAgentResolver {
    private final String headerName;
    private final List<McpAgentRegistration> registrations;

    public HeaderApiKeyMcpAgentResolver(String headerName, List<McpAgentRegistration> registrations) {
        this.headerName = headerName == null ? "X-MCP-API-Key" : headerName;
        this.registrations = registrations == null
                ? Collections.<McpAgentRegistration>emptyList()
                : Collections.unmodifiableList(new ArrayList<McpAgentRegistration>(registrations));
    }

    public McpAgentContext resolve(HttpHeaders headers) {
        String suppliedKey = extractApiKey(headers);
        for (McpAgentRegistration registration : registrations) {
            if (secureEquals(suppliedKey, registration.getApiKey())) {
                return new McpAgentContext(registration.getAgentId(),
                        registration.getAllowedTools(),
                        registration.getWriteAllowedTools());
            }
        }
        return null;
    }

    private String extractApiKey(HttpHeaders headers) {
        String bearer = headers.getFirst("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring("Bearer ".length());
        }
        return headers.getFirst(headerName);
    }

    private boolean secureEquals(String supplied, String expected) {
        if (supplied == null || expected == null) {
            return false;
        }
        byte[] suppliedBytes = supplied.getBytes(StandardCharsets.UTF_8);
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(suppliedBytes, expectedBytes);
    }
}
