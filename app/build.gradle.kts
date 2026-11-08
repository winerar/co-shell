plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "app.coshell"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.coshell"
        minSdk = 26
        targetSdk = 37
        // CI passes its run number so every build is newer than the previous one (needed for updates).
        versionCode = providers.environmentVariable("BUILD_NUMBER").orNull?.toInt() ?: 1
        versionName = "0.1.0"
        buildConfigField("String", "UPDATE_BASE_URL", "\"\"")
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
            // Only release builds update themselves; the URL comes from CI (Firebase Hosting site).
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
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
