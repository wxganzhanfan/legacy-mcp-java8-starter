package io.github.legacy_mcp.stdio;

public final class BridgeConfig {
    private final String url;
    private final String apiKey;
    private final String apiKeyHeader;
    private final int timeoutMs;

    public BridgeConfig(String url, String apiKey, String apiKeyHeader, int timeoutMs) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("--url is required");
        }
        this.url = url;
        this.apiKey = apiKey;
        this.apiKeyHeader = apiKeyHeader == null || apiKeyHeader.trim().isEmpty()
                ? "X-MCP-API-Key"
                : apiKeyHeader;
        this.timeoutMs = timeoutMs <= 0 ? 30000 : timeoutMs;
    }

    public static BridgeConfig parse(String[] args) {
        String url = null;
        String apiKey = null;
        String apiKeyHeader = "X-MCP-API-Key";
        int timeoutMs = 30000;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--url".equals(arg)) {
                url = requireValue(args, ++i, "--url");
            } else if ("--api-key".equals(arg)) {
                apiKey = requireValue(args, ++i, "--api-key");
            } else if ("--api-key-header".equals(arg)) {
                apiKeyHeader = requireValue(args, ++i, "--api-key-header");
            } else if ("--timeout-ms".equals(arg)) {
                timeoutMs = Integer.parseInt(requireValue(args, ++i, "--timeout-ms"));
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }
        return new BridgeConfig(url, apiKey, apiKeyHeader, timeoutMs);
    }

    private static String requireValue(String[] args, int index, String name) {
        if (index >= args.length) {
            throw new IllegalArgumentException(name + " requires a value");
        }
        return args[index];
    }

    public String getUrl() {
        return url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiKeyHeader() {
        return apiKeyHeader;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }
}
