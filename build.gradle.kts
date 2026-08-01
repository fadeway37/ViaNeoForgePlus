import org.gradle.api.artifacts.component.ModuleComponentIdentifier

plugins {
    `java-library`
    `maven-publish`
    id("net.neoforged.moddev") version "2.0.141"
}

group = property("project_group") as String
version = property("project_version") as String

base {
    archivesName = property("project_name") as String
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of((property("project_jvm_version") as String).toInt())
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://repo.viaversion.com")
    maven("https://maven.lenni0451.net/everything")
    maven("https://jitpack.io") {
        content {
            includeGroup("com.github.oryxel1")
        }
    }
}

sourceSets {
    main {
        java.srcDir("vianeoforgeplus-api/src/main/java")
        java.srcDir("vianeoforgeplus-visuals/src/main/java")
        resources.srcDir("vianeoforgeplus-api/src/main/resources")
        resources.srcDir("vianeoforgeplus-visuals/src/main/resources")
    }
}
val productionRunSourceSet = sourceSets.create("productionRun")

val embedded = configurations.create("embedded") {
    isCanBeConsumed = false
    isCanBeResolved = true
}
val webrtcNatives = configurations.create("webrtcNatives") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}
configurations.compileOnly {
    extendsFrom(embedded)
}

// NeoForge loads ordinary Jar-in-Jar dependencies on its library layer, where
// Mixins cannot transform them. ViaNeoForgePlus intentionally targets classes
// in ViaVersion, ViaLegacy, ViaBedrock and their support libraries, so merge
// those libraries into the mod output. Libraries supplied by Minecraft remain
// external to avoid split packages on the module layer.
val minecraftProvidedModules = setOf(
    "com.google.code.gson:gson",
    "com.google.errorprone:error_prone_annotations",
    "it.unimi.dsi:fastutil",
    "org.jetbrains:annotations",
    "org.joml:joml",
    "org.projectlombok:lombok"
)
val mergedLibraries = embedded.incoming.artifactView {
    componentFilter { identifier ->
        identifier !is ModuleComponentIdentifier ||
            "${identifier.group}:${identifier.module}" !in minecraftProvidedModules
    }
}.files
val mergedLibraryOutput = layout.buildDirectory.dir("generated/merged-libraries")
val mergeLibraries = tasks.register<Sync>("mergeLibraries") {
    from({ (mergedLibraries + webrtcNatives).map(::zipTree) })
    into(mergedLibraryOutput)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude(
        "META-INF/MANIFEST.MF",
        "META-INF/INDEX.LIST",
        "META-INF/*.SF",
        "META-INF/*.RSA",
        "META-INF/*.DSA",
        "META-INF/maven/**",
        "module-info.class",
        "META-INF/versions/**/module-info.class"
    )
}
sourceSets.main {
    output.dir(mapOf("builtBy" to mergeLibraries), mergedLibraryOutput)
}
val prepareProductionRun = tasks.register<Sync>("prepareProductionRun") {
    dependsOn(tasks.named("jar"))
    from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    into(layout.projectDirectory.dir("run/production/mods"))
}

neoForge {
    version = property("neoforge_version") as String
    validateAccessTransformers = true
    addModdingDependenciesTo(sourceSets.test.get())
    addModdingDependenciesTo(productionRunSourceSet)

    runs {
        register("client") {
            client()
        }
        register("productionClient") {
            client()
            gameDirectory.set(layout.projectDirectory.dir("run/production"))
            sourceSet.set(productionRunSourceSet)
            loadedMods.set(emptySet())
            taskBefore(prepareProductionRun)
        }
        register("localIntegrationClient") {
            client()
            gameDirectory.set(layout.projectDirectory.dir("run/production"))
            sourceSet.set(productionRunSourceSet)
            loadedMods.set(emptySet())
            taskBefore(prepareProductionRun)
            programArgument("--quickPlayMultiplayer")
            programArgument("127.0.0.1:25566")
        }
    }

    mods {
        register("vianeoforgeplus") {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    add("embedded", "com.viaversion:viaversion-common:5.10.1-20260627.115115-6")
    add("embedded", "com.viaversion:viabackwards-common:5.10.1-20260620.160713-2")
    add("embedded", "com.viaversion:viaaprilfools-common:4.2.1")
    add("embedded", "net.raphimc:ViaLegacy:3.0.16")
    add("embedded", "net.raphimc:ViaBedrock:0.0.29-20260626.094539-2") {
        exclude(group = "com.mojang", module = "brigadier")
        exclude(group = "at.yawk.lz4", module = "lz4-java")
        exclude(group = "io.netty")
    }

    add("embedded", "net.lenni0451:Reflect:1.6.3")
    add("embedded", "de.florianreuth:classic4j:2.3.0")
    add("embedded", "net.raphimc:MinecraftAuth:5.0.1") {
        exclude(group = "com.google.code.gson", module = "gson")
    }
    add("embedded", "dev.kastle.netty:netty-transport-raknet:1.7.0") {
        exclude(group = "io.netty")
    }
    add("embedded", "dev.kastle.netty:netty-transport-nethernet:1.7.0") {
        exclude(group = "io.netty")
        exclude(group = "com.google.code.gson", module = "gson")
    }

    arrayOf("windows-x86_64", "windows-aarch64", "linux-x86_64", "linux-aarch64", "macos-aarch64").forEach {
        add("webrtcNatives", "dev.kastle.webrtc:webrtc-java:1.0.3:$it")
    }

    testImplementation(platform("org.junit:junit-bom:6.1.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = (project.property("project_jvm_version") as String).toInt()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    enabled = project.property("updating_minecraft").toString().toBoolean()
}

tasks.processResources {
    exclude("fabric.mod.json", "*.accesswidener")

    val properties = mapOf(
        "version" to project.version,
        "description" to project.property("project_description"),
        "minecraftVersion" to project.property("minecraft_version"),
        "neoForgeVersion" to project.property("neoforge_version")
    )
    inputs.properties(properties)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(properties)
    }
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.property("project_name").toString(),
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "ViaVersion",
            "Automatic-Module-Name" to "com.viaversion.vianeoforgeplus"
        )
    }
}
