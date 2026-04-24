plugins {
    id("java-library")
    id("jacoco")
}

version = "1.0-SNAPSHOT"

dependencies {
    api(project(":system"))
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}
