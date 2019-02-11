import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.3.11"

    java
    kotlin("jvm") version kotlinVersion
    `maven-publish`
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion
    id("org.springframework.boot") version "2.1.2.RELEASE" apply false
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("org.jetbrains.dokka") version "0.9.17"
    id("com.jfrog.bintray") version "1.8.4"
}

group = "com.ninja-squad"
version = "1.1.1-SNAPSHOT"
description = "MockBean and SpyBean, but for MockK instead of Mockito"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
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

    jar {
        manifest(sharedManifest)
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
        manifest(sharedManifest)
    }

    register<Jar>("javadocJar") {
        dependsOn(javadoc)
        archiveClassifier.set("javadoc")
        from(javadoc.get().destinationDir)
        manifest(sharedManifest)
    }
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES) {
            bomProperty("kotlin.version", "1.3.11")
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
        exclude(module = "mockito-core")
    }
    implementation("io.mockk:mockk:1.9.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

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
            url = uri("$buildDir/repo")
        }
    }
}

bintray {
    user = "ninjasquad"
    key = project.findProperty("bintray.key") as String?
    setPublications("maven")
    publish = true
    pkg = PackageConfig().apply {
        repo = "maven"
        name = project.name
        desc = project.description
        websiteUrl = "https://github.com/Ninja-Squad/springmockk"
        issueTrackerUrl = "https://github.com/Ninja-Squad/springmockk/issues"
        vcsUrl = "https://github.com/Ninja-Squad/springmockk"
        setLicenses("Apache-2.0")
        version = VersionConfig().apply {
            gpg = GpgConfig().apply {
                sign = true
                passphrase = project.findProperty("signing.password") as String?
            }
            mavenCentralSync = MavenCentralSyncConfig().apply {
                sync = true
                user = project.findProperty("sonatypeUsername") as String?
                password = project.findProperty("sonatypePassword") as String?
            }
        }
    }
}
