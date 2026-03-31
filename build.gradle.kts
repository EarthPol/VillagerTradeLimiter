plugins {
    `java-library`
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "9.2.1"
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") // Paper Repository
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot Repo
    maven("https://repo.codemc.io/repository/maven-public/") // Codemc Repo
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    
    // External dependencies
    implementation("de.tr7zw:item-nbt-api:2.15.6")
    compileOnly("de.tr7zw:functional-annotations:0.1-SNAPSHOT")
    compileOnly("mysql:mysql-connector-java:8.0.33")
    compileOnly("org.xerial:sqlite-jdbc:3.40.1.0")

    // Test dependencies (optional)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

group = "com.pretzel.dev"
version = "1.6.7"
description = "VillagerTradeLimiter"

tasks {
    runServer {
        minecraftVersion("1.21.11")
        downloadPlugins {
            url("https://github.com/Test-Account666/PlugManX/releases/download/2.4.1/PlugManX-2.4.1.jar")
        }
    }

    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("de.tr7zw.changeme.nbtapi", "com.pretzel.dev.villagertradelimiter.lib.nbtapi")
    }

    assemble {
        dependsOn(shadowJar)
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
