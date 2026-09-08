# ViaNeoForgePlus 5.0.1 test report

Test date: **September 8, 2026**, times below in **Asia/Shanghai (UTC+08:00)**.
These are local build and interactive regression results, not a GitHub Actions result or a claim that every supported protocol was tested.

## Exact environment and artifacts

- Core upstream: `707b0e0b44fe33f78e5a2f89f37eefec861ec660` (5.0.1).
- Core: ViaNeoForgePlus **5.0.1**; optional addon: ViaNeoForgePlus Bedrock **1.0.0**.
- Client: **Minecraft 26.2**, **NeoForge 26.2.0.82**, FML **11.0.16**, Windows x64, Java **25.0.3**.
- Build: checked-in **Gradle 9.7.1**, ModDevGradle **2.0.141**.
- Official NeoForge Maven metadata checked on the test date: lastUpdated `20260908050413`; latest 26.2 entry `26.2.0.82`.
- Metadata source: https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml

SHA-256 of the final build outputs, verified identical to the copies actually tested in both official-client profiles:

~~~text
DC5374AA4623D9DDA9DC466B180550D7EC2F0734DD6BF4042801C1CA3360673C  ViaNeoForgePlus-5.0.1.jar
54F9926C164DF80AC19E910A2D2EC855E70A92AE2518E2663611F49D7A3F5C05  vianeoforgeplus-bedrock-1.0.0.jar
~~~

## Build checks

`.\gradlew.bat build publishToMavenLocal` succeeded for the core; `.\gradlew.bat build` succeeded in the separate addon repository.

- Core distribution: **5,524 classes / 414 Mixin entries**; NeoForge metadata and public API present; no Fabric metadata/access wideners, ViaBedrock or WebRTC natives.
- Addon distribution: **1,891 classes / 48 Mixin entries**; service provider and all five WebRTC platform resources present.
- Both distribution checks reject duplicate ZIP entries and bundled Minecraft/Gson/Netty classes. The addon check found no split class packages with the core.
- NeoForge access-transformer validation and Java compilation succeeded.
- Core `test` was **SKIPPED**: the upstream JUnit sources are opt-in Minecraft update-data generators, not regression coverage. The addon has no JUnit test sources.
- Gradle reported deprecations affecting future Gradle 10 compatibility. They do not constitute a Gradle 10 validation.

## New isolated installation

Test root: `D:/project2/vianeoforgeplus-test-26.2-26.2.0.82`.

| Relative directory | Purpose |
| --- | --- |
| `core` | ModDev DEV, core-only JAR loading |
| `bedrock` | ModDev DEV, core + addon JAR loading |
| `standalone` | New client installed with the official NeoForge 26.2.0.82 installer |
| `standalone/instances/core` | Independent official PROD core-only client profile |
| `standalone/instances/bedrock` | Independent official PROD core + addon client profile |
| `server-26.1.2` | Loopback-only Java 26.1.2 regression server with a new flat world |

The official installed client uses its generated NeoForge version manifest, patched Minecraft JAR and official library/asset manifests.
The standalone launcher verified **115 classpath artifacts** and **5,057 unique asset objects** using SHA-1 from the manifests.
Asset index 32 SHA-1: `795a52d29f7f6b1e51d3a65e60ca46ad62aaddec`.
Both official profile logs explicitly report **CLIENT in PROD**; ModDev logs explicitly report **CLIENT in DEV**.

No accounts, settings, worlds or launcher login data were copied from the existing `run/` directory.
The standalone profiles use the offline test identity `VNFTest` and can QuickPlay only to a loopback server.
The server reused only an existing vanilla server JAR; its world/configuration/player data were newly created for this task.
It binds `127.0.0.1:25582`, with RCON/query/management disabled. Do not expose this offline-mode test server publicly.

## Passed interactive scenarios

| Runtime | Verified result |
| --- | --- |
| DEV, core only | Startup, mod list, protocol and settings screens; exactly one lifecycle provider; normal exit at 18:33:17 |
| DEV, core + addon | Two providers; Bedrock registration/resource initialization; Java 26.1.2 world, inventory, movement, server-confirmed placement/breaking; normal exit at 18:51:48 |
| PROD, core + addon | Correct two-mod list; two providers; Bedrock 1.26.30 (1001) and resource HTTP startup; Java 26.1.2 world, inventory, movement, placement/breaking; Bedrock settings tab with no account; normal exit at 19:14:28 |
| PROD, core only | Correct core-only mod list; one provider and no Bedrock registration/tab; Java 26.1.2 world, inventory, movement, placement/breaking; protocol/settings screens; normal exit at 19:25:09 |

