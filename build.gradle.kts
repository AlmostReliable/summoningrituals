plugins {
    id("net.neoforged.moddev") version "2.0.143"
    id("com.almostreliable.almostgradle") version "2.4.0"
}

almostgradle.setup {
    withSourcesJar = false
    downloadSources = true
    downloadJavadoc = true
    dataGen = "src/main/generated"
}

repositories {
    // KubeJS
    maven("https://maven.latvian.dev/releases")
    maven("https://maven.latvian.dev/mirror")
}

dependencies {
    // KubeJS
    implementation("dev.latvian.mods:kubejs-neoforge:${almostgradle.getProperty("kjsVersion")}")
}
