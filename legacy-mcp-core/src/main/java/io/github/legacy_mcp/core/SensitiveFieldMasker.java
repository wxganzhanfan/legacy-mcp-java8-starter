package io.github.legacy_mcp.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SensitiveFieldMasker {
    public static final String MASK = "******";

    @SuppressWarnings("unchecked")
    public Map<String, Object> mask(Map<String, Object> input) {
        Map<String, Object> masked = new LinkedHashMap<String, Object>();
        if (input == null) {
            return masked;
        }
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            if (isSensitive(entry.getKey())) {
                masked.put(entry.getKey(), MASK);
            } else {
                masked.put(entry.getKey(), maskValue(entry.getValue()));
            }
        }
        return masked;
    }

    @SuppressWarnings("unchecked")
    private Object maskValue(Object value) {
        if (value instanceof Map) {
            return mask((Map<String, Object>) value);
        }
        if (value instanceof List) {
            List<Object> masked = new ArrayList<Object>();
            for (Object item : (List<?>) value) {
                masked.add(maskValue(item));
            }
            return masked;
        }
        return value;
    }

    private boolean isSensitive(String key) {
        if (key == null) {
            return false;
        }
        String lower = key.toLowerCase(Locale.ENGLISH);
        return lower.contains("password")
                || lower.contains("secret")
                || lower.contains("token")
                || lower.contains("apikey")
                || lower.contains("api_key")
                || lower.contains("authorization");
    }
}
