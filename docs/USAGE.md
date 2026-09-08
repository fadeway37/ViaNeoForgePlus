# ViaNeoForgePlus usage

## Install and migrate

Use Minecraft **26.2**, NeoForge **26.2.0.82** and Java **25**. Install only `ViaNeoForgePlus-5.0.1.jar` for Java-edition servers.
For Bedrock, add the separate `vianeoforgeplus-bedrock-1.0.0.jar` as well. Neither Fabric Loader nor Fabric API is needed.

Before upgrading from 4.x, back up the instance's `config/vianeoforgeplus` directory and remove the older core JAR from that instance.
5.x reorganizes the API and settings; review defaults instead of assuming every old setting was migrated.
Visual features are now in the core, so do not install a separate old visuals module.
Bedrock support and its dependencies have moved out of the core. Keep account files private; never attach them to an issue report.
No automatic account/config/world migration from another instance is performed by the test launcher.

## Protocol selection

**Multiplayer → ViaNeoForgePlus** opens the protocol screen. It is also available from NeoForge's mod-list Config button.
Tabs group protocols by era, and search matches version/update names. **Auto Detect (1.7+ servers)** detects a supported Java server's version when connecting.
Change the global target only while disconnected. **Add/Edit Server → Set version** stores a per-server override in `servers.dat`; Reset restores the global choice.
The Direct Connect button changes the global selection. Button placement is configurable in General settings.

BetaCraft and ClassiCube lists remain available. Joining authenticated servers requires the corresponding valid account/session.
Their online/account workflows are not covered by the offline regression tests.

## Settings and commands

Settings have General, Visual and Advanced tabs; optional integrations may add tabs, including Bedrock.
Version-range settings act only within their supported target range. Advanced options are intended for debugging.

The primary command is `/vianeoforgeplus`; `/viafabricplus` remains an alias.
Use `/vianeoforgeplus settings` for settings; historical targets also expose `settime` and `listextensions` where applicable.
ViaVersion diagnostic commands may upload diagnostic data: inspect what will be shared before creating a dump.
The F3 debug screen can display the ViaNeoForgePlus connection information.

## Bedrock addon

Install core **5.0.1** and addon **1.0.0**, then select **Bedrock 1.26.30** in the protocol screen.
Bedrock uses UDP (normally port 19132), unlike Java's TCP server connection.
The Bedrock settings include automatic default-port replacement and experimental-feature toggles.
Authenticated servers and Realms require a Microsoft/Bedrock account via **Account for Bedrock Edition**; complete authentication yourself in the browser.
The Realms control stays unavailable until an account is set.
This feature retains upstream ViaBedrock limitations and is not a guarantee of compatibility with a different Bedrock protocol version.

## Configuration

Files are under the **current instance's** `config/vianeoforgeplus`:

- `settings.json`: settings and selected protocol.
- `classicube.json`: ClassiCube account data, when configured.
- `viaversion.yml`, `viabackwards.yml`, `vialegacy.yml`, `viaaprilfools.yml`: Java protocol libraries.
- With the addon: `bedrock.json` (account data) and ViaBedrock configuration/resources.

**Runtime library overrides in `jars/` are not supported on NeoForge.** Its module graph cannot accept Fabric's mutable-classloader trick.
Old JARs there are ignored with a warning, not loaded. To test a different Via library, change the pinned Gradle dependency and rebuild.
The directory may still be created for compatibility. Do not place ordinary mods there.

## Troubleshooting

Check the instance's `logs/latest.log` and `crash-reports`. A menu-only launch does not prove gameplay Mixins are valid.
Use a clean two-mod (or core-only) instance to reproduce issues. Do not upload account JSON, access tokens or unrelated private logs.
See [the test report](TESTING-5.0.1.md) for the verified scenarios and explicit limitations of this release.
