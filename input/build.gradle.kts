plugins {
    id("java-library")
    id("jacoco")
}

version = "1.0-SNAPSHOT"

dependencies {
    api(project(":system"))
    api(project(":event"))
    implementation("com.github.zafarkhaja:java-semver:0.10.2")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}
