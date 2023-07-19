pluginManagement {
    repositories {
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        gradlePluginPortal()
    }
}

val modName = extra.get("modName").toString().replace(" ", "-")
val minecraftVersion: String by extra
rootProject.name = "$modName-$minecraftVersion-Forge"
