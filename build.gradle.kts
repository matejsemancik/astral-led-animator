import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
    application
}

group = "dev.matsem"
version = "1.0-SNAPSHOT"

val props = org.jetbrains.kotlin.konan.properties.Properties().apply { load(file("local.properties").inputStream()) }
val processingCoreDir = props["processingCoreDir"]
val processingLibsDir = props["processingLibsDir"]
val processingLibs = listOf(
    "minim",
    "themidibus",
    "artnet4j",
    "ControlP5"
)

application {
    mainClassName = "dev.matsem.ala.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(fileTree(mapOf("dir" to processingCoreDir, "include" to listOf("*.jar"))))
    processingLibs.forEach { libName ->
        implementation(fileTree(mapOf("dir" to "$processingLibsDir/$libName/library", "include" to listOf("*.jar"))))
    }

    implementation(kotlin("reflect"))
    implementation(kotlin("script-runtime"))
    implementation(kotlin("script-util"))
    implementation(kotlin("compiler-embeddable"))
    implementation(kotlin("scripting-compiler-embeddable"))
    implementation("org.slf4j:slf4j-api:1.7.14")
    implementation("ch.qos.logback:logback-classic:1.1.3")
    implementation("net.java.dev.jna:jna:4.2.2")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}