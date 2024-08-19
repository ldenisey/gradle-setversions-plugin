import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenCentral()
}

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.0.10"

    id("com.github.ldenisey.setversions") version "1.0.4"
    id("org.jetbrains.changelog") version "2.2.1"

    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = "com.github.ldenisey"
version = "1.0.5-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.0.10")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
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
    website = "https://github.com/ldenisey/gradle-setversions-plugin"
    vcsUrl = "https://github.com/ldenisey/gradle-setversions-plugin"
    plugins {
        create("SetVersionsPlugin") {
            id = "com.github.ldenisey.setversions"
            displayName = "Gradle Set Versions Plugin"
            description = "Gradle plugin that provides tasks to modify project and modules versions."
            implementationClass = "com.github.ldenisey.setversions.SetVersionsPlugin"
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
    }
}

// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set("${project.version}")
}
