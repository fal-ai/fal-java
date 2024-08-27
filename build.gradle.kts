plugins {
    id("com.diffplug.spotless") version "6.25.0"
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    spotless {
        java {
            palantirJavaFormat()
        }
        kotlin {
            ktlint()
        }
        kotlinGradle {
          target("**/*.gradle.kts")
          ktlint()
        }
    }
}
