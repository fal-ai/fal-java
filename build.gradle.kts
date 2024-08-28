plugins {
    id("com.diffplug.spotless") version "6.25.0"
    id("com.vanniktech.maven.publish") version "0.29.0"
}

subprojects {
    group = "ai.fal.client"
    version = "0.7.0-SNAPSHOT"

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

    tasks.withType<Javadoc> {
        // Disable empty javadoc warnings
        (options as CoreJavadocOptions).addBooleanOption("Xdoclint:none", true)
    }

}
