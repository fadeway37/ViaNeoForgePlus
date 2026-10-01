import org.gradle.api.artifacts.component.ModuleComponentIdentifier


plugins {
    `java-library`
    `maven-publish`
    id("net.neoforged.moddev") version "2.0.147"
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
        resources.srcDir("vianeoforgeplus-api/src/main/resources")
    }
}
val productionRunSourceSet = sourceSets.create("productionRun")

val embedded = configurations.create("embedded") {
    isCanBeConsumed = false
    isCanBeResolved = true
}
configurations.compileOnly {
    extendsFrom(embedded)
}

// NeoForge loads ordinary Jar-in-Jar dependencies on its library layer, where
// Mixins cannot transform them. ViaNeoForgePlus intentionally targets classes
// in ViaVersion, ViaLegacy and their support libraries, so merge
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
    from({ mergedLibraries.map(::zipTree) })
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
val prepareProductionRun = tasks.register<Copy>("prepareProductionRun") {
    dependsOn(tasks.named("jar"))
    from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    into(layout.projectDirectory.dir("run/production/mods"))
}
val freshClientDirectory = layout.projectDirectory.dir(
    providers.gradleProperty("clientDirectory").getOrElse("../vianeoforgeplus-test-26.2-26.2.0.82/core")
)
val prepareFreshClient = tasks.register<Copy>("prepareFreshClient") {
    dependsOn(tasks.named("jar"))
    from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    into(freshClientDirectory.dir("mods"))
}

neoForge {
    version = property("neoforge_version") as String
    validateAccessTransformers = true
    addModdingDependenciesTo(sourceSets.test.get())
    addModdingDependenciesTo(productionRunSourceSet)

    runs {
        register("freshClient") {
            client()
            gameDirectory.set(freshClientDirectory)
            sourceSet.set(productionRunSourceSet)
            loadedMods.set(emptySet())
            taskBefore(prepareFreshClient)
            providers.gradleProperty("testServer").orNull?.let {
                programArgument("--quickPlayMultiplayer")
                programArgument(it)
            }
        }
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
    add("embedded", "com.viaversion:viaversion-common:5.12.0-20260904.185715-7")
    add("embedded", "com.viaversion:viabackwards-common:5.12.0-20260904.190714-4")
    add("embedded", "com.viaversion:viaaprilfools-common:4.2.3-20260820.140819-4")
    add("embedded", "net.raphimc:ViaLegacy:3.1.0-20260830.173114-8")

    add("embedded", "net.lenni0451:Reflect:1.6.4")
    add("embedded", "de.florianreuth:classic4j:2.3.1")

    testImplementation(platform("org.junit:junit-bom:6.0.3"))
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "vianeoforgeplus"
            pom {
                name.set("ViaNeoForgePlus")
                description.set(project.property("project_description").toString())
                url.set("https://github.com/fadeway37/ViaNeoForgePlus")
            }
        }
    }
}

apply(from = "gradle/verify-distribution.gradle.kts")
