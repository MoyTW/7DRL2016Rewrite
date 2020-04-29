plugins {
    kotlin("jvm") version "1.3.50" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.50" apply false
}

subprojects {
    group = "com.mtw.supplier"
    version = "0.0.1-SNAPSHOT"
}

allprojects {
    repositories {
        jcenter()
    }
}