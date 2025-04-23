plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.sapirgo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sapirgo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth) // Firebase authentication - ONLY this one for auth
    implementation(libs.play.services.maps) // For Google Maps
    implementation(libs.firebase.ui.auth) // Firebase UI Auth - This includes Play Services Auth
    implementation (libs.play.services.location) // For Location Services
    implementation(libs.google.maps) // For Google Maps SDK
    implementation(libs.android.maps.utils)
    implementation(libs.volley) // For Google Maps Utils

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


}