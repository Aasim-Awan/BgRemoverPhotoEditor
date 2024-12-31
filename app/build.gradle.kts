plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.photoTools.bgEraser"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bg.remover.android.background.eraser.editor"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    setProperty("archivesBaseName", "Photo Collage BGRemover")


    buildFeatures {
        viewBinding = true
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

    kotlinOptions {
        jvmTarget = "1.8"
    }

}


dependencies {
    // AndroidX Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.ui.graphics.android)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.config)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Third-party Libraries
    implementation ("jp.co.cyberagent.android:gpuimage:2.1.0")
    implementation(project(":simplecropview"))
    implementation(project(":cutout"))
    implementation (project(":nativetemplates"))
    implementation("com.karumi:dexter:6.2.3")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.github.erenalpaslan:removebg:1.0.4")
    implementation ("com.burhanrashid52:photoeditor:3.0.2")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("com.google.android.gms:play-services-ads:23.3.0")
    //implementation ("androidx.lifecycle:lifecycle-runtime-ktx::2.8.6")
    implementation ("com.google.android.material:material:1.9.0")
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-messaging:23.0.0")
}
