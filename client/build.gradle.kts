plugins {
  `java-library`
  `maven-publish`
}

group = "ai.fal"
version = "1.0.0"

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11

  withSourcesJar()
  withJavadocJar()
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = "fal-client"
      from(components["java"])
    }
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
  implementation("com.google.code.gson:gson:2.11.0")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")

  compileOnly("org.projectlombok:lombok:1.18.34")
  annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
}

tasks.withType<Test> {
  useJUnitPlatform()
}
