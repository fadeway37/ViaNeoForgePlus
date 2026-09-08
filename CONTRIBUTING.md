# Contributing to ViaNeoForgePlus

Use Java **25** and the included Gradle wrapper. A standalone Gradle installation is not needed.
Open this repository as a NeoForge ModDevGradle project. The current port is on **ver/26.2**.

~~~powershell
.\gradlew.bat build
.\gradlew.bat runClient
~~~

For addon work, publish the core with `.\gradlew.bat publishToMavenLocal`, then build the separate `vianeoforgeplus-bedrock` repository.
Keep addon-specific dependencies and features there; do not restore Bedrock to the core.

## Changes

Start a feature branch from the active port branch and target the same branch with a pull request.
Preserve upstream copyright notices, package names and translation keys. Describe whether a fix is upstream behavior or a NeoForge-specific adaptation.
Historical gameplay and visual emulation are both part of the 5.x core; provide evidence of the historical behavior being reproduced.
Do not weaken required Mixin checks merely to suppress a launch failure.

Run distribution checks, then test an actual world/server interaction as appropriate. For loading/packaging changes, also test final JARs in a clean official NeoForge installation.
Document untested cases honestly. Keep logs with tokens, account JSON, private worlds and generated test instances out of commits.

## Translations and API

Upstream translations are maintained on [Crowdin](https://crowdin.com/project/viafabricplus).
Keep translation keys in the upstream namespace and port-facing product labels as ViaNeoForgePlus.
For API changes, update Javadocs and [integration examples](docs/DEVELOPER_API.md). 5.x uses the NeoForge service-provider lifecycle described there.
See [maintenance](docs/MAINTAINING.md) for architecture constraints and release checks.
