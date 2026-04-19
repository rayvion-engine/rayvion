plugins {
    id("java-library")
}

version = "1.0-SNAPSHOT"

dependencies {
    api(project(":commons"))
    api(project(":system"))
}
