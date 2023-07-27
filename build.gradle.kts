plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    `java-library`
}

group = "io.sn"
version = "0.1.2"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://jitpack.io/")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("dev.jorel:commandapi-bukkit-core:9.0.3")
    compileOnly("com.github.HAPPYLAND-Dev:Slimefun4:00f2f4bf63")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.5.0b")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(
            "version" to project.version
        )
    }
}
