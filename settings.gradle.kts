pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
}

val modName = extra.get("modName").toString().replace(" ", "-")
val minecraftVersion: String by extra
rootProject.name = "$modName-$minecraftVersion-Fabric"
