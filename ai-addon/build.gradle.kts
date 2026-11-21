// The optional «ИИ на телефоне» add-on (#587): a separate APK with the LiteRT-LM engine (~20 MB, not in the app's 15 MB budget).
// A standalone Gradle project, like tools/aibench: not part of `check`. Build: scripts/agent/agent.sh gradle -p ai-addon assembleDebug.
// The app talks to it through `app/.../ai/AddonLanguageModel`; the protocol file is duplicated and kept equal by `AddonProtocolTest`.
plugins {
    id("com.android.application") version "9.5.1"
    // Only to put Kotlin 2.4.x on the build classpath (the library is compiled with 2.4; AGP alone brings 2.2).
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
}

android {
    namespace = "app.coshell.ai"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.coshell.ai"
        minSdk = 26
        targetSdk = 37
        versionCode = (System.getenv("BUILD_NUMBER") ?: "1").toInt()
        versionName = "0.1"
    }

    signingConfigs {
        // The same shared debug key as the app: the bind permission is signature-level, so both must be signed alike.
        // Not a secret, debug builds only; a release add-on is signed with the app's release key in CI.
        getByName("debug") {
            storeFile = file("../app/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        // The release add-on is signed with the app's release key (secrets decoded by the workflow): the bind permission is signature-level.
        val releaseStore = providers.environmentVariable("RELEASE_KEYSTORE_FILE").orNull
        if (releaseStore != null) {
            create("release") {
                storeFile = file(releaseStore)
                storePassword = providers.environmentVariable("RELEASE_KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD").get()
            }
        }
    }

    buildTypes {
        release {
            // No R8: the engine is native code plus a thin wrapper, and a shrinker risks its JNI names for little gain.
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
        }
    }

    packaging { jniLibs.useLegacyPackaging = true }
}

dependencies {
    implementation("com.google.ai.edge.litertlm:litertlm-android:0.19.1")
}
