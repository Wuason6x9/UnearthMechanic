import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import java.net.URL
import org.jetbrains.dokka.Platform


plugins {
    java
    kotlin("jvm") version "2.0.20-RC2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.gradle.maven-publish")
    id("org.jetbrains.dokka") version "1.9.20"
}

val targetJavaVersion = 21

allprojects {

    project.group = "dev.wuason"
    project.version = "0.1.10"

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
        maven("https://repo.nexomc.com/snapshots/") {
            name = "nexo-snapshots"
        }
        maven("https://repo.nexomc.com/releases/") {
            name = "nexo-releases"
        }
    }

    kotlin {
        jvmToolchain(targetJavaVersion)
    }

    dependencies {
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")
    }

    tasks {

        compileJava {
            options.encoding = "UTF-8"
            dependsOn(clean)
        }

    }

}

project(":api") {

    apply(plugin = "org.jetbrains.dokka")

    tasks.jar {
        archiveFileName.set("UnearthMechanic-api-${project.version}.jar")
        archiveClassifier.set("")
        destinationDirectory.set(file("../target"))
    }

    tasks.jar {
        dependsOn("dokkaJavadocJar")
    }

    tasks.register<Jar>("dokkaJavadocJar") {
        dependsOn(tasks.dokkaJavadoc)
        from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
        destinationDirectory.set(file("../target"))
        archiveFileName = "javadoc.jar"
        archiveClassifier.set("javadoc")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                groupId = rootProject.group.toString()
                artifactId = rootProject.name
                version = rootProject.version.toString()
                artifact(tasks.jar)
                artifact(tasks.getByName("dokkaJavadocJar"))
            }
        }
    }

    tasks.withType<DokkaTask>() {
        moduleName.set(rootProject.name)
        moduleVersion.set(project.version.toString())
        outputDirectory.set(layout.buildDirectory.dir("dokka/$name"))
        failOnWarning.set(false)
        suppressObviousFunctions.set(false)
        suppressInheritedMembers.set(false)
        offlineMode.set(false)
        dokkaSourceSets {
            configureEach {
                suppress.set(false)
                documentedVisibilities.set(
                    setOf(
                        Visibility.PUBLIC
                    )
                )
                skipDeprecated.set(false)
                reportUndocumented.set(true)
                skipEmptyPackages.set(true)
                jdkVersion.set(17)
                noStdlibLink.set(false)
                noJdkLink.set(false)
                languageVersion.set("1.7")
                apiVersion.set("1.7")
                displayName.set(name)
                sourceRoots.from(file("src/main/kotlin"))
                suppressGeneratedFiles.set(true)

                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(URL("https://github.com/Wuason6x9/UnearthMechanic/tree/master/api/src/main/kotlin/"
                    ))
                    remoteLineSuffix.set("#L")
                }

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
        compileOnly("com.github.Wuason6x9:mechanics:1.0.2")
        compileOnly("io.th0rgal:oraxen:1.178.0") // 1.174.0 supported version
        compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.3-beta-14")
        compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
        compileOnly("com.nexomc:nexo:0.4.0:dev")
    }
}

dependencies {
    implementation(project(":api"))
    implementation(project(":core"))
}

tasks.shadowJar {
    archiveFileName.set("UnearthMechanic-${project.version}.jar")
    archiveClassifier.set("")
    destinationDirectory.set(file("target"))
}

tasks {

    build {
        dependsOn("shadowJar")
    }

}

gradle.projectsEvaluated {

}
val file = file("readme.md")
gradle.projectsEvaluated {
    val content = file.readText()
    if (content.contains("UnearthMechanic/${project.version}/javadoc")) return@projectsEvaluated
    val newContent = content.replace(Regex("(?<=UnearthMechanic/)(.*?)(?=/javadoc)"), "${project.version}")
    file.writeText(newContent)
    println("Readme.md updated")
}

