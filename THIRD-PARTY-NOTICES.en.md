# Third-Party Notices

[简体中文](THIRD-PARTY-NOTICES.md)

This document lists third-party open source components directly used by the main artifacts or potentially redistributed with binary packages. The module `pom.xml` files and Maven dependency trees are the source of truth for exact versions.

## Runtime Dependencies

| Component | Purpose | License |
| --- | --- | --- |
| Jackson Databind / Core / Annotations | JSON serialization, deserialization, and JSON-RPC message handling | Apache License 2.0 |
| Spring Framework / Spring Web MVC | HTTP endpoint and Spring MVC integration | Apache License 2.0 |
| Spring Boot Autoconfigure | Spring Boot 2 auto-configuration support | Apache License 2.0 |

## Shaded Jar Note

`legacy-mcp-stdio-bridge` produces an executable shaded jar. That jar includes this project's code and Jackson classes so stdio-only MCP agents can forward requests to an HTTP MCP endpoint.

When redistributing the shaded jar, keep this project's `LICENSE`, `NOTICE`, and this document. The Maven Shade plugin is configured to merge Apache LICENSE / NOTICE resources, but release maintainers should still inspect `META-INF/LICENSE` and `META-INF/NOTICE` in the final jar.

## Test and Sample Dependencies

JUnit, AssertJ, Spring Boot Test, Spring Security, Logback, and related dependencies are mainly used by tests or sample modules. They are not part of the minimal runtime distribution for the core libraries. If you publish sample applications or binary bundles separately, regenerate third-party notices based on the actual packaged content.
