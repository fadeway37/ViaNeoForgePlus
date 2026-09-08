# ViaNeoForgePlus 5.x developer API

Use **Java 25** and a **NeoForge ModDevGradle** project for Minecraft 26.2.
The API sources in `vianeoforgeplus-api/src/main/java` are compiled into the core JAR; there is no separately published NeoForge API artifact in this port.

## Dependency

Publish the core first with `./gradlew publishToMavenLocal`. Then in your addon:

~~~kotlin
repositories {
    mavenLocal { content { includeModule("com.viaversion", "vianeoforgeplus") } }
}
dependencies {
    compileOnly("com.viaversion:vianeoforgeplus:5.0.1") { isTransitive = false }
}
~~~

Install the core JAR alongside your addon for runtime. Do not embed it or a second copy of its ViaVersion libraries.
Declare a client-side required dependency on `vianeoforgeplus` in `META-INF/neoforge.mods.toml`.
Public Maven hosting of the NeoForge artifacts must be arranged separately; `repo.viaversion.com` hosts upstream Fabric artifacts, not this unpublished port.

## API surface

~~~java
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.api.ViaFabricPlusAPI;

ViaFabricPlusAPI api = ViaFabricPlus.api();
api.addChangeProtocolVersionListener((oldVersion, newVersion) -> {
    // React to a target-version change.
});
~~~

`ViaFabricPlusAPI` (API version **7**) exposes metadata, settings, `protocolTranslation()`, `conversions()`, `limitations()` and `screens()`.
The upstream Java namespace is deliberately retained. Prefer this API to `ViaFabricPlusImpl` internals.
`ViaFabricPlus.getImpl()` / `ViaFabricPlusBase` are deprecated adapters; many 4.x event/settings types were replaced in upstream 5.x.
Migrate listeners and settings explicitly instead of relying on old Fabric lifecycle registration.

## NeoForge lifecycle entrypoint

Implement `com.viaversion.viafabricplus.api.entrypoint.ViaFabricPlusEntrypoint` in a public no-argument class.
Add a UTF-8 service descriptor:

~~~text
META-INF/services/com.viaversion.viafabricplus.api.entrypoint.ViaFabricPlusEntrypoint
~~~

Its contents are the fully qualified provider name, for example:

~~~text
com.example.ExampleEntrypoint
~~~

This uses **ServiceLoader on NeoForge's transforming classloader**, not `fabric.mod.json`.
Retain a normal `@Mod` discovery class and NeoForge metadata. Do not instantiate the service provider a second time from that class.
The core provider is first; all providers are discovered before settings callbacks begin.

- `onPreLoading`: API/bootstrap before Minecraft registry bootstrap. Do not use Minecraft client instances or `ModList.get()` here.
- `onPreSettingsLoading` / `onPostSettingsLoading`: register your settings before persisted values are read.
- `onPostRegistryLoading`: after NeoForge registry creation/freeze; custom registries must use NeoForge `DeferredRegister` on the mod bus, not this callback.
- `onPostProtocolTranslationLoading`: protocol worker thread, after the Java stack loads; register protocol providers here, without UI access.
- `onPostGameLoading`: after client construction; protocol loading has been joined by the core before addon post-game callbacks.

Use the independent Bedrock addon as a working integration example.

## Custom settings

~~~java
@Override
public void onPreSettingsLoading() {
    var group = ViaFabricPlus.api().settings().register("example_settings.example");
    group.registerBoolean("enabled", true);
}
~~~

Provide translation keys `example_settings.example` and `example_settings.example.enabled`.
Versioned settings expose `isActive()`, which considers both their value and target-version range.

## Packaging

Protocol libraries targeted by Mixins must be in the transforming mod output, not NeoForge's ordinary library-layer Jar-in-Jar dependencies.
Never bundle Minecraft-provided packages or packages already owned by the core. The addon `verifyDistribution` task checks split packages and lifecycle/native resources.
An upstream Fabric JAR cannot be used as the runtime dependency of a NeoForge addon.
