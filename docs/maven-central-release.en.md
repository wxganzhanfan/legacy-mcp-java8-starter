# Maven Central Release Guide

[简体中文](maven-central-release.md)

This document explains how to publish `legacy-mcp-java8-starter` to Maven Central. Do not commit tokens, GPG private keys, or passwords to the repository.

## Prerequisites

1. The GitHub repository exists and is publicly reachable: `https://github.com/wxganzhanfan/legacy-mcp-java8-starter`
2. The `io.github.wxganzhanfan` namespace is verified in Sonatype Central Portal.
3. Maven can access a Central Portal publishing token.
4. A GPG/PGP signing key is available, and the public key has been uploaded to a common key server.
5. All published modules can generate sources, javadocs, and signature files.

## Configure Central Portal Token

Generate a user token in Central Portal, then store it in Maven `settings.xml`. Do not store it in the project repository.

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

The `release` profile in `pom.xml` configures `central-publishing-maven-plugin` to read the `central` server id.

## Configure GPG

If the machine does not have a GPG key, create one:

```powershell
gpg --full-generate-key
gpg --list-secret-keys --keyid-format LONG
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
```

During release, the Maven GPG plugin creates `.asc` signatures for POMs, jars, sources jars, javadoc jars, and other deployed files.

## Pre-release Checks

First verify tests and Java 8 bytecode checks:

```powershell
mvn clean verify
```

Then verify that the release profile can generate sources and javadocs:

```powershell
mvn -Prelease -Dgpg.skip=true -DskipTests package
```

Verify that local GPG signing works:

```powershell
mvn -Prelease -DskipTests "-Dgpg.keyname=<KEY_FINGERPRINT>" verify
```

If the machine has only one GPG private key, `-Dgpg.keyname` can be omitted. If multiple private keys exist, explicitly specify the fingerprint of the key used for release.

## Release

Before releasing, change all module versions from `0.1.0-SNAPSHOT` to a release version such as `0.1.0`, and update the changelog.

Upload to Central Portal:

```powershell
mvn -Prelease "-Dgpg.keyname=<KEY_FINGERPRINT>" deploy
```

The current configuration uses `autoPublish=false`. Maven uploads the deployment for Central Portal validation, then manual confirmation is required in the Portal. Keep this mode for the first release to avoid accidentally publishing immutable artifacts.

## After Release

1. Check the deployment status in Central Portal.
2. If validation fails, fix the issue and publish a new version.
3. Manually publish after validation succeeds.
4. Create the matching GitHub tag, for example `v0.1.0`.
5. Update README examples to use the released version.

Maven Central artifacts cannot be overwritten after publication. Publish a new patch version for fixes.
