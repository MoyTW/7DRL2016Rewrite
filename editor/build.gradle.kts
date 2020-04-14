import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.50"
}

group = "com.mtw"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val developmentOnly by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(developmentOnly)
    }
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compile(project(":server"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.50")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.13.0") // JVM dependency
    implementation("io.github.rybalkinsd:kohttp:0.11.1")
    implementation("org.hexworks.zircon:zircon.core-jvm:2020.1.1-PREVIEW")
    implementation("org.hexworks.zircon:zircon.jvm.swing:2020.1.1-PREVIEW")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
