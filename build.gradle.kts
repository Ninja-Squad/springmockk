import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Duration

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
    // see https://github.com/rwinch/gradle-publish-ossrh-sample for the release mechanism
    id("io.codearte.nexus-staging") version "0.22.0"
    id("de.marcphilipp.nexus-publish") version "0.4.0"
}

group = "com.ninja-squad"
version = "3.0.1"
description = "MockBean and SpyBean, but for MockK instead of Mockito"

val sonatypeUsername = project.findProperty("sonatypeUsername")?.toString() ?: ""
val sonatypePassword = project.findProperty("sonatypePassword")?.toString() ?: ""

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

    named("closeRepository") {
        mustRunAfter("publishToSonatype")
    }
    named("closeAndReleaseRepository") {
        mustRunAfter("publishToSonatype")
    }

    register("publishToSonatypeAndClose") {
        group = "Maven Central Release"
        description = "Published to the Sonatype OSSRH repository and closes, but does not do the final release to Maven Central"

        dependsOn("publishToSonatype")
        dependsOn("closeRepository")
    }

    register("publishToSonatypeAndCloseAndReleaseToMavenCentral") {
        group = "Maven Central Release"
        description = "Publishes to the Sonatype OSSRH repository and closes, then does the final release to Maven Central"

        dependsOn("publishToSonatype")
        dependsOn("closeAndReleaseRepository")
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
    }
}

signing {
    sign(publishing.publications["maven"])
}

nexusStaging {
    username = sonatypeUsername
    password = sonatypePassword
    repositoryDescription = "Release ${project.group} ${project.version}"
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(sonatypeUsername)
            password.set(sonatypePassword)
        }
    }
    connectTimeout.set(Duration.ofMinutes(3))
    clientTimeout.set(Duration.ofMinutes(3))
}
