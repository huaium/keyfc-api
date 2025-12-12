import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "net.keyfc"
version = "1.0.1"

kotlin {
    jvm()
    androidLibrary {
        namespace = "net.keyfc.api"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilerOptions {
            jvmTarget.set(
                JvmTarget.JVM_21
            )
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ksoup)
            implementation(libs.ksoup.network)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            api(libs.napier)
            implementation(libs.slf4j.simple) // to suppress warnings of Ktor
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            api(libs.napier)
            implementation(libs.slf4j.simple) // to suppress warnings of Ktor
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            api(libs.napier)
        }

        linuxX64Main.dependencies {
            implementation(libs.ktor.client.cio)
        }
    }
}

mavenPublishing {
    // only needed when publishing to Sonatype Maven Central
    // publishToMavenCentral()
    // signAllPublications()

    configure(
        com.vanniktech.maven.publish.KotlinMultiplatform(
            javadocJar = com.vanniktech.maven.publish.JavadocJar.Empty() // speeds up build
        )
    )

    coordinates(group.toString(), "api", version.toString())

    pom {
        name = "Keyfc-api"
        description =
            "A Kotlin Multiplatform library for parsing KeyFC pages into structured data classes."
        inceptionYear = "2025"
        url = "https://github.com/huaium/keyfc-api"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "repo"
            }
        }
    }
}
