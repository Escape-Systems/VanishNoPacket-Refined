import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

group = "org.kitteh"
version = "3.22-SNAPSHOT"
description = "VanishNoPacket-Refined"


plugins {
    `java-library`
    `maven-publish`
    idea
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.0"
    id("com.gradleup.shadow") version "9.0.0-rc1"
}

repositories {
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://repo.helpch.at/releases")
    maven("https://nexus.scarsz.me/content/groups/public/")
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://jitpack.io")
}

dependencies {

    testImplementation("junit:junit:4.13.2")
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
    implementation("net.essentialsx:EssentialsX:2.21.1") {
        exclude("org.spigotmc")
    }
    compileOnly("com.github.milkbowl:vaultapi:1.7") {
        exclude("org.bukkit")
    }
    implementation("me.clip:placeholderapi:2.11.6") {
        exclude("me.clip.placeholderapi.libs.kyori")
    }
    compileOnly("com.discordsrv:discordsrv:1.29.0") {
        exclude("github.scarsz.discordsrv.dependencies.kyori")
    }
    compileOnly("xyz.jpenilla:squaremap-api:1.3.7")
}

java.sourceCompatibility = JavaVersion.VERSION_21
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.shadowJar {
    minimize()
    archiveFileName.set("${project.name}-${project.version}.jar")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

paperPluginYaml {
    main = "org.kitteh.vanish.VanishPlugin"
    authors.add("Matt \"MBax\" Baxter")
    authors.add("Lexie \"Tech\" Malina")
    apiVersion = "1.21.4"
    version = project.version.toString()
    description = "Vanish for the high speed admin"
    foliaSupported = true

    dependencies {
        server("Essentials", PaperPluginYaml.Load.OMIT, false)
        server("PlaceholderAPI", PaperPluginYaml.Load.OMIT, false)
        server("Vault", PaperPluginYaml.Load.OMIT, false)
        server("squaremap", PaperPluginYaml.Load.OMIT, false)
        server("LuckPerms", PaperPluginYaml.Load.OMIT, false)
    }
}