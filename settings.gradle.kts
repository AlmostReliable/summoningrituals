val modName: String by extra
val minecraftVersion: String by extra
rootProject.name = "${modName.replace(" ", "-")}-$minecraftVersion-NeoForge"
