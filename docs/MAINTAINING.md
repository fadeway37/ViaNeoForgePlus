# Maintaining ViaNeoForgePlus

## Updating Minecraft or NeoForge

Update `minecraft_version`, `neoforge_version`, and `project_jvm_version` in `gradle.properties`, then update the
ModDevGradle plugin version if required. Keep the exact ViaVersion-family build identifiers pinned until the complete
client reaches the main menu and protocol mappings finish loading.

Run:

```powershell
.\gradlew.bat clean compileJava
.\gradlew.bat runClient
.\gradlew.bat build
```

## Port checks

- Resolve every failed required Mixin against the patched NeoForge Minecraft method descriptors.
- Re-check NeoForge hooks used for client ticks, commands, payload registration, particles, and the mod-list config
  screen.
- Keep `META-INF/accesstransformer.cfg` synchronized with access needs; prefer a Mixin invoker when a broad public
  method transform is unnecessary.
- Protocol libraries targeted by Mixins must remain merged into the main transforming mod output. Do not move them
  back to ordinary Jar-in-Jar `LIBRARY` entries.
- Verify ViaBedrock resource/skin packs load without mapping errors in both the development folder module and the final
  JAR.
- Inspect the final archive for NeoForge metadata and confirm it contains no `fabric.mod.json` or access widener.

## Release verification

Start a clean NeoForge 26.2 instance containing only the produced ViaNeoForgePlus JAR. Confirm the main menu loads,
the mod-list settings button opens, protocol mappings finish, the native WebRTC resource for the current platform is
present, and no missing-class or Mixin errors appear in `latest.log`.

Retain legacy package/resource identifiers unless a deliberate breaking API migration is planned.
