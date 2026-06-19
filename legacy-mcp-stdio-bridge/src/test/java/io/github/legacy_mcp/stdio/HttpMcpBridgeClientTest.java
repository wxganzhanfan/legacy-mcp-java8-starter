package io.github.legacy_mcp.stdio;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpMcpBridgeClientTest {

    @Test
    void forwardsJsonRpcBodyAndApiKeyHeader() throws Exception {
        RecordingHandler handler = new RecordingHandler();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/mcp", handler);
        server.start();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/mcp";
            HttpMcpBridgeClient client = new HttpMcpBridgeClient(new BridgeConfig(url, "dev-key", "X-MCP-API-Key", 5000));

            String response = client.forward("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}");

            assertEquals("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{}}", response);
            assertEquals("POST", handler.method);
            assertEquals("dev-key", handler.apiKey);
            assertEquals("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}", handler.body);
        } finally {
            server.stop(0);
        }
    }

    private static final class RecordingHandler implements HttpHandler {
        private String method;
        private String apiKey;
        private String body;

        public void handle(HttpExchange exchange) throws IOException {
            method = exchange.getRequestMethod();
            apiKey = exchange.getRequestHeaders().getFirst("X-MCP-API-Key");
            body = read(exchange.getRequestBody());
            byte[] response = "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{}}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }

        private String read(InputStream inputStream) throws IOException {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
