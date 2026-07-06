@file:Suppress("UnstableApiUsage")

val license: String by project
val minecraftVersion: String by project
val modVersion: String by project
val modPackage: String by project
val modId: String by project
val modName: String by project
val modAuthor: String by project
val modDescription: String by project
val modCredits: String by project
val forgeVersion: String by project
val forgeRecipeViewer: String by project
val jeiVersion: String by project
val reiVersion: String by project
val kubeVersion: String by project
val githubUser: String by project
val githubRepo: String by project

plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.140"
    id("com.github.gmazzo.buildconfig") version "4.0.4"
    java
}

// cannot be configured inside the block for some reason
legacyForge.version = "$minecraftVersion-$forgeVersion"

base {
    version = "$minecraftVersion-$modVersion"
    archivesName.set("$modId-forge")
}

legacyForge {
    runs {
        configureEach {
            // DCEVM hot-swapping
            jvmArgument("-XX:+AllowEnhancedClassRedefinition")
            jvmArgument("-XX:+IgnoreUnrecognizedVMOptions")
        }

        create("client") {
            client()
        }
        create("server") {
            server()
        }
    }
    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

mixin {
    add(sourceSets.main.get(), "$modId.mixins.refmap.json")
    config("$modId.mixins.json")
}

repositories {
    maven("https://maven.blamejared.com") // JEI
    maven("https://maven.shedaniel.me") // REI and Cloth Math
    maven("https://maven.latvian.dev/releases") // KubeJS and Rhino
    maven("https://maven.architectury.dev") // Architectury for KubeJS
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    modCompileOnlyApi("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion") { isTransitive = false }
    modCompileOnlyApi("mezz.jei:jei-$minecraftVersion-forge-api:$jeiVersion") { isTransitive = false }
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-forge:$reiVersion")
    modCompileOnly("me.shedaniel.cloth:basic-math:0.6.1")
    compileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:9.+")

    modImplementation("dev.latvian.mods:kubejs-forge:$kubeVersion")
    modImplementation("dev.latvian.mods:rhino-forge:2001.2.2-build.17")
    modImplementation("dev.architectury:architectury-forge:9.1.12")
    runtimeOnly("io.github.llamalad7:mixinextras-forge:0.2.0-rc.4")

    when (forgeRecipeViewer) {
        "rei" -> modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-forge:$reiVersion")
        "jei" -> modRuntimeOnly("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion") { isTransitive = false }
        else -> throw GradleException("Invalid recipeViewer value: $forgeRecipeViewer")
    }
}

tasks {
    processResources {
        val resourceTargets = listOf("META-INF/mods.toml", "pack.mcmeta")

        val replaceProperties = mapOf(
            "license" to license,
            "minecraftVersion" to minecraftVersion,
            "version" to project.version as String,
            "modId" to modId,
            "modName" to modName,
            "modAuthor" to modAuthor,
            "modDescription" to modDescription,
            "modCredits" to modCredits,
            "forgeVersion" to forgeVersion,
            "forgeLoaderVersion" to forgeVersion.substringBefore("."),
            "jeiVersion" to jeiVersion,
            "reiVersion" to reiVersion,
            "kubeVersion" to kubeVersion,
            "githubUser" to githubUser,
            "githubRepo" to githubRepo
        )

        println("[Process Resources] Replacing properties in resources: ")
        replaceProperties.forEach { (key, value) -> println("\t -> $key = $value") }

        inputs.properties(replaceProperties)
        filesMatching(resourceTargets) {
            expand(replaceProperties)
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    withType<GenerateModuleMetadata> {
        enabled = false
    }

    jar {
        manifest.attributes(mapOf("MixinConfigs" to "$modId.mixins.json"))
    }
}

extensions.configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

buildConfig {
    buildConfigField("String", "MOD_ID", "\"$modId\"")
    buildConfigField("String", "MOD_NAME", "\"$modName\"")
    buildConfigField("String", "MOD_VERSION", "\"$version\"")
    packageName(modPackage)
    className(modName.replace(" ", "") + "Constants")
    useJavaOutput()
}
