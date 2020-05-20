import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.2.6.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	kotlin("jvm") version "1.3.71"
	kotlin("plugin.spring") version "1.3.71"
}

group = "org.stream.bot"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}


repositories {
	mavenCentral()
	jcenter()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.3.4")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	implementation("org.telegram:telegrambots-abilities:4.7")
	implementation("org.telegram:telegrambots-spring-boot-starter:4.1.2")
	implementation("org.apache.pdfbox:pdfbox:2.0.19")
	//dependencies for google drive
	implementation("com.google.api-client:google-api-client:1.30.9"){
		exclude(group = "com.google.guava", module="guava-jdk5")
	}
	implementation("com.google.oauth-client:google-oauth-client-jetty:1.30.6")
	implementation("com.google.apis:google-api-services-drive:v3-rev197-1.25.0")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.apache.tika:tika-parsers:1.24.1")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}
