package io.github.legacy_mcp.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class McpJsonSchema {
    private final Map<String, Object> schema;

    private McpJsonSchema(String type) {
        this.schema = new LinkedHashMap<String, Object>();
        this.schema.put("type", type);
    }

    public static McpJsonSchema object() {
        McpJsonSchema jsonSchema = new McpJsonSchema("object");
        jsonSchema.schema.put("properties", new LinkedHashMap<String, Object>());
        return jsonSchema;
    }

    public static McpJsonSchema string(String description) {
        McpJsonSchema jsonSchema = new McpJsonSchema("string");
        if (description != null) {
            jsonSchema.schema.put("description", description);
        }
        return jsonSchema;
    }

    @SuppressWarnings("unchecked")
    public McpJsonSchema property(String name, McpJsonSchema propertySchema) {
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        if (properties == null) {
            properties = new LinkedHashMap<String, Object>();
            schema.put("properties", properties);
        }
        properties.put(name, propertySchema.toMap());
        return this;
    }

    @SuppressWarnings("unchecked")
    public McpJsonSchema required(String name) {
        List<String> required = (List<String>) schema.get("required");
        if (required == null) {
            required = new ArrayList<String>();
            schema.put("required", required);
        }
        required.add(name);
        return this;
    }

    public Map<String, Object> toMap() {
        return new LinkedHashMap<String, Object>(schema);
    }
}

