import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka") version "1.9.20"
}

repositories {
    mavenCentral()
}

mavenPublishing {
    configure(
        KotlinJvm(
            javadocJar = JavadocJar.Dokka("dokkaHtml"),
            sourcesJar = true,
        ),
    )
}

dependencies {
    api(project(":fal-client-async"))
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.test {
    useJUnitPlatform()
}

afterEvaluate {
    when {
        plugins.hasPlugin("org.jetbrains.kotlin.jvm") -> {
            tasks.named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml") {
                doLast {
                    copy {
                        from(outputDirectory)
                        into(rootProject.projectDir.resolve("docs/${project.name}"))
                    }
                }
            }
        }
    }
}
