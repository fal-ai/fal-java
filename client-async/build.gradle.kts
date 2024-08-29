plugins {
    `java-library`
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "fal-client-async"
            from(components["java"])
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":fal-client"))
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}
