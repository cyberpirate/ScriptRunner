plugins {
    kotlin("multiplatform")
}

group = "com.joe"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {

                implementation("org.jetbrains.kotlin:kotlin-scripting-common")
                implementation("org.jetbrains.kotlin:kotlin-scripting-jvm")
                implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies")
                implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies-maven")
                // coroutines dependency is required for this particular definition
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val jvmTest by getting
    }
}

//dependencies {
//    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
//}
//
//tasks.getByName<Test>("test") {
//    useJUnitPlatform()
//}