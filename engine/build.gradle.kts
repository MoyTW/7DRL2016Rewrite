import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	id("org.jetbrains.kotlin.plugin.serialization")
}

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
	implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.1.8.RELEASE")
	implementation("org.springframework.boot:spring-boot-starter-web:2.1.8.RELEASE")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.50")
	developmentOnly("org.springframework.boot:spring-boot-devtools:2.1.8.RELEASE")
	testImplementation("org.springframework.boot:spring-boot-starter-test:2.1.8.RELEASE")
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.50")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.13.0") // JVM dependency
	implementation("com.github.Hexworks.mixite:mixite.core-jvm:2018.2.0-RELEASE")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}