F3 in the official clients displayed client protocol **26.2 (776)** translating to server **26.1–26.1.2 (775)**.
The core-only settings contained General, Visual and Advanced; the two-mod profile additionally contained Bedrock.
The Bedrock Realms control remained unavailable without an account; no sign-in or diagnostic upload was initiated.

Server-side conditional block checks emitted these markers in `server-26.1.2/logs/latest.log`:

| Time | Marker |
| --- | --- |
| 18:46:48 | `VNF_501_PLACE_PASS` (DEV) |
| 18:47:02 | `VNF_501_BREAK_PASS` (DEV) |
| 19:07:57 | `VNF_501_PROD_ADDON_PLACE_PASS_5_m60_10` |
| 19:08:28 | `VNF_501_PROD_ADDON_BREAK_PASS_5_m60_10` |
| 19:22:30 | `VNF_501_PROD_CORE_PLACE_PASS_4_m60_9` |
| 19:23:24 | `VNF_501_PROD_CORE_BREAK_PASS_4_m60_9` |

The official core player's position changed from `[5.509888484256009,-60,8.70990624966535]` at 19:20:18 to
`[5.5197769685120175,-60,8.9198124993307]` at 19:21:42 in response to movement input.
Both the client display and server state were checked, not just a successful connection message.
The server was stopped normally at **19:25:09**, saving players and all dimensions. Both official client processes exited **0**.

## Bugs found and fixed before the passing runs

- Registry/bootstrap ordering and addon provider discovery were corrected so the full provider list exists before settings callbacks and registry-dependent work runs after NeoForge registry freeze.
- Entering a world initially exposed an invalid upstream `ordinal=2` injection into NeoForge's `performUseItemOn`.
  The patched production bytecode contains one `ItemStack.isEmpty()` call after NeoForge interaction hooks.
  The injection now targets that call with `require=1, allow=1`. The subsequent DEV and both PROD gameplay runs passed.

Earlier failed-run crash reports remain under the DEV profile directories as debugging evidence, including
`bedrock/crash-reports/crash-2026-09-08_18.37.30-client.txt`. They predate the fix and are not removed to make the test appear clean.

## Warnings and explicit limits

The logs are **not error-free**. Observed non-fatal issues included this Windows host's OSHI/Perflib counters,
offline-account HTTP 401 responses for user attributes/certificates, unauthorized Realms feature checks,
NeoForge shader sampler warnings, a Netty Linux-epoll capability-probe/log-rendering exception on Windows,
unregistered `vv:mod_details` payload warnings, and unavailable LAN multicast discovery.
The loopback NIO connection and all listed gameplay checks still completed. No Windows security/network settings were changed.

The Java 25 UDS workaround is confined to the test process: `-Djdk.net.unixdomain.tmpdir=<nonexistent-path>` causes TCP fallback.
It does not require changing global Java or system settings; keep the specified directory absent.

**Not tested:** real Bedrock/RakNet server login or gameplay, Microsoft/Xbox authentication, Bedrock/Java Realms,
public authenticated Java servers, ClassiCube accounts, every legacy/April-Fools protocol, other mods/modded servers,
non-Windows native loading and production CI.
The addon registers **Bedrock 1.26.30**, but no compatible Bedrock test server/account was available.
The official Bedrock download service returned stable **1.26.45.1** on the test date; that was not treated as a matching-protocol server and was not downloaded or launched.
Source: https://net-secondary.web.minecraft-services.net/api/v1.0/download/links

## Repeat the local test

Use the test root's `start-client.ps1` (PowerShell 7) with `-Profile core` or `-Profile bedrock`.
For the optional loopback integration test, start `start-server.ps1` in another terminal, then add `-Server 127.0.0.1:25582` to the client command.
Type `stop` in the server console afterwards. The test-root README contains the exact paths and requirements.

The reusable implementation is `scripts/launch-standalone-test.ps1` in the core repository.
It expects an already officially installed client, supports `-PrepareOnly`, verifies assets/libraries,
copies only the requested mod artifacts, and never reads launcher account files.
Use a normal authenticated launcher for public online-mode servers.
