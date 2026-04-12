plugins {
    id("application")
    id("java")
    id("jacoco")
}

version = "1.0-SNAPSHOT"

dependencies {
    val gdxVersion = "1.12.1"

    implementation(project(":system-manager"))
    implementation(project(":entity-impl"))

    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("org.dyn4j:dyn4j:4.2.2")

    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")

    implementation("org.jetbrains:annotations:26.1.0")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    implementation("org.apache.logging.log4j:log4j-api:2.25.4")
    implementation("org.apache.logging.log4j:log4j-core:2.25.4")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("eth.likespro.cyberpunkeb.DesktopLauncher")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}