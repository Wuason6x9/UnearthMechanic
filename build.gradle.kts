plugins {
    kotlin("jvm") version "2.0.20-RC2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.gradle.maven-publish")
}

val targetJavaVersion = 21

allprojects {

    project.group = "dev.wuason"
    project.version = "0.1.9"

    //apply kotlin jvm plugin
    apply(plugin = "kotlin")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "org.gradle.maven-publish")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "papermc-repo"
        }
        maven("https://oss.sonatype.org/content/groups/public/") {
            name = "sonatype"
        }
        maven("https://jitpack.io") {
            name = "jitpack"
        }
        maven("https://repo.oraxen.com/releases") {
            name = "oraxen"
        }
        maven("https://maven.enginehub.org/repo/") {
            name = "enginehub"
        }
    }

    kotlin {
        jvmToolchain(targetJavaVersion)
    }

}

project(":api") {
    tasks.jar {
        archiveFileName.set("UnearthMechanic-api-${project.version}.jar")
        archiveClassifier.set("")
        destinationDirectory.set(file("../target"))
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                groupId = rootProject.group.toString()
                artifactId = rootProject.name
                version = rootProject.version.toString()
                artifact(tasks.jar)
            }
        }
    }
}

project(":core") {
    tasks.processResources {
        val props = mapOf("version" to rootProject.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    dependencies {
        compileOnly(project(":api"))
    }
}

subprojects {
    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
        compileOnly("com.github.Wuason6x9:mechanics:1.0.1.12a")
        compileOnly("io.th0rgal:oraxen:1.178.0") // 1.174.0 supported version
        compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.3-beta-14")
        compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(project(":api"))
    implementation(project(":core"))
}

tasks.shadowJar {
    archiveFileName.set("UnearthMechanic-${project.version}.jar")
    archiveClassifier.set("")
    destinationDirectory.set(file("target"))
}

tasks.build {
    dependsOn("shadowJar")
}


