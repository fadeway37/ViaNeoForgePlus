# Developer API

ViaNeoForgePlus exposes events and utility functions so other mods can integrate with it.
If you want to include it in your project, keep in mind:

- Use the Java version specified by `project_jvm_version` in [gradle.properties](../gradle.properties)
- Needs a **NeoForge ModDevGradle** setup (since it's a Minecraft mod, not a standalone API library)

## Adding as a Dependency

Use the `vianeoforgeplus` artifact, which includes the **public API**.
Run `./gradlew publishToMavenLocal` in the ViaNeoForgePlus repository, then add it to your project:

### Kotlin (Gradle)

```kotlin
repositories {
    mavenLocal()
}

dependencies {
    // Replace it with the release you published
    runtimeOnly("com.viaversion:vianeoforgeplus:x.x.x") {
        isTransitive = false
    }
    compileOnly("com.viaversion:vianeoforgeplus:x.x.x") {
        isTransitive = false
    }
}
```

### Groovy (Gradle)

```groovy
repositories {
    mavenLocal()
}

dependencies {
    // Replace it with the release you published
    runtimeOnly("com.viaversion:vianeoforgeplus:x.x.x") {
        transitive = false
    }
    compileOnly("com.viaversion:vianeoforgeplus:x.x.x") {
        transitive = false
    }
}
```

Declare a client-side required dependency on `vianeoforgeplus` in `META-INF/neoforge.mods.toml`.
Users need to install ViaNeoForgePlus alongside your mod; do not bundle it inside your own JAR.

## Using the API

Everything is reached through a single entry point:

```java
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.api.ViaFabricPlusAPI;

final ViaFabricPlusAPI api = ViaFabricPlus.api();
```

`ViaFabricPlusAPI` is the only stable API surface other mods should use. Next to the mod's own metadata it hands
out four sub-APIs:

- `protocolTranslation()` – the target version, per-connection versions, ViaVersion's `UserConnection` and a
  listener for version changes
- `conversions()` – translating items between Minecraft and ViaVersion, and dummy user connections
- `limitations()` – whether an item, enchantment, effect or banner pattern exists in a given version
- `screens()` – opening the mod's own screens

`apiVersion()` is incremented on every meaningful API change, so you can check what you're running against.

```java
ViaFabricPlus.api().addChangeProtocolVersionListener((oldVersion, newVersion) -> {
    // Called whenever the target protocol version changes
});
```

> `ViaFabricPlus.getImpl()` and `ViaFabricPlusBase` are deprecated for removal. Use `ViaFabricPlus.api()` and
> `ViaFabricPlusAPI` instead.

## Entrypoint

Since your mod may load **after** ViaNeoForgePlus, hook into the loading cycle with a `ViaFabricPlusEntrypoint`.
Implement it in a public class with a public no-argument constructor:

```java
package com.example;

import com.viaversion.viafabricplus.api.entrypoint.ViaFabricPlusEntrypoint;

public class ExampleEntrypoint implements ViaFabricPlusEntrypoint {
}
```

Create `src/main/resources/META-INF/services/com.viaversion.viafabricplus.api.entrypoint.ViaFabricPlusEntrypoint`
and add the class name:

```text
com.example.ExampleEntrypoint
```

All of its methods are optional. Keep your usual NeoForge `@Mod` class; ViaNeoForgePlus creates the service provider
for you, so do not instantiate it again there.

Use `onPostGameLoading()` for client UI setup. The protocol-loading callback runs on a worker thread.
See [ViaFabricPlusEntrypoint](../vianeoforgeplus-api/src/main/java/com/viaversion/viafabricplus/api/entrypoint/ViaFabricPlusEntrypoint.java)
for the available callbacks.

## Custom Settings

`api.settings()` gives access to the mod's own groups and lets you register your own, which then show up as their
own tab in the settings screen. Register them in `onPreSettingsLoading()`, before saved settings are read:

```java
@Override
public void onPreSettingsLoading() {
    final SettingGroup group = ViaFabricPlus.api().settings().register("example_settings.example");

    final BooleanSetting enabled = group.registerBoolean("enabled", true);
    final VersionedBooleanSetting legacyOnly = group.registerVersionedBoolean("legacy_only", andOlder(v1_12_2), false);
}
```

The group and its settings are named by translation keys, so the example above needs `example_settings.example`,
`example_settings.example.enabled` and `example_settings.example.legacy_only` in your language files.

A `VersionedBooleanSetting` displays its version range in the screen, and its `isActive()` only returns `true`
when the setting is enabled and the target version is inside that range.
