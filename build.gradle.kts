import io.papermc.hangarpublishplugin.model.Platforms
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
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
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
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
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
paperweight.reobfArtifactConfiguration =
    io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

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

hangarPublish {
    publications.register("plugin") {
        version = project.version.toString()
        channel.set("Snapshot")
        id.set("VanishNoPacket-Refined")
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))

        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                val versions: List<String> =
                    (property("paperVersion") as String).split(",").map { it.trim() }
                platformVersions.set(versions)

                dependencies {
                    hangar("Essentials") {
                        required.set(false)
                    }
                    hangar("PlaceholderAPI") {
                        required.set(false)
                    }
                    url("Vault", "https://www.spigotmc.org/resources/vault.34315/") {
                        required.set(false)
                    }
                    hangar("squaremap") {
                        required.set(false)
                    }
                    url("LuckPerms", "https://luckperms.net/download")
                }
            }
        }
    }
}

paperPluginYaml {
    main = "org.kitteh.vanish.VanishPlugin"
    authors.add("Matt \"MBax\" Baxter")
    authors.add("Lexie \"Tech\" Malina")
    apiVersion = "1.20.6"
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