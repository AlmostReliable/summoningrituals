plugins {
    id("net.neoforged.moddev") version "2.0.80"
    id("com.almostreliable.almostgradle") version "1.4.1"
}

almostgradle.setup {
    withSourcesJar = false
    dataGen = "src/main/generated"
}

repositories {
    // KubeJS
    maven("https://maven.latvian.dev/releases")
    maven("https://jitpack.io") { // Animated Gif Library
        content {
            includeGroup("com.github.rtyley")
        }
    }

    mavenLocal()
}

dependencies {
    // KubeJS
    implementation("dev.latvian.mods:kubejs-neoforge:${almostgradle.getProperty("kjsVersion")}")
}
