plugins {
    kotlin("jvm") version "2.1.21" apply false
    id("io.ktor.plugin") version "3.2.2" apply false
}

allprojects {
    group = "net.keyfc"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}