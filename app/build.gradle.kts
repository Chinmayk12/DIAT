plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.chinmay.diat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.chinmay.diat"
        minSdk = 25
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("com.google.firebase:firebase-firestore:24.10.3") //Firestore Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Import the BoM for the Firebase platform
    implementation("com.google.firebase:firebase-auth") // For firebase Auth
    implementation("com.google.android.gms:play-services-auth:21.2.0")  // Google Play services library
    implementation ("com.google.android.material:material:1.5.0") // Matrial UI Design
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}