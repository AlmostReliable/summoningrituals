@file:Suppress("UnstableApiUsage")


val license: String by project
val enableAccessWidener: String by project
val minecraftVersion: String by project
val modVersion: String by project
val modPackage: String by project
val modId: String by project
val modName: String by project
val modAuthor: String by project
val modDescription: String by project
val modCredits: String by project
val parchmentVersion: String by project
val manifoldVersion: String by project
val forgeVersion: String by project
val forgeRecipeViewer: String by project
val jeiVersion: String by project
val reiVersion: String by project
val kubeVersion: String by project
val githubUser: String by project
val githubRepo: String by project

plugins {
    id("dev.architectury.loom") version "1.4.+"
    id("com.github.gmazzo.buildconfig") version "4.0.4"
    java
}

base {
    version = "$minecraftVersion-$modVersion"
    archivesName.set("$modId-forge")
}

loom {
    silentMojangMappingsLicense()

    forge {
        mixinConfig("$modId.mixins.json")
    }

    if (project.findProperty("enableAccessWidener") == "true") {
        accessWidenerPath.set(file("src/main/resources/$modId.accesswidener"))
        forge {
            convertAccessWideners.set(true)
            extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
        }
        println("Access widener enabled for project. Access widener path: ${loom.accessWidenerPath.get()}")
    }

    runs {
        forEach {
            val dir = "run/${it.environment}"
            println("[Run Config] '${it.name}' directory: $dir")
            it.runDir(dir)
            // allows DCEVM hot-swapping when using the JBR (https://github.com/JetBrains/JetBrainsRuntime)
            it.vmArgs("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
        }
    }
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots") // Manifold
    maven("https://maven.parchmentmc.org/") // Parchment
    maven("https://maven.blamejared.com") // JEI
    maven("https://maven.shedaniel.me") // REI
    maven("https://maven.saps.dev/minecraft") // KubeJS
    maven("https://cursemaven.com") // Jade
    mavenLocal()
}

dependencies {
    // Minecraft
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
    })

    // Forge
    forge("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")

    // Manifold
    compileOnly("systems.manifold:manifold-ext-rt:$manifoldVersion")
    annotationProcessor("systems.manifold:manifold-ext:$manifoldVersion")

    // JEI/REI
    modCompileOnlyApi("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion") { isTransitive = false }
    modCompileOnlyApi("mezz.jei:jei-$minecraftVersion-forge-api:$jeiVersion") { isTransitive = false }
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-forge:$reiVersion")
    compileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:9.+")

    // KubeJS
    modCompileOnly("dev.latvian.mods:kubejs-forge:$kubeVersion")
    localRuntime("io.github.llamalad7:mixinextras-forge:0.2.0-rc.4")

    // Runtime
    modLocalRuntime("curse.maven:jade-324717:4986594") // Jade 11.7.1
    modLocalRuntime("dev.latvian.mods:kubejs-forge:$kubeVersion")
    when (forgeRecipeViewer) {
        "rei" -> modLocalRuntime("me.shedaniel:RoughlyEnoughItems-forge:$reiVersion")
        "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion") { isTransitive = false }
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
        options.compilerArgs.add("-Xplugin:Manifold no-bootstrap")
    }

    withType<GenerateModuleMetadata> {
        enabled = false
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
