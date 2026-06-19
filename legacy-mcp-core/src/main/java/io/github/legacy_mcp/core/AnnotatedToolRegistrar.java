package io.github.legacy_mcp.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public final class AnnotatedToolRegistrar {
    private final McpToolRegistry registry;

    public AnnotatedToolRegistrar(McpToolRegistry registry) {
        this.registry = registry;
    }

    public void register(Object target) {
        if (target == null) {
            throw new IllegalArgumentException("Annotated MCP tool target must not be null");
        }
        Method[] methods = target.getClass().getMethods();
        for (Method method : methods) {
            LegacyMcpToolDefinition definition = method.getAnnotation(LegacyMcpToolDefinition.class);
            if (definition != null) {
                registry.register(new MethodTool(target, method, definition));
            }
        }
    }

    private static final class MethodTool implements LegacyMcpTool {
        private final Object target;
        private final Method method;
        private final LegacyMcpToolDefinition definition;
        private final LegacyMcpParam[] params;

        private MethodTool(Object target, Method method, LegacyMcpToolDefinition definition) {
            this.target = target;
            this.method = method;
            this.definition = definition;
            this.params = extractParams(method);
        }

        public String name() {
            return definition.name();
        }

        public String title() {
            return definition.title().length() == 0 ? definition.name() : definition.title();
        }

        public String description() {
            return definition.description();
        }

        public McpJsonSchema inputSchema() {
            McpJsonSchema schema = McpJsonSchema.object();
            for (LegacyMcpParam param : params) {
                schema.property(param.name(), McpJsonSchema.string(param.description()));
                if (param.required()) {
                    schema.required(param.name());
                }
            }
            return schema;
        }

        public Object call(Map<String, Object> arguments, McpAgentContext context) {
            Object[] values = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                values[i] = arguments.get(params[i].name());
                if (params[i].required() && values[i] == null) {
                    throw new McpException("Missing required argument: " + params[i].name());
                }
            }
            try {
                return method.invoke(target, values);
            } catch (IllegalAccessException ex) {
                throw new McpException("Cannot access MCP tool method: " + method.getName(), ex);
            } catch (InvocationTargetException ex) {
                Throwable targetException = ex.getTargetException();
                if (targetException instanceof RuntimeException) {
                    throw (RuntimeException) targetException;
                }
                throw new McpException("MCP tool method failed: " + method.getName(), targetException);
            }
        }

        private static LegacyMcpParam[] extractParams(Method method) {
            Annotation[][] annotations = method.getParameterAnnotations();
            LegacyMcpParam[] params = new LegacyMcpParam[annotations.length];
            for (int i = 0; i < annotations.length; i++) {
                for (Annotation annotation : annotations[i]) {
                    if (annotation instanceof LegacyMcpParam) {
                        params[i] = (LegacyMcpParam) annotation;
                    }
                }
                if (params[i] == null) {
                    throw new IllegalArgumentException("Every MCP tool parameter must declare @LegacyMcpParam: " + method.getName());
                }
            }
            return params;
        }
    }
}

