# Developer API

ViaNeoForgePlus keeps the upstream API packages under `com.viaversion.viafabricplus` for source and binary
compatibility. Existing integrations can continue to use `ViaFabricPlus.getImpl()`, `ViaFabricPlusBase`, settings
types, and the loading/protocol callbacks without package renames.

The port currently builds the API, visuals, and implementation into one NeoForge mod JAR. There is no separately
published `vianeoforgeplus-api` artifact in this workspace. For local development, depend on the completed
ViaNeoForgePlus JAR as `compileOnly`/NeoForge mod development input.

## Accessing the API

```java
ViaFabricPlusBase api = ViaFabricPlus.getImpl();
ProtocolVersion target = api.getTargetVersion();

api.registerOnChangeProtocolVersionCallback((oldVersion, newVersion) -> {
    // React to a global or per-server protocol change.
});
```

The legacy `ViaFabricPlusLoadEntrypoint` type is retained for compatibility with code compiled against the upstream
API. NeoForge integrations should register callbacks from their own mod initialization after ViaNeoForgePlus is
available instead of declaring a Fabric entrypoint.

Resource IDs and translation keys intentionally remain in the `viafabricplus` namespaces. Do not create duplicate
`vianeoforgeplus` copies of those assets.
