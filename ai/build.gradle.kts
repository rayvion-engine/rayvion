plugins {
    id("java-library")
}

version = "1.0-SNAPSHOT"

dependencies {
    api(project(":system"))
    api(project(":commons"))
}
