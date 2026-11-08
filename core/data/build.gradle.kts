import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

// KMP module: Room entities, DAOs and repository implementations live in commonMain.
// The jvm target exists for fast database tests with the bundled SQLite driver (no emulator,
// no Robolectric). DI wiring is in :app (Hilt is Android-only).
kotlin {
    android {
        namespace = "app.coshell.core.data"
        compileSdk = 37
        minSdk = 26
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(libs.room.runtime)
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.sqlite.bundled)
        }
    }
}

// Exported schemas are committed: they are the baseline for migrations (docs/agents/database.md).
room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
}
