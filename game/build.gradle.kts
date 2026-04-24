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

    val gdxVersion = "1.12.1"
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}


