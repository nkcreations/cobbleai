import java.net.URI

pluginManagement {
    repositories {
        maven { url = URI("https://maven.fabricmc.net/") }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "CobblemonSideMod"
