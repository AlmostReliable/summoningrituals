pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
}

val modName = extra.get("modName").toString().replace(" ", "-")
val mcVersion: String by extra
rootProject.name = "$modName-$mcVersion-Fabric"
