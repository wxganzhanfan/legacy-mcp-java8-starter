# Contributing

[简体中文](CONTRIBUTING.md)

Thank you for considering a contribution. This project aims to provide a small, explicit, and auditable MCP adapter for Java 8 and Spring Boot 2 legacy systems.

## Before Submitting

Before opening an issue or pull request, please try to confirm that:

- The request fits the Java 8 / Spring MVC / Spring Boot 2 legacy MCP integration scope.
- New protocol capabilities update both `docs/protocol-scope.md` and `docs/protocol-scope.en.md`.
- New tool authoring capabilities update both `docs/tool-authoring-guide.md` and `docs/tool-authoring-guide.en.md`.
- New agent integration modes update both `docs/agent-integration-guide.md` and `docs/agent-integration-guide.en.md`.
- Code remains Java 8 compatible and does not introduce Java 9+ APIs, Reactor, WebFlux, or Jakarta-only APIs.

## Local Verification

Run this before submitting when possible:

```powershell
mvn clean verify
```

To verify Java 8 bytecode compatibility, set `JAVA8_HOME` and run:

```powershell
$env:JAVA8_HOME = '<path-to-jdk8>'
powershell -ExecutionPolicy Bypass -File scripts\verify-java8.ps1
```

## Dependencies and Licenses

When adding a dependency, explain:

- Why the dependency is needed.
- Whether Java 8 compatibility is preserved.
- Whether the license is compatible with Apache-2.0 use and redistribution.
- Whether shaded jars require updates to `THIRD-PARTY-NOTICES.en.md`.

Do not submit company-internal code, real business data, secrets, tokens, cookies, private repository URLs, or document fragments that cannot be published.
