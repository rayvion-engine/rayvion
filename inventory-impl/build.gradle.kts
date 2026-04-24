plugins {
    id("java-library")
    id("jacoco")
}

version = "1.0-SNAPSHOT"

dependencies {
    api(project(":inventory"))
    api(project(":event"))
    implementation(project(":world"))
    implementation(project(":transform"))
    implementation(project(":audio"))
    implementation(project(":characteristic"))
    implementation("org.slf4j:slf4j-api:2.0.12")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(project(":event-impl"))
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}
