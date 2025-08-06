import java.net.URI

plugins {
    id("fabric-loom") version "1.5.+"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven { url = URI("https://maven.fabricmc.net/") }
    maven { url = URI("https://curse.maven.zeker.zed") }
}

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.14:v2")
    modImplementation("net.fabricmc:fabric-loader:0.15.3")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.95.6+1.20.1")

    // Cobblemon 1.5.2 (Fabric) from Curse Maven
    modImplementation("curse.maven:cobblemon-687131:5375408")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
