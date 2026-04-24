plugins {
    id("java-library")
}

version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":ai"))
    implementation(project(":tick"))
    implementation(project(":system-manager"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

