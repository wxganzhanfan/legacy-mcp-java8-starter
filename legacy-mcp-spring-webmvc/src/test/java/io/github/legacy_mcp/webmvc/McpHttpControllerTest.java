package io.github.legacy_mcp.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.legacy_mcp.core.LegacyMcpTool;
import io.github.legacy_mcp.core.McpAgentContext;
import io.github.legacy_mcp.core.McpDispatcher;
import io.github.legacy_mcp.core.McpJsonSchema;
import io.github.legacy_mcp.core.McpServerInfo;
import io.github.legacy_mcp.core.McpToolRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class McpHttpControllerTest {

    @Test
    void postInitializeReturnsJsonRpcResponse() throws Exception {
        MockMvc mvc = mvc(1024);

        mvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-MCP-API-Key", "dev-key")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.result.serverInfo.name").value("test-mcp"))
                .andExpect(jsonPath("$.result.capabilities.tools").exists());
    }

    @Test
    void toolsListUsesResolvedAgentAllowlist() throws Exception {
        MockMvc mvc = mvc(1024);

        mvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-MCP-API-Key", "dev-key")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.tools", hasSize(1)))
                .andExpect(jsonPath("$.result.tools[0].name").value("demo.echo"));
    }

    @Test
    void invalidApiKeyReturnsUnauthorized() throws Exception {
        MockMvc mvc = mvc(1024);

        mvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-MCP-API-Key", "wrong")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void oversizedRequestReturnsPayloadTooLarge() throws Exception {
        MockMvc mvc = mvc(16);

        mvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-MCP-API-Key", "dev-key")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}"))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    void getMcpReturnsMethodNotAllowed() throws Exception {
        MockMvc mvc = mvc(1024);

        mvc.perform(get("/mcp"))
                .andExpect(status().isMethodNotAllowed());
    }

    private MockMvc mvc(int maxBytes) {
        McpToolRegistry registry = new McpToolRegistry();
        registry.register(new EchoTool("demo.echo"));
        registry.register(new EchoTool("demo.hidden"));
        McpDispatcher dispatcher = new McpDispatcher(new McpServerInfo("test-mcp", "0.1.0"), registry);
        McpAgentResolver resolver = new HeaderApiKeyMcpAgentResolver(
                "X-MCP-API-Key",
                Arrays.asList(new McpAgentRegistration("codex", "dev-key", Arrays.asList("demo.echo")))
        );
        McpHttpController controller = new McpHttpController(
                dispatcher,
                new ObjectMapper(),
                resolver,
                new McpHttpProperties("/mcp", maxBytes)
        );
        return MockMvcBuilders.standaloneSetup(controller).build();
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
            return "Echo test tool.";
        }

        public McpJsonSchema inputSchema() {
            return McpJsonSchema.object();
        }

        public Object call(Map<String, Object> arguments, McpAgentContext context) {
            return "ok";
        }
    }
}

