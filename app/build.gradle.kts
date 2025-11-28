plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.liuxing.daily"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.liuxing.daily"
        minSdk = 26
        targetSdk = 36
        versionCode = 22
        versionName = "2.9.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }
    viewBinding {
        enable = true
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // Navigation
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)

    // Gson
    implementation(libs.gson)

    // OkHttp
    implementation(libs.okhttp)

    // Glide
    implementation(libs.glide)

    // PhotoView
    implementation(libs.photoview)

    // subsampling-scale-image-view
    implementation(libs.davemorrissey.subsampling.scale.image.view)

    // sardine-android
    implementation(libs.sardine.android)

    // zip4j
    implementation(libs.zip4j)

    // ColorPickerView
    implementation(libs.colorpickerview)

    // palette
    implementation("androidx.palette:palette-ktx:1.0.0")
}
