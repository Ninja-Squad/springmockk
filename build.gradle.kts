import java.time.Duration

plugins {
    // if it's changed, it must also be channged in the bomProperty below
    val kotlinVersion = "2.2.21"

    `java-library`
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

group = "com.ninja-squad"
version = "5.0.0"
description = "MockitoBean and MockitoSpyBean, but for MockK instead of Mockito"

val sonatypeUsername = project.findProperty("sonatypeUsername")?.toString() ?: ""
val sonatypePassword = project.findProperty("sonatypePassword")?.toString() ?: ""

java {
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

kotlin {
    jvmToolchain(17)

    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks {
    test {
        useJUnitPlatform()
        jvmArgs(
            "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
        )
    }

    withType<Jar> {
        manifest(sharedManifest)
    }

    register("publishToSonatypeAndClose") {
        group = "Maven Central Release"
        description = "Publishes to the Sonatype OSSRH repository and closes, but does not do the final release to Maven Central"

        dependsOn("publishToSonatype")
        dependsOn("closeSonatypeStagingRepository")
    }

    register("publishToSonatypeAndCloseAndReleaseToMavenCentral") {
        group = "Maven Central Release"
        description = "Publishes to the Sonatype OSSRH repository and closes, then does the final release to Maven Central"

        dependsOn("publishToSonatype")
        dependsOn("closeAndReleaseSonatypeStagingRepository")
    }
}

afterEvaluate {
    tasks.named("closeSonatypeStagingRepository") {
        mustRunAfter("publishToSonatype")
    }
    tasks.named("closeAndReleaseSonatypeStagingRepository") {
        mustRunAfter("publishToSonatype")
    }
}

val springVersion = "7.0.1"

dependencies {
    api("io.mockk:mockk-jvm:1.14.6")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework:spring-test:$springVersion")
    implementation("org.springframework:spring-context:$springVersion")

    testImplementation("org.assertj:assertj-core:3.27.6")
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:6.0.1")
    testImplementation("jakarta.annotation:jakarta.annotation-api:3.0.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.0.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.1")
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
                        organization.set("Ninja Squad")
                        organizationUrl.set("https://ninja-squad.com")
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
            url = uri("${project.layout.buildDirectory}/repo")
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))

            username.set(sonatypeUsername)
            password.set(sonatypePassword)
        }
    }
    connectTimeout.set(Duration.ofMinutes(3))
    clientTimeout.set(Duration.ofMinutes(3))
}
