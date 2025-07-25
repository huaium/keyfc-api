plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("com.fleeksoft.ksoup:ksoup:0.2.4")
    implementation("com.fleeksoft.ksoup:ksoup-network:0.2.4")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("ch.qos.logback:logback-classic:1.5.18") // to suppress warnings of Ktor
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}