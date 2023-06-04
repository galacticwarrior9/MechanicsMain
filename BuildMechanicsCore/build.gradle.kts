import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

description = "Library plugin for WeaponMechanics"
version = "2.3.0"

plugins {
    `maven-publish`
    id("me.deecaad.java-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    kotlin("jvm") version "1.7.20-RC"
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    implementation(project(":MechanicsCore"))
    implementation(project(":CoreCompatibility"))
    implementation(project(":WorldGuardV6"))
    implementation(project(":WorldGuardV7"))
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":Core_1_19_R3", "reobf"))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17) // We need to set release compatibility to java 17 since MC 18+ uses it
    }
}

// See https://github.com/Minecrell/plugin-yml
bukkit {
    main = "me.deecaad.core.MechanicsCore"
    name = "MechanicsCore" // Since we don't want to use "BuildMechanicsCore"
    apiVersion = "1.13"

    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    authors = listOf("DeeCaaD", "CJCrafter")
    loadBefore = listOf("WorldEdit", "WorldGuard", "PlaceholderAPI", "MythicMobs")
}

tasks.named<ShadowJar>("shadowJar") {

    destinationDirectory.set(file("../build"))
    archiveFileName.set("MechanicsCore-${version}.jar")
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    dependencies {
        include(project(":MechanicsCore"))
        include(project(":CoreCompatibility"))
        include(project(":WorldGuardV6"))
        include(project(":WorldGuardV7"))

        include(project(":Core_1_19_R3"))

        include(dependency("org.jetbrains.kotlin:"))
        //relocate ("kotlin.", "me.deecaad.core.lib.kotlin.") {
        //    include(dependency("org.jetbrains.kotlin:"))
        //}

        relocate ("net.kyori", "me.deecaad.core.lib") {
            include(dependency("net.kyori::"))
        }

        relocate ("com.zaxxer.hikari", "me.deecaad.core.lib.hikari") {
            include(dependency("com.zaxxer::"))
        }

        relocate ("org.slf4j", "me.deecaad.core.lib.slf4j") {
            include(dependency("org.slf4j::"))
        }
    }

    doFirst {
        println("Compile MechanicsCore")
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/WeaponMechanics/MechanicsMain")
            credentials {
                username = findProperty("user").toString()
                password = findProperty("pass").toString()
            }
        }
    }
    publications {
        create<MavenPublication>("corePublication") {
            artifact(tasks.named("shadowJar")) {
                classifier = null
            }

            pom {
                groupId = "me.deecaad"
                artifactId = "mechanicscore" // MUST be lowercase
                packaging = "jar"
            }
        }
    }
}