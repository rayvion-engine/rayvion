plugins {
    id("java-library")
}

version = "1.0-SNAPSHOT"

dependencies {
    api(project(":tick"))
    api(project(":scheduler"))
    api(project(":system"))
    api(project(":commons"))
    
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
    
    testImplementation(project(":system-manager"))
    testImplementation(project(":scheduler-impl"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
