package io.github.legacy_mcp.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpSecurityAuditTest {

    @Test
    void writeToolIsBlockedUnlessGlobalAndAgentAllowIt() {
        McpToolRegistry registry = new McpToolRegistry();
        registry.register(new WriteNoticeTool());
        RecordingAuditPublisher auditPublisher = new RecordingAuditPublisher();
        McpDispatcher dispatcher = new McpDispatcher(
                new McpServerInfo("legacy-demo", "0.1.0"),
                registry,
                auditPublisher,
                new NoopMcpPrincipalInstaller(),
                false
        );

        JsonRpcResponse blocked = dispatcher.dispatch(
                toolCall("demo.notice.publish", Collections.singletonMap("message", "hello")),
                new McpAgentContext("codex", Arrays.asList("demo.notice.publish"),
                        Arrays.asList("demo.notice.publish"))
        );

        assertTrue((Boolean) ((Map<?, ?>) blocked.getResult()).get("isError"));
        assertTrue(toolText(blocked).contains("Write tool is disabled"));
        assertFalse(auditPublisher.events.get(0).isSuccess());

        McpDispatcher writeEnabledDispatcher = new McpDispatcher(
                new McpServerInfo("legacy-demo", "0.1.0"),
                registry,
                new RecordingAuditPublisher(),
                new NoopMcpPrincipalInstaller(),
                true
        );
        JsonRpcResponse agentBlocked = writeEnabledDispatcher.dispatch(
                toolCall("demo.notice.publish", Collections.singletonMap("message", "hello")),
                new McpAgentContext("codex", Arrays.asList("demo.notice.publish"), Collections.<String>emptyList())
        );

        assertTrue((Boolean) ((Map<?, ?>) agentBlocked.getResult()).get("isError"));
        assertTrue(toolText(agentBlocked).contains("not allowed to write"));
    }

    @Test
    void auditEventIsPublishedForSuccessfulAndFailedToolCallsWithSensitiveFieldsMasked() {
        McpToolRegistry registry = new McpToolRegistry();
        registry.register(new EchoTool());
        registry.register(new FailingTool());
        RecordingAuditPublisher auditPublisher = new RecordingAuditPublisher();
        McpDispatcher dispatcher = new McpDispatcher(
                new McpServerInfo("legacy-demo", "0.1.0"),
                registry,
                auditPublisher,
                new NoopMcpPrincipalInstaller(),
                false
        );

        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("message", "hello");
        arguments.put("apiKey", "secret-key");
        JsonRpcResponse success = dispatcher.dispatch(
                toolCall("demo.echo", arguments),
                new McpAgentContext("codex", Arrays.asList("demo.echo", "demo.fail"))
        );
        JsonRpcResponse failure = dispatcher.dispatch(
                toolCall("demo.fail", Collections.<String, Object>emptyMap()),
                new McpAgentContext("codex", Arrays.asList("demo.echo", "demo.fail"))
        );

        assertFalse((Boolean) ((Map<?, ?>) success.getResult()).get("isError"));
        assertTrue((Boolean) ((Map<?, ?>) failure.getResult()).get("isError"));
        assertEquals(2, auditPublisher.events.size());
        assertTrue(auditPublisher.events.get(0).isSuccess());
        assertFalse(auditPublisher.events.get(1).isSuccess());
        assertEquals("******", auditPublisher.events.get(0).getArguments().get("apiKey"));
        assertEquals("boom", auditPublisher.events.get(1).getErrorMessage());
    }

    @Test
    void principalInstallerIsClearedAfterToolExecution() {
        McpToolRegistry registry = new McpToolRegistry();
        registry.register(new EchoTool());
        RecordingPrincipalInstaller installer = new RecordingPrincipalInstaller();
        McpDispatcher dispatcher = new McpDispatcher(
                new McpServerInfo("legacy-demo", "0.1.0"),
                registry,
                new RecordingAuditPublisher(),
                installer,
                false
        );

        dispatcher.dispatch(
                toolCall("demo.echo", Collections.singletonMap("message", "hello")),
                new McpAgentContext("codex", Arrays.asList("demo.echo"))
        );

        assertEquals("codex", installer.installed.getAgentId());
        assertTrue(installer.clearCalled);
        assertNull(installer.current);
    }

    private static JsonRpcRequest toolCall(String name, Map<String, Object> arguments) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", name);
        params.put("arguments", arguments);
        return new JsonRpcRequest("2.0", 1, "tools/call", params);
    }

    private static String toolText(JsonRpcResponse response) {
        Map<?, ?> result = (Map<?, ?>) response.getResult();
        List<?> content = (List<?>) result.get("content");
        return String.valueOf(((Map<?, ?>) content.get(0)).get("text"));
    }

    private static final class RecordingAuditPublisher implements McpAuditPublisher {
        private final List<McpAuditEvent> events = new ArrayList<McpAuditEvent>();

        public void publish(McpAuditEvent event) {
            events.add(event);
        }
    }

    private static final class RecordingPrincipalInstaller implements McpPrincipalInstaller {
        private McpPrincipal installed;
        private McpPrincipal current;
        private boolean clearCalled;

        public void install(McpPrincipal principal) {
            this.installed = principal;
            this.current = principal;
        }

        public void clear() {
            this.clearCalled = true;
            this.current = null;
        }
    }

    private static class EchoTool implements LegacyMcpTool {
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
            return McpJsonSchema.object().property("message", McpJsonSchema.string("Message"));
        }

        public Object call(Map<String, Object> arguments, McpAgentContext context) {
            return arguments.get("message");
        }
    }

    private static final class FailingTool implements LegacyMcpTool {
        public String name() {
            return "demo.fail";
        }

        public String title() {
            return "Fail";
        }

        public String description() {
            return "Always fails.";
        }

        public McpJsonSchema inputSchema() {
            return McpJsonSchema.object();
        }

        public Object call(Map<String, Object> arguments, McpAgentContext context) {
            throw new IllegalStateException("boom");
        }
    }

    private static final class WriteNoticeTool extends EchoTool {
        public String name() {
            return "demo.notice.publish";
        }

        public boolean writeOperation() {
            return true;
        }
    }
}
