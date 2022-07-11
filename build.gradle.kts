import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenCentral()
}

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.6.21"

    id("com.github.ldenisey.setversions") version "1.0.1"
    id("org.jetbrains.changelog") version "1.3.1"

    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.21.0"
}

group = "com.github.ldenisey"
version = "1.0.2-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.7.10")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter")
    testImplementation(gradleTestKit())
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("SetVersionsPlugin") {
            id = "com.github.ldenisey.setversions"
            displayName = "Gradle Set Versions Plugin"
            description = "Gradle plugin that provides tasks to modify project and modules versions."
            implementationClass = "com.github.ldenisey.setversions.SetVersionsPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/ldenisey/gradle-setversions-plugin"
    vcsUrl = "https://github.com/ldenisey/gradle-setversions-plugin"
    tags = listOf(
        "version",
        "versioning",
        "update",
        "set",
        "increment",
        "prefix",
        "suffix",
        "ci",
        "devops",
        "continuous integration"
    )
}

// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set("${project.version}")
}
