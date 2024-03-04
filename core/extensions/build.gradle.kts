plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "net.c306.photopress.core.extensions"
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.fragment.ktx)
}