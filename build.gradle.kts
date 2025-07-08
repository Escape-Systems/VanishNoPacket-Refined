group = "org.kitteh"
version = "3.22"
description = "VanishNoPacket-Refined"


plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

repositories {
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://repo.mikeprimm.com/")
    maven("https://repo.helpch.at/releases")
    maven("https://nexus.scarsz.me/content/groups/public/")
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://jitpack.io")
}

dependencies {

//    testImplementation(libs.junit.junit)
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
    compileOnly("us.dynmap:DynmapCoreAPI:3.7-beta-6")
    implementation("net.essentialsx:EssentialsX:2.21.1") {
        exclude("org.spigotmc")
    }
    compileOnly("com.github.milkbowl:vaultapi:1.7") {
        exclude("org.bukkit")
    }
    implementation("me.clip:placeholderapi:2.11.6")
    compileOnly("com.discordsrv:discordsrv:1.29.0")
    compileOnly("xyz.jpenilla:squaremap-api:1.3.7")
}

java.sourceCompatibility = JavaVersion.VERSION_21
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
