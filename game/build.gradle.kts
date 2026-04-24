plugins {
    id("java")
    id("application")
}

application {
    mainClass.set("com.rayvion.game.DesktopLauncher")
}

version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":commons"))
    implementation(project(":system"))
    implementation(project(":system-manager"))
    implementation(project(":entity"))
    implementation(project(":entity-impl"))
    implementation(project(":event"))
    implementation(project(":event-impl"))
    implementation(project(":input"))
    implementation(project(":bindings"))
    implementation(project(":bindings-impl"))
    implementation(project(":scheduler"))
    implementation(project(":scheduler-impl"))
    implementation(project(":tick"))
    implementation(project(":tick-impl"))
    implementation(project(":world"))
    implementation(project(":world-impl"))
    implementation(project(":transform"))
    implementation(project(":transform-impl"))
    implementation(project(":physics"))
    implementation(project(":physics-impl"))
    implementation(project(":graphics"))
    implementation(project(":graphics-impl"))
    implementation(project(":ai"))
    implementation(project(":ai-impl"))
    implementation(project(":inventory"))
    implementation(project(":inventory-impl"))
    implementation(project(":equipment"))
    implementation(project(":equipment-impl"))
    implementation(project(":quest"))
    implementation(project(":quest-impl"))
    implementation(project(":characteristic"))
    implementation(project(":characteristic-impl"))
    implementation(project(":audio"))

    val gdxVersion = "1.12.1"
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    runtimeOnly("ch.qos.logback:logback-classic:1.5.3")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("com.badlogicgames.gdx:gdx-backend-headless:$gdxVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}


