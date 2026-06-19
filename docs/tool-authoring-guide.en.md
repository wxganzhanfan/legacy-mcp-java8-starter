# Tool Authoring Guide

[简体中文](tool-authoring-guide.md)

The stable baseline integration style is to implement the tool interface:

```java
public interface LegacyMcpTool {
    String name();
    String title();
    String description();
    McpJsonSchema inputSchema();
    Object call(Map<String, Object> arguments, McpAgentContext context);
}
```

In a Spring Boot project, tools are ordinary Spring Beans. The starter only registers Beans that implement `LegacyMcpTool`. It does not scan Controllers, Services, or arbitrary methods.

You can also register annotated methods through `AnnotatedToolRegistrar`:

```java
@LegacyMcpToolDefinition(name = "system.user.search", description = "Search users")
public Object searchUsers(@LegacyMcpParam(name = "keyword", required = true) String keyword) {
    return userService.search(keyword);
}
```

Java 8 projects must provide parameter names explicitly in `@LegacyMcpParam`. Do not rely on compiler parameter-name metadata.

Write tool example:

```java
public boolean writeOperation() {
    return true;
}
```

Write tools require global write permission to be enabled and must also be listed under the corresponding agent's `write-allowed-tools`.
