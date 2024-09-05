import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.diffplug.spotless") version "6.25.0"
    id("com.vanniktech.maven.publish") version "0.29.0"
    kotlin("jvm") version "1.9.25" apply false
}

subprojects {
    group = "ai.fal.client"
    version = "0.7.1-SNAPSHOT"

    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "com.vanniktech.maven.publish")

    spotless {
        java {
            palantirJavaFormat()
        }
        kotlin {
            ktlint()
        }
        kotlinGradle {
          target("*.gradle.kts")
          ktlint()
        }
    }

    mavenPublishing {
        publishToMavenCentral(SonatypeHost.DEFAULT, automaticRelease = true)
        signAllPublications()
        pom {
            name.set("Fal Client Library")
            description.set("A Client library for fal.ai APIs")
            inceptionYear.set("2024")
            url.set("https://github.com/fal-ai/fal-java")
            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/license/mit")
                }
            }
            developers {
                developer {
                    id.set("fal")
                    name.set("Fal AI")
                    email.set("developers@fal.ai")
                    url.set("https://github.com/fal-ai")
                }
            }
            scm {
                url.set("https://github.com/fal-ai/fal-java")
                connection.set("scm:git:git://github.com/fal-ai/fal-java.git")
            }
        }
    }

    tasks.withType<Javadoc> {
        // Disable empty javadoc warnings
        (options as CoreJavadocOptions).addBooleanOption("Xdoclint:none", true)
    }

}
