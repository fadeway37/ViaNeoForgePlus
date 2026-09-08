# Maintaining ViaNeoForgePlus

## Pinned baselines

Core: ViaFabricPlus **5.0.1**, commit `707b0e0b44fe33f78e5a2f89f37eefec861ec660`.
Addon: viafabricplus-bedrock **1.0.0**, release `b7ed71bf63485d51cbc099097256158d59622a7e`; source checkout `a3c0498` also includes the upstream post-release documentation update.
Minecraft **26.2**, NeoForge **26.2.0.82**, Java **25**, ModDevGradle **2.0.141**.
Core/addon versions track their respective upstream repositories; the addon is not numbered 5.0.1.

Merge release commits into the corresponding active NeoForge version branch. Do not substitute a newer upstream branch tip for a requested release.
Keep original upstream tags in `refs/upstream/tags/*` and use `upstream/*` for fetched branch refs.
The tested NeoForge release tags point to port commits, not Fabric commits. Never force-update published tags without a separate coordinated release decision.
See [ref inventory](UPSTREAM_REFS.md).

## Architecture that must survive updates

- Main HEAD bootstrap installs the API before vanilla registry bootstrap; NeoForge's early loading list supplies metadata before `ModList` is ready.
- The complete service-provider list is frozen before pre-settings callbacks. Core initialization happens before addon initialization.
- Registry-dependent feature work runs after registry freeze, before protocol mapping initialization; the client joins the protocol future before post-game initialization finishes.
- NeoForge owns ticks, particles, payload registration and the mod-list config screen. Guard callbacks without a live client level/connection.
- Protocol libraries that Mixins target are merged into transforming mod output. Ordinary library-layer Jar-in-Jar is insufficient.
- Keep Minecraft libraries external. Do not split packages between core and addon. Preserve service files and dependency license notices.
- Bedrock's resource-directory fallback uses the module reader when a directory URL is absent. Keep all five WebRTC native binaries in the addon, none in the core.
- Runtime `config/vianeoforgeplus/jars` overrides are unsupported and must warn, not pretend to work.
- NeoForge changes method descriptors and call sites: fluid predicates, movement restitution, glide overloads, item-use hooks, tool abilities, key bindings, respawn dimensions and packet byte overloads all need rechecking.
- In 26.2.0.82, `performUseItemOn` has one `ItemStack.isEmpty` check after NeoForge's interaction event; upstream's ordinal 2 is invalid. Keep the required injection check.

## Build and checks

~~~powershell
.\gradlew.bat build publishToMavenLocal
# In the independent addon repository:
.\gradlew.bat build
~~~

Both `check` tasks run `verifyDistribution`: metadata version, Mixin entries, duplicate entries, forbidden packages, and core/addon separation.
The addon additionally checks lifecycle service registration, native resources and split packages against the exact local core dependency.
The upstream JUnit suite generates Minecraft update data and is gated by `-Pupdating_minecraft=true`; a normal skipped `test` is **not** a passed unit-test suite.

For this Windows host's Java 25 `PipeImpl: Invalid argument: connect` issue, a per-process `-Djdk.net.unixdomain.tmpdir=<nonexistent-path>` causes TCP fallback.
Keep that path nonexistent. Do not change Windows security settings or create it. This is an environment workaround, not a required global mod setting.

## Runtime release gate

1. Build a core-only clean instance and verify the mod list, protocol screen and settings.
2. Build a second instance with exactly core + addon; verify two lifecycle providers, Bedrock protocol registration and resource initialization.
3. Actually join an older local Java server. Check rendered world, movement, inventory and server-confirmed placement/breaking. Menu-only tests miss lazy-loaded Mixins.
4. Install the official NeoForge client and repeat with final JARs in **PRODUCTION**, not only ModDev's DEV launcher.
5. Separately report Bedrock login/gameplay, Microsoft authentication, Realms and other untested server versions. Do not infer them from startup or a Java-server test.
6. Record exact versions, local test directories, log evidence and artifact checksums in the release test report. Keep accounts/private runtime files out of Git.
7. Commit the port, then create matching release tags. Publishing/pushing is a separate action.

`scripts/launch-standalone-test.ps1` prepares and launches an **already officially installed** 26.2.0.82 client using version manifests and SHA-1-verified files.
It supports independent core/bedrock profiles, optional verified cache reuse and loopback QuickPlay. It never reads launcher account files.
It uses an offline test identity, so it is not a replacement for an authenticated launcher for public servers.

## CI

The core workflow builds and verifies the core. The addon workflow checks out the matching NeoForge core release, publishes it to MavenLocal, then builds/verifies the addon.
Until the core commit/tag is pushed to its remote, the addon CI dependency cannot be fetched there. Local builds do not prove a remote CI run has succeeded.
