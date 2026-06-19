package io.github.legacy_mcp.core;

import java.util.Collections;
import java.util.Map;

public final class JsonRpcRequest {
    private String jsonrpc;
    private Object id;
    private String method;
    private Map<String, Object> params;

    public JsonRpcRequest() {
    }

    public JsonRpcRequest(String jsonrpc, Object id, String method, Map<String, Object> params) {
        this.jsonrpc = jsonrpc;
        this.id = id;
        this.method = method;
        this.params = params == null ? Collections.<String, Object>emptyMap() : params;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, Object> getParams() {
        return params == null ? Collections.<String, Object>emptyMap() : params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public boolean isNotification() {
        return id == null;
    }
}

