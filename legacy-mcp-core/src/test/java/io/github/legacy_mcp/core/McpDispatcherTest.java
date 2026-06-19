package io.github.legacy_mcp.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpDispatcherTest {

    @Test
    void initializeReturnsServerMetadataAndToolCapability() {
        McpToolRegistry registry = new McpToolRegistry();
        McpDispatcher dispatcher = new McpDispatcher(
                new McpServerInfo("legacy-demo", "0.1.0"),
                registry
        );

        JsonRpcResponse response = dispatcher.dispatch(
                new JsonRpcRequest("2.0", 1, "initialize", Collections.<String, Object>emptyMap()),
                McpAgentContext.anonymous()
        );

        assertNull(response.getError());
        Map<?, ?> result = (Map<?, ?>) response.getResult();
        assertEquals("2025-06-18", result.get("protocolVersion"));
        assertEquals("legacy-demo", ((Map<?, ?>) result.get("serverInfo")).get("name"));
        assertTrue(((Map<?, ?>) result.get("capabilities")).containsKey("tools"));
    }

    @Test
    void initializedNotificationReturnsNoResponse() {
        McpDispatcher dispatcher = new McpDispatcher(new McpServerInfo("legacy-demo", "0.1.0"), new McpToolRegistry());

        JsonRpcResponse response = dispatcher.dispatch(
                new JsonRpcRequest("2.0", null, "notifications/initialized", Collections.<String, Object>emptyMap()),
                McpAgentContext.anonymous()
        );

        assertNull(response);
    }

    @Test
    void pingReturnsEmptyResult() {
        McpDispatcher dispatcher = new McpDispatcher(new McpServerInfo("legacy-demo", "0.1.0"), new McpToolRegistry());

        JsonRpcResponse response = dispatcher.dispatch(
                new JsonRpcRequest("2.0", "ping-1", "ping", Collections.<String, Object>emptyMap()),
                McpAgentContext.anonymous()
        );

        assertNull(response.getError());
        assertEquals(Collections.emptyMap(), response.getResult());
    }

    @Test
    void toolsListReturnsOnlyAllowedToolsForAgent() {
        McpToolRegistry registry = new McpToolRegistry();
        registry.register(new EchoTool("demo.echo"));
        registry.register(new EchoTool("demo.hidden"));
        McpDispatcher dispatcher = new McpDispatcher(new McpServerInfo("legacy-demo", "0.1.0"), registry);
        McpAgentContext context = new McpAgentContext("codex", Arrays.asList("demo.echo"));

        JsonRpcResponse response = dispatcher.dispatch(
                new JsonRpcRequest("2.0", 2, "tools/list", Collections.<String, Object>emptyMap()),
                context
        );

        Map<?, ?> result = (Map<?, ?>) response.getResult();
        List<?> tools = (List<?>) result.get("tools");
        assertEquals(1, tools.size());
        assertEquals("demo.echo", ((Map<?, ?>) tools.get(0)).get("name"));
    }

    @Test
    void toolsCallInvokesRegisteredHandler() {
        McpToolRegistry registry = new McpToolRegistry();
        registry.register(new EchoTool("demo.echo"));
        McpDispatcher dispatcher = new McpDispatcher(new McpServerInfo("legacy-demo", "0.1.0"), registry);

        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("message", "hello");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "demo.echo");
        params.put("arguments", arguments);

        JsonRpcResponse response = dispatcher.dispatch(
                new JsonRpcRequest("2.0", 3, "tools/call", params),
                new McpAgentContext("codex", Arrays.asList("demo.echo"))
        );

        Map<?, ?> result = (Map<?, ?>) response.getResult();
        List<?> content = (List<?>) result.get("content");
        assertFalse((Boolean) result.get("isError"));
        assertEquals("hello", ((Map<?, ?>) content.get(0)).get("text"));
    }

    @Test
    void unknownMethodReturnsMethodNotFound() {
        McpDispatcher dispatcher = new McpDispatcher(new McpServerInfo("legacy-demo", "0.1.0"), new McpToolRegistry());

        JsonRpcResponse response = dispatcher.dispatch(
                new JsonRpcRequest("2.0", 4, "resources/list", Collections.<String, Object>emptyMap()),
                McpAgentContext.anonymous()
        );

        assertNotNull(response.getError());
        assertEquals(-32601, response.getError().getCode());
    }

    @Test
    void unknownToolReturnsToolErrorResult() {
        McpDispatcher dispatcher = new McpDispatcher(new McpServerInfo("legacy-demo", "0.1.0"), new McpToolRegistry());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "demo.missing");
        params.put("arguments", Collections.emptyMap());

        JsonRpcResponse response = dispatcher.dispatch(
                new JsonRpcRequest("2.0", 5, "tools/call", params),
                McpAgentContext.anonymous()
        );

        Map<?, ?> result = (Map<?, ?>) response.getResult();
        assertTrue((Boolean) result.get("isError"));
        assertTrue(((Map<?, ?>) ((List<?>) result.get("content")).get(0)).get("text").toString().contains("demo.missing"));
    }

    private static final class EchoTool implements LegacyMcpTool {
        private final String name;

        private EchoTool(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public String title() {
            return "Echo";
        }

        public String description() {
            return "Echoes a message.";
        }

        public McpJsonSchema inputSchema() {
            return McpJsonSchema.object()
                    .property("message", McpJsonSchema.string("Message to echo."))
                    .required("message");
        }

        public Object call(Map<String, Object> arguments, McpAgentContext context) {
            return arguments.get("message");
        }
    }
}

