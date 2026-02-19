plugins {
    java
    application
}

application {
    mainClass.set("com.hansenjc.Main")
}

group = "com.hansenjc"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.jetbrains:annotations:16.0.2")
    implementation("org.ejml:ejml-all:0.44.0")
}

tasks.test {
    useJUnitPlatform()
}