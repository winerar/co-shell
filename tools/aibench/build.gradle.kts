// Standalone tool (not part of the app's Gradle build): measures on-device models on the phone (#587).
// Build: scripts/agent/agent.sh aibench  (= gradle -p tools/aibench assembleDebug).
plugins {
    id("com.android.application") version "9.5.1"
    // Only to put Kotlin 2.4.x on the build classpath (the library is compiled with 2.4; AGP alone brings 2.2).
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
}

android {
    namespace = "app.coshell.aibench"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.coshell.aibench"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1"
    }

    signingConfigs {
        // Same shared debug key as the app: not a secret, debug builds only.
        getByName("debug") {
            storeFile = file("../../app/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    sourceSets["main"].assets.directories.add("../../docs/samples")
    // The app's own code (#742): the bench measures what «Обработать» and «Прочитать с ИИ» do, not a separate prompt.
    sourceSets["main"].kotlin.directories.addAll(
        listOf("../../core/model/src/commonMain/kotlin", "../../core/domain/src/commonMain/kotlin", "../ai-eval/src"),
    )

    packaging { jniLibs.useLegacyPackaging = true }
}

dependencies {
    implementation("com.google.ai.edge.litertlm:litertlm-android:0.19.1")
    // What core:model and core:domain need (same versions as gradle/libs.versions.toml).
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
}
