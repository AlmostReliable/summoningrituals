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
    maven("https://mods.uncandango.dev/maven/") {
        content {
            includeGroup("dev.uncandango")
        }
    }
    maven("https://cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }
}

dependencies {
    // KubeJS
    implementation("dev.latvian.mods:kubejs-neoforge:${almostgradle.getProperty("kjsVersion")}")
    compileOnly("dev.uncandango:kubegroovy-neoforge:${almostgradle.getProperty("kgyVersion")}")
    compileOnly("org.apache.groovy:groovy:6.0.0")
}
