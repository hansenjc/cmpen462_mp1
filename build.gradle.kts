plugins {
    java
    application
}

application {
    mainClass.set("com.hansenjc.Main")
}

group = "com.hansenjc"
version = "1.0"

java {
    // Ensuring your project uses a specific Java version (e.g., 17 or 21)
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.jetbrains:annotations:16.0.2")
    implementation("org.ejml:ejml-all:0.44.0")
    implementation("com.github.wendykierp:JTransforms:3.2")
}

tasks.test {
    useJUnitPlatform()
}