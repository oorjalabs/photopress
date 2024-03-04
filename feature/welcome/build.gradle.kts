plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "net.c306.photopress.welcome"

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    implementation(project(path = ":core:designSystem"))

    implementation(libs.androidx.fragment.ktx)
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Choose one of the following:
    // Material Design 3
    implementation(libs.androidx.compose.material3)
    // or only import the main APIs for the underlying toolkit systems,
    // such as input and measurement/layout
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.util)

    // Android Studio Preview support
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // UI Tests
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Included automatically by material, only add when you need
    // the icons but not the material library (e.g. when using Material3 or a
    // custom design system based on Foundation)
    implementation(libs.androidx.compose.material.iconsCore)
    // Add full set of material icons
    implementation(libs.androidx.compose.material.iconsExtended)
    // Add window size utils
    implementation(libs.androidx.compose.material3.windowSizeClass)

    // Integration with activities
    implementation(libs.androidx.activity.compose)
    // Integration with ViewModels
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Integration with LiveData
    implementation(libs.androidx.compose.runtime.livedata)
}