import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

application {
    this.mainClassName = "com.mtw.supplier.client.Main"
}

java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compile(project(":engine"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.50")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.50")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.13.0") // JVM dependency
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
    implementation("io.github.rybalkinsd:kohttp:0.11.1")
    implementation("org.hexworks.zircon:zircon.core-jvm:2020.1.4-HOTFIX")
    implementation("org.hexworks.zircon:zircon.jvm.swing:2020.1.4-HOTFIX")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
