# ViaNeoForgePlus

Client-side NeoForge port of [ViaVersion/ViaFabricPlus](https://github.com/ViaVersion/ViaFabricPlus).
The upstream project supplies the protocol fixes, API, visuals and original implementation; this port adapts its loader integration and Minecraft hooks to NeoForge.

## Release 5.0.1

| Component | Version |
| --- | --- |
| ViaNeoForgePlus | **5.0.1** (upstream release 5.0.1) |
| Minecraft client | **26.2** |
| NeoForge | **26.2.0.82** (latest 26.2 build verified September 8, 2026) |
| Java | **25**, 64-bit |
| Optional ViaNeoForgePlus Bedrock | **1.0.0** (upstream addon version) |

The exact core upstream baseline is `707b0e0b44fe33f78e5a2f89f37eefec861ec660`, not the newer tip of `ver/26.2`.
The active port branch and release tag are `ver/26.2` and `5.0.1`. Existing older NeoForge history is retained.
See [ref synchronization](docs/UPSTREAM_REFS.md) for the distinction between tested port releases and archived upstream refs.

## Install

1. Create a Minecraft **26.2 / NeoForge 26.2.0.82** instance using **Java 25**.
2. Put `ViaNeoForgePlus-5.0.1.jar` in its `mods` directory.
3. Open **Multiplayer → ViaNeoForgePlus**, or **Mods → ViaNeoForgePlus → Config**, and select a protocol.

Do not install Fabric Loader, Fabric API, the Fabric ViaFabricPlus JAR, or an older ViaNeoForgePlus JAR alongside this release.
The core JAR contains the Java-edition protocol stack and the formerly separate visual features.
**It no longer contains ViaBedrock.**

### Optional Bedrock support

The independently versioned NeoForge addon lives in the separate sibling repository **vianeoforgeplus-bedrock**.
Install `vianeoforgeplus-bedrock-1.0.0.jar` alongside the core for the Bedrock protocol, settings, account and Realms screens.
It ports [florianreuth/viafabricplus-bedrock](https://github.com/florianreuth/viafabricplus-bedrock) 1.0.0; it is not a Fabric addon loaded through a compatibility layer.
No public NeoForge-addon download or Maven repository is assumed to exist until separately published.

## Scope

- Client-only: do not install on a server. The server does not need this mod.
- Protocol choices cover Classic, Alpha, Beta, release and selected special versions from the upstream stack.
- Select **Auto Detect (1.7+ servers)** for supported Java servers, or choose the exact target version.
- Bedrock is optional and experimental, with the limitations of ViaBedrock. The bundled protocol is **Bedrock 1.26.30 (1001)**.
- This release targets client **26.2 only**. Historical branches are not a promise of current maintenance or tested NeoForge compatibility.
- Older resource packs and arbitrary modded-server registries are not made cross-version compatible.
- Server anti-cheat and server rules still apply.

## Build

Use Java 25 and the checked-in Gradle wrapper; no separate Gradle installation is needed.

~~~powershell
.\gradlew.bat build
.\gradlew.bat publishToMavenLocal
~~~

Output: `build/libs/ViaNeoForgePlus-5.0.1.jar`. Maven coordinate: `com.viaversion:vianeoforgeplus:5.0.1` (local publication).
Build and publish the core before building the independent addon.
`check` includes distribution/Mixin-entry/dependency-isolation checks; upstream update-data generators are **not** normal regression tests.

- [Usage and 4.x migration](docs/USAGE.md)
- [Developer API and lifecycle](docs/DEVELOPER_API.md)
- [Maintenance and release checks](docs/MAINTAINING.md)
- [5.0.1 test report](docs/TESTING-5.0.1.md)
- [Contributing](CONTRIBUTING.md)

## Compatibility identifiers and credits

The NeoForge mod ID, artifact, config directory, logger and primary command use `vianeoforgeplus` / ViaNeoForgePlus.
Upstream Java packages, translation keys, resource namespaces and persistent identifiers retain `viafabricplus` to avoid needless breakage.
The 5.x upstream API changes still require integrations to migrate; the old 4.x API is not fully preserved.

Thanks to the original ViaFabricPlus and ViaVersion contributors, ViaBackwards, ViaAprilFools, ViaLegacy, and the optional ViaBedrock/MinecraftAuth projects.
Copyright notices and upstream licenses are retained. Licensed under [GPL-3.0-or-later](LICENSE).
Report port-specific issues to [ViaNeoForgePlus](https://github.com/fadeway37/ViaNeoForgePlus/issues).
