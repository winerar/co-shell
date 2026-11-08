import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

// Pure Kotlin module: no Android or java.* APIs in commonMain.
// iOS targets (iosArm64, iosSimulatorArm64) are added here when an iOS app appears.
kotlin {
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
