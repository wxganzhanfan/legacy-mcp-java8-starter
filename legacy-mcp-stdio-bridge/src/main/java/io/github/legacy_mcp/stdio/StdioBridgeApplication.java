package io.github.legacy_mcp.stdio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public final class StdioBridgeApplication {
    private StdioBridgeApplication() {
    }

    public static void main(String[] args) throws Exception {
        BridgeConfig config = BridgeConfig.parse(args);
        HttpMcpBridgeClient client = new HttpMcpBridgeClient(config);
        run(client,
                new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                new PrintWriter(System.out, true),
                new PrintWriter(System.err, true));
    }

    static void run(HttpMcpBridgeClient client,
                    BufferedReader stdin,
                    PrintWriter stdout,
                    PrintWriter stderr) throws Exception {
        String line;
        while ((line = stdin.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                stdout.println(client.forward(line));
            } catch (Exception ex) {
                stderr.println("legacy-mcp-stdio-bridge error: " + ex.getMessage());
                stdout.println("{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":{\"code\":-32603,\"message\":\"Bridge forwarding failed\"}}");
            }
        }
    }
}
