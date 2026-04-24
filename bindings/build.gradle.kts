plugins {
    id("java-library")
    id("jacoco")
}

version = "1.0-SNAPSHOT"

dependencies {
    api(project(":system"))
    api(project(":event"))
    api(project(":input"))

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}
