import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
	id("org.springframework.boot") version "4.2.0"
	kotlin("jvm") version "2.4.21"
	kotlin("plugin.spring") version "2.4.21"
}

group = "org.stream.bot"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

val telegramBotsVersion = "10.4.0"

dependencies {
	implementation(platform(SpringBootPlugin.BOM_COORDINATES))

	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	implementation("org.telegram:telegrambots-abilities:$telegramBotsVersion")
	implementation("org.telegram:telegrambots-client:$telegramBotsVersion")
	implementation("org.telegram:telegrambots-springboot-longpolling-starter:$telegramBotsVersion")

	implementation("org.apache.pdfbox:pdfbox:3.0.8")
	implementation("org.apache.tika:tika-core:3.4.0")
	implementation("org.apache.tika:tika-parsers-standard-package:3.4.0")
	implementation("commons-io:commons-io:2.22.0")

	//dependencies for google drive
	implementation("com.google.api-client:google-api-client:2.9.2")
	implementation("com.google.apis:google-api-services-drive:v3-rev20261106-2.9.2")
	implementation("com.google.auth:google-auth-library-oauth2-http:1.58.0")
	implementation("com.google.http-client:google-http-client-gson:2.2.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
		jvmTarget = JvmTarget.JVM_25
	}
}

tasks.withType<Test> {
	// "integration" tests need a real MongoDB, bot token and Telegram/Google access
	useJUnitPlatform {
		excludeTags("integration")
	}
	// Mockito attaches its inline mock maker as an agent at runtime
	jvmArgs("-XX:+EnableDynamicAgentLoading")
}
