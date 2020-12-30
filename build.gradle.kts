import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.ninjasquad.gradle.MavenSyncTask

plugins {
    val kotlinVersion = "1.4.10"

    `java-library`
    kotlin("jvm") version kotlinVersion
    `maven-publish`
    signing
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion
    id("org.springframework.boot") version "2.4.0" apply false
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    id("org.jetbrains.dokka") version "0.10.1"
}

group = "com.ninja-squad"
version = "3.0.1"
description = "MockBean and SpyBean, but for MockK instead of Mockito"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

val sharedManifest = Action<Manifest> {
    attributes(
        "Implementation-Title" to project.name,
        "Implementation-Version" to project.version,
        "Implementation-Vendor" to "ninja-squad.com"
    )
}

val bintrayUser = "ninjasquad"
val bintrayRepo = "maven"
val bintrayPackage = project.name
val bintrayKey = project.findProperty("bintray.key")?.toString() ?: ""

tasks {
    withType(KotlinCompile::class.java) {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=enable")
            jvmTarget = "1.8"
        }
    }

    test {
        useJUnitPlatform()
    }

    withType<Jar> {
        manifest(sharedManifest)
    }

    register("syncToMavenCentral", MavenSyncTask::class) {
        mustRunAfter("publishMavenPublicationToBintrayRepository")
        group = "publishing"
        description = "Syncs to Maven Central"

        sonatypeUsername = project.findProperty("sonatypeUsername")?.toString() ?: ""
        sonatypePassword = project.findProperty("sonatypePassword")?.toString() ?: ""
        bintrayUsername = bintrayUser
        bintrayPassword = bintrayKey
        bintrayRepoName = bintrayRepo
        bintrayPackageName = bintrayPackage
    }

    register("publishAndSyncToMavenCentral", MavenSyncTask::class) {
        group = "publishing"
        description = "Publishes to Bintray, then syncs to Maven Central"

        dependsOn("publishMavenPublicationToBintrayRepository")
        dependsOn("syncToMavenCentral")
    }
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES) {
            bomProperty("kotlin.version", "1.4.10")
        }
    }
}

dependencies {
    api("io.mockk:mockk:1.10.2")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-test")
    implementation("org.springframework:spring-test")
    implementation("org.springframework:spring-context")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/Ninja-Squad/springmockk")
                organization {
                    name.set("Ninja Squad")
                    url.set("http://ninja-squad.com")
                }
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("jnizet")
                        name.set("Jean-Baptiste Nizet")
                        email.set("jb@ninja-squad.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Ninja-Squad/springmockk")
                    developerConnection.set("scm:git:git://github.com/Ninja-Squad/springmockk")
                    url.set("https://github.com/Ninja-Squad/springmockk")
                }
            }
        }
    }
    repositories {
        maven {
            name = "build"
            url = uri("$buildDir/repo")
        }
        maven {
            name = "bintray"
            url = uri("https://api.bintray.com/maven/$bintrayUser/$bintrayRepo/$bintrayPackage/;publish=1")
            credentials {
                username = bintrayUser
                password = bintrayKey
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
