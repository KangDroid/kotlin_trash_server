import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    val springBootVersion = "2.1.7.RELEASE"

    repositories {
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
        classpath("org.jetbrains.kotlin:kotlin-allopen:1.4.21")
    }
}

plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.jpa") version "1.3.61"
    kotlin("plugin.allopen") version "1.4.21"
}

noArg {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

group = "me.kangdroid"
version = "1.0-SNAPSHOT"

apply {
    plugin("kotlin-spring")
    plugin("org.springframework.boot")
    plugin("io.spring.dependency-management")
}

repositories {
    mavenCentral()
}

dependencies {
    compile("org.springframework.boot:spring-boot-starter-web")
    compile("org.springframework.boot:spring-boot-starter-data-jpa")
    compile("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    runtimeOnly("mysql:mysql-connector-java")
    testCompile("org.springframework.security:spring-security-test")
    testCompile("org.springframework.boot:spring-boot-starter-test")
    implementation("com.h2database:h2")
    testImplementation(kotlin("test-junit"))
}

tasks.test {
    systemProperty("kdr.isTesting", "test")
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}