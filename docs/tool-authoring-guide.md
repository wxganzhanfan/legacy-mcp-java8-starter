# 工具编写指南

[English](tool-authoring-guide.en.md)

稳定的基础接入方式是实现接口注册工具：

```java
public interface LegacyMcpTool {
    String name();
    String title();
    String description();
    McpJsonSchema inputSchema();
    Object call(Map<String, Object> arguments, McpAgentContext context);
}
```

在 Spring Boot 2 项目中，工具就是普通 Spring Bean。starter 只会注册实现了 `LegacyMcpTool` 的 Bean，不会扫描 Controller、Service 或任意方法。

也可以通过 `AnnotatedToolRegistrar` 使用注解式注册：

```java
@LegacyMcpToolDefinition(name = "system.user.search", description = "Search users")
public Object searchUsers(@LegacyMcpParam(name = "keyword", required = true) String keyword) {
    return userService.search(keyword);
}
```

Java 8 项目必须在 `@LegacyMcpParam` 中显式提供参数名，不要依赖编译器的参数名元数据。

写工具示例：

```java
public boolean writeOperation() {
    return true;
}
```

写工具需要同时开启全局写权限，并在对应 agent 的 `write-allowed-tools` 中声明。
