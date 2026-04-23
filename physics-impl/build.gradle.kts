plugins {
    id("java")
    id("jacoco")
}

version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":physics"))
    implementation("org.dyn4j:dyn4j:5.0.2")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}
