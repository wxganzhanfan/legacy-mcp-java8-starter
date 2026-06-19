package io.github.legacy_mcp.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AnnotatedToolRegistryTest {

    @Test
    void annotationScannerRegistersMethodsWithExplicitParameterNames() {
        McpToolRegistry registry = new McpToolRegistry();
        new AnnotatedToolRegistrar(registry).register(new UserTools());
        McpDispatcher dispatcher = new McpDispatcher(new McpServerInfo("legacy-demo", "0.1.0"), registry);

        Map<String, Object> params = new java.util.HashMap<String, Object>();
        params.put("name", "system.user.search");
        params.put("arguments", Collections.<String, Object>singletonMap("keyword", "alice"));

        JsonRpcResponse response = dispatcher.dispatch(
                new JsonRpcRequest("2.0", 1, "tools/call", params),
                new McpAgentContext("codex", Arrays.asList("system.user.search"))
        );

        Map<?, ?> result = (Map<?, ?>) response.getResult();
        List<?> content = (List<?>) result.get("content");
        assertFalse((Boolean) result.get("isError"));
        assertEquals("alice-result", ((Map<?, ?>) content.get(0)).get("text"));
    }

    public static final class UserTools {
        @LegacyMcpToolDefinition(
                name = "system.user.search",
                title = "Search users",
                description = "Search users by keyword."
        )
        public String search(@LegacyMcpParam(name = "keyword", required = true) String keyword) {
            return keyword + "-result";
        }
    }
}

