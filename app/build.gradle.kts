plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.roborazzi)
}

fun env(name: String): String = providers.environmentVariable(name).orNull.orEmpty()

android {
    namespace = "app.coshell"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.coshell"
        minSdk = 26
        targetSdk = 37
        // CI passes its run number so every build is newer than the previous one (needed for updates).
        versionCode = providers.environmentVariable("BUILD_NUMBER").orNull?.toInt() ?: 1
        // Release builds: 0.N, N = releases so far (scripts/ci/version_name.py, only on main); others — "0.0".
        versionName = providers.environmentVariable("VERSION_NAME").orNull ?: "0.0"
        buildConfigField("String", "UPDATE_BASE_URL", "\"\"")
        // Feedback form → Firestore (docs/setup/feedback.md). Not secrets: the Web API key is public
        // by design, access is limited by Firestore rules. Empty locally → the form says "not configured".
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${env("FIREBASE_PROJECT_ID")}\"")
        buildConfigField("String", "FIREBASE_API_KEY", "\"${env("FIREBASE_API_KEY")}\"")
        // Google sign-in for family sync (docs/setup/sync.md): OAuth "Web client" ID, public by design.
        // Empty → the «Семья» screen says sync is not configured in this build.
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${env("GOOGLE_WEB_CLIENT_ID")}\"")
        // Realtime Database for the instant-sync bell; empty → <project>-default-rtdb.firebaseio.com.
        buildConfigField("String", "FIREBASE_DATABASE_URL", "\"${env("FIREBASE_DATABASE_URL")}\"")
    }

    androidResources {
        // Only the app's languages (language/AppLanguage.SUPPORTED): libraries' other translations are
        // dropped — a smaller APK, and nothing can show up in a language the app doesn't speak.
        localeFilters += listOf("ru", "en", "es")
    }

    signingConfigs {
        // Shared debug key in the repo: APKs from any machine or CI run install over each other
        // without losing data. Not a secret — debug builds only; release has its own key (below).
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        // Release key lives only in GitHub Secrets (and the owner's backup); CI decodes it to a file
        // and passes these variables. Without them the release build stays unsigned.
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
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
            // Family phones are ARM: the bundled SQLite's x86 copies only served emulators (#264, −1 МБ).
            // `-PemulatorAbi=x86_64` (CI, emulator.yml): the same R8 build with the x86 copy, signed with the debug key.
            val emulatorAbi = providers.gradleProperty("emulatorAbi").orNull
            ndk { abiFilters += listOfNotNull("arm64-v8a", "armeabi-v7a", emulatorAbi) }
            if (emulatorAbi != null) signingConfig = signingConfigs.getByName("debug")
            // Only release builds update themselves; the URL comes from CI (public releases repo).
            val updateUrl = providers.environmentVariable("UPDATE_BASE_URL").orNull.orEmpty()
            buildConfigField("String", "UPDATE_BASE_URL", "\"$updateUrl\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        // Robolectric smoke tests (MainShellSmokeTest) read the app's strings and themes.
        unitTests.isIncludeAndroidResources = true
        // Robolectric reaches into JDK internals (FileDescriptor) — JDK 17+ must export them.
        unitTests.all {
            it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
            it.filter.isFailOnNoMatchingTests = false // `agent.sh shots <filter>` runs both modules
        }
    }

    lint {
        // Full lint runs in `check` on every PR; skipping lintVital keeps `assembleRelease` fast on main.
        checkReleaseBuilds = false
        ignoreTestSources = true // tests are not shipped; saves the lintAnalyze*Test tasks
        // Golden rule 9 (#377): text only in resources, in every language of AppLanguage.SUPPORTED.
        error += setOf("HardcodedText", "MissingTranslation", "ExtraTranslation", "MissingQuantity")
    }
}

// Compose stability (#383): classes from modules without the Compose compiler (core:model, collections)
// are declared stable in compose-stability.conf. `agent.sh stability` writes the compiler reports
// (-PcomposeReports) and fails on an unstable UiState.
composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose-stability.conf"))
    // Off by default: the build cache does not restore reports, so `agent.sh stability` recompiles with --rerun.
    if (providers.gradleProperty("composeReports").isPresent) {
        reportsDestination = layout.buildDirectory.dir("compose_reports")
    }
}

// Screenshot references of the key screens (#226), compared in every `check` like core:designsystem's.
// Re-record after a deliberate visual change: `scripts/agent/agent.sh shots`.
roborazzi {
    outputDir.set(file("src/test/screenshots"))
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime)
    // Receipt QR scanner (#526): own camera + ZXing (pure Java, ~0.5 MB; ML Kit pushed the release APK to 15.8 MB, over the 15 MB budget).
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.zxing.core)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
    implementation(libs.androidx.hilt.work)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
