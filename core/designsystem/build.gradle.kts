plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "app.coshell.core.designsystem"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    lint {
        ignoreTestSources = true // no tests to ship; saves the lintAnalyze*Test tasks
    }

    testOptions {
        // Screenshot tests (Robolectric, NATIVE graphics) render the real fonts, icons and themes.
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
            it.filter.isFailOnNoMatchingTests = false // `agent.sh shots <filter>` runs both modules
        }
    }
}

// Screenshot references live next to the tests and are compared in every `check` (gradle.properties:
// roborazzi.test.verify). Re-record after a deliberate visual change: `scripts/agent/agent.sh shots`.
roborazzi {
    outputDir.set(file("src/test/screenshots"))
}

dependencies {
    api(platform(libs.compose.bom))
    api(libs.compose.ui)
    api(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    api(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
