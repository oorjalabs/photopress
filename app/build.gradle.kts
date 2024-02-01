import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
}


val apiKeyProperties = Properties()
val apiKeyPropertiesFile: File = rootProject.file("apikey.properties")
if (apiKeyPropertiesFile.exists()) {
    apiKeyProperties.load(FileInputStream(apiKeyPropertiesFile))
}
val envWpClientId = System.getenv("WP_CLIENT_ID") ?: ""
val envWpClientSecret = System.getenv("WP_CLIENT_SECRET") ?: ""


val versionString = "0.14.1"
val versionNumber = 5036
val showWhatsNew = "false"

fun getDate(): String = SimpleDateFormat("yyyy.MMdd").format(Date())

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "net.c306.photopress"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = versionNumber
        versionName = "${getDate()}.${versionString}"

        // should correspond to key/value pairs inside the file
        buildConfigField(
            type = "String",
            name = "WP_ID",
            value = apiKeyProperties["CLIENT_ID"] as? String ?: envWpClientId,
        )
        buildConfigField(
            type = "String",
            name = "WP_SECRET",
            value = apiKeyProperties["CLIENT_SECRET"] as? String ?: envWpClientSecret,
        )

        buildConfigField(
            type = "boolean",
            name = "SHOW_WHATS_NEW",
            value = showWhatsNew,
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//        javaCompileOptions {
//            annotationProcessorOptions {
//                arguments = [
//                        // Export schema location
//                        "room.schemaLocation"  : "$projectDir/schemas".toString(),
//                        // Incremental processing annotations
//                        "room.incremental"     : "true"
//                ]
//            }
//        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
//            isMinifyEnabled true
//            isShrinkResources true
            isMinifyEnabled = true
            isShrinkResources = false

            applicationIdSuffix = ".debug"

//            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "versionString"

    productFlavors {
        create("elsa") {
            versionName = "${getDate()}.${versionString}"
        }

        create("granny") {
            versionName = "${getDate()}.${versionString}-granny"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    }

    buildFeatures {
        viewBinding = true
    }
    namespace = "net.c306.photopress"

}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(fileTree(baseDir = "libs") { include("*.jar") })
    implementation(libs.kotlin.stdlib.jdk7)

    // Core
    implementation(libs.androidx.legacySupport)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)

    // Networking
    implementation(libs.okhttp)
    implementation(libs.okhttp.loggingInterceptor)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Images
    implementation(libs.glide)
//    annotationProcessor libs.compiler
    ksp(libs.glide.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Better logging
    implementation(libs.timber)

    // Kotlin co-routines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Material design
    implementation(libs.google.material)

    // Navigation component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Settings
    implementation(libs.androidx.preference.ktx)

    // Viewpager for welcome screen
    implementation(libs.androidx.viewpager2)
    implementation(libs.airbnb.lottie)

    // Room database
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.javax.annotation)
    // testImplementation libs.androidx.room.testing

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Photo view for viewing full image
    implementation(libs.cb.photoView)

    // My custom components
    implementation(libs.ab.customComponents)
}