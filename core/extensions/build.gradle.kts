plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "net.c306.photopress.core.extensions"
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.fragment.ktx)
}