import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.61"
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
    "artnet4j"
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