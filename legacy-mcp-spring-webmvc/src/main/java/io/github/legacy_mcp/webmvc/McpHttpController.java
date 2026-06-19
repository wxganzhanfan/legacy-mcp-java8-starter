package io.github.legacy_mcp.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.legacy_mcp.core.JsonRpcRequest;
import io.github.legacy_mcp.core.JsonRpcResponse;
import io.github.legacy_mcp.core.McpAgentContext;
import io.github.legacy_mcp.core.McpDispatcher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("${legacy.mcp.endpoint:/mcp}")
public class McpHttpController {
    private final McpDispatcher dispatcher;
    private final ObjectMapper objectMapper;
    private final McpAgentResolver agentResolver;
    private final McpHttpProperties properties;

    public McpHttpController(McpDispatcher dispatcher,
                             ObjectMapper objectMapper,
                             McpAgentResolver agentResolver,
                             McpHttpProperties properties) {
        this.dispatcher = dispatcher;
        this.objectMapper = objectMapper;
        this.agentResolver = agentResolver;
        this.properties = properties;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> post(@RequestBody String body, @RequestHeader HttpHeaders headers) throws IOException {
        if (body.getBytes(StandardCharsets.UTF_8).length > properties.getMaxRequestBytes()) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        }
        McpAgentContext agentContext = agentResolver.resolve(headers);
        if (agentContext == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        JsonRpcRequest request = objectMapper.readValue(body, JsonRpcRequest.class);
        JsonRpcResponse response = dispatcher.dispatch(request, agentContext);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Void> get() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}
