package io.github.legacy_mcp.core;

public final class JsonRpcResponse {
    private final String jsonrpc;
    private final Object id;
    private final Object result;
    private final JsonRpcError error;

    private JsonRpcResponse(Object id, Object result, JsonRpcError error) {
        this.jsonrpc = "2.0";
        this.id = id;
        this.result = result;
        this.error = error;
    }

    public static JsonRpcResponse result(Object id, Object result) {
        return new JsonRpcResponse(id, result, null);
    }

    public static JsonRpcResponse error(Object id, int code, String message) {
        return new JsonRpcResponse(id, null, new JsonRpcError(code, message));
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public Object getId() {
        return id;
    }

    public Object getResult() {
        return result;
    }

    public JsonRpcError getError() {
        return error;
    }
}

