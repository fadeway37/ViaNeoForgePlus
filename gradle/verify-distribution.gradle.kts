import groovy.json.JsonSlurper
import java.util.zip.ZipFile

// Checks the distributable, not the developer classpath. Upstream's JUnit
// generators are deliberately unrelated to this release safety check.
val distribution = tasks.named<Jar>("jar").flatMap { it.archiveFile }
val core = configurations.findByName("coreMod")
val verifyDistribution = tasks.register("verifyDistribution") {
    group = "verification"
    description = "Verify loader metadata, Mixins, embedded libraries and addon isolation."
    dependsOn(tasks.named("jar"))
    inputs.file(distribution)
    if (core != null) inputs.files(core)
    doLast {
        ZipFile(distribution.get().asFile).use { jar ->
            val names = jar.entries().asSequence().map { it.name }.toList()
            val files = names.toSet()
            check(names.size == files.size) { "Duplicate archive entries" }
            check("META-INF/neoforge.mods.toml" in files) { "Missing NeoForge metadata" }
            check(files.none { it == "fabric.mod.json" || it.endsWith(".accesswidener") }) { "Fabric loader metadata leaked into distribution" }
            val metadata = jar.getInputStream(jar.getEntry("META-INF/neoforge.mods.toml")).bufferedReader().use { it.readText() }
            check(!metadata.contains("\${")) { "Unexpanded mod metadata" }
            check(metadata.contains("version=\"${project.version}\"")) { "Wrong mod version" }
            val configs = files.filter { it.endsWith(".mixins.json") }
            check(configs.isNotEmpty()) { "Missing Mixin configuration" }
            var mixinCount = 0
            configs.forEach { config ->
                val data = jar.getInputStream(jar.getEntry(config)).bufferedReader().use {
                    JsonSlurper().parse(it) as Map<*, *>
                }
                val pkg = (data["package"] as String).replace('.', '/')
                listOf("mixins", "client", "server").forEach { side ->
                    (data[side] as? List<*>)?.forEach { name ->
                        check("$pkg/${name.toString().replace('.', '/')}.class" in files) { "Missing Mixin $name in $config" }
                        mixinCount++
                    }
                }
                (data["plugin"] as? String)?.let {
                    check(it.replace('.', '/') + ".class" in files) { "Missing Mixin plugin $it" }
                }
            }
            val classes = files.filter { it.endsWith(".class") && !it.startsWith("META-INF/") }
            check(classes.none { it.startsWith("net/minecraft/") || it.startsWith("com/google/gson/") || it.startsWith("io/netty/") }) { "Minecraft-provided classes must not be embedded" }
            if (core != null) {
                val descriptor = "META-INF/services/com.viaversion.viafabricplus.api.entrypoint.ViaFabricPlusEntrypoint"
                check(descriptor in files) { "Missing lifecycle service descriptor" }
                jar.getInputStream(jar.getEntry(descriptor)).bufferedReader().use { reader ->
                    reader.lineSequence().map { it.substringBefore('#').trim() }.filter { it.isNotEmpty() }.forEach {
                        check(it.replace('.', '/') + ".class" in files) { "Missing lifecycle provider $it" }
                    }
                }
                val natives = listOf("webrtc-java-windows-x86_64.dll", "webrtc-java-windows-aarch64.dll",
                    "libwebrtc-java-linux-x86_64.so", "libwebrtc-java-linux-aarch64.so", "libwebrtc-java-macos-aarch64.dylib")
                natives.forEach { check(it in files) { "Missing WebRTC native $it" } }
                val packages = classes.map { it.substringBeforeLast('/') }.toSet()
                ZipFile(core.singleFile).use { coreJar ->
                    val corePackages = coreJar.entries().asSequence().map { it.name }
                        .filter { it.endsWith(".class") && !it.startsWith("META-INF/") }
                        .map { it.substringBeforeLast('/') }.toSet()
                    val overlap = packages.intersect(corePackages)
                    check(overlap.isEmpty()) { "Split packages between core and addon: $overlap" }
                }
            } else {
                check(files.none { it.startsWith("net/raphimc/viabedrock/") || it.contains("webrtc-java") }) { "Bedrock libraries belong in the standalone addon" }
                check("com/viaversion/viafabricplus/api/ViaFabricPlusAPI.class" in files) { "Missing public API" }
            }
            logger.lifecycle("Verified ${distribution.get().asFile.name}: ${classes.size} classes, $mixinCount Mixins, loader metadata and dependency isolation")
        }
    }
}
tasks.named("check") { dependsOn(verifyDistribution) }
