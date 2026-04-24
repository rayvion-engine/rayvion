plugins {
    id("java-library")
    id("jacoco")
}

version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":bindings"))
    implementation(project(":event"))
    implementation(project(":input"))
    implementation(project(":commons"))
    implementation("org.slf4j:slf4j-api:2.0.16")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    testImplementation(project(":event-impl"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}
