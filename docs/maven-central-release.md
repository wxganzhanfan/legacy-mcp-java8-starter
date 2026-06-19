# Maven Central 发布指南

[English](maven-central-release.en.md)

本文档说明如何把 `legacy-mcp-java8-starter` 发布到 Maven Central。不要把 token、GPG 私钥或密码提交到仓库。

## 前置条件

1. GitHub 仓库已创建并公开可访问：`https://github.com/wxganzhanfan/legacy-mcp-java8-starter`
2. Sonatype Central Portal 中 `io.github.wxganzhanfan` namespace 已验证。
3. Maven 能访问 Central Portal 发布 token。
4. GPG/PGP 签名密钥已生成，并且公钥已发布到常用 key server。
5. 所有发布模块都能生成 sources、javadoc 和签名文件。

## 配置 Central Portal Token

在 Central Portal 中生成 user token，然后写入 Maven `settings.xml`。不要写入项目仓库。

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>YOUR_TOKEN_USERNAME</username>
      <password>YOUR_TOKEN_PASSWORD</password>
    </server>
  </servers>
</settings>
```

`pom.xml` 中的 `release` profile 已将 `central-publishing-maven-plugin` 配置为读取 `central` 这个 server id。

## 配置 GPG

如果本机没有 GPG key，可以生成一个：

```powershell
gpg --full-generate-key
gpg --list-secret-keys --keyid-format LONG
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
```

发布时 Maven GPG 插件会为 POM、jar、sources jar、javadoc jar 等文件生成 `.asc` 签名。

## 发布前检查

先确认测试和 Java 8 字节码检查通过：

```powershell
mvn clean verify
```

再确认 release profile 能生成 sources 和 javadoc：

```powershell
mvn -Prelease -Dgpg.skip=true -DskipTests package
```

确认本机 GPG 签名可用：

```powershell
mvn -Prelease -DskipTests verify
```

## 正式发布

正式发布前，将所有模块版本从 `0.1.0-SNAPSHOT` 改为正式版本，例如 `0.1.0`，并更新 changelog。

上传到 Central Portal：

```powershell
mvn -Prelease deploy
```

当前配置使用 `autoPublish=false`。这意味着 Maven 会上传部署包并等待 Central Portal 校验，校验通过后需要在 Portal 中手动确认发布。第一次发布建议保留这个模式，避免误发布无法修改的制品。

## 发布后动作

1. 在 Central Portal 确认 deployment 状态。
2. 如果校验失败，根据 Portal 报错修复后重新发布新版本。
3. 校验通过后手动 publish。
4. 在 GitHub 创建对应 tag，例如 `v0.1.0`。
5. 在 README 中把示例版本更新为正式版本。

Maven Central 制品发布后不可覆盖同一个版本。如需修复，发布新的补丁版本。
