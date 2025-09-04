plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

val modName: String by extra
val minecraftVersion: String by extra
rootProject.name = "${modName.replace(" ", "-")}-$minecraftVersion-NeoForge"
