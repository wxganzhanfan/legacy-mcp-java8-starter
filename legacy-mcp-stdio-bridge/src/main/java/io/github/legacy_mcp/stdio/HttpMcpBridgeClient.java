package io.github.legacy_mcp.stdio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class HttpMcpBridgeClient {
    private final BridgeConfig config;

    public HttpMcpBridgeClient(BridgeConfig config) {
        this.config = config;
    }

    public String forward(String jsonRpcBody) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(config.getUrl()).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(config.getTimeoutMs());
        connection.setReadTimeout(config.getTimeoutMs());
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            connection.setRequestProperty(config.getApiKeyHeader(), config.getApiKey());
        }
        connection.setDoOutput(true);

        byte[] requestBytes = jsonRpcBody.getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(requestBytes.length);
        OutputStream outputStream = connection.getOutputStream();
        try {
            outputStream.write(requestBytes);
        } finally {
            outputStream.close();
        }

        int status = connection.getResponseCode();
        InputStream responseStream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
        String responseBody = responseStream == null ? "" : read(responseStream);
        if (status >= 200 && status < 300) {
            return responseBody;
        }
        throw new IOException("HTTP MCP endpoint returned " + status + ": " + responseBody);
    }

    private String read(InputStream inputStream) throws IOException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            inputStream.close();
        }
    }
}
