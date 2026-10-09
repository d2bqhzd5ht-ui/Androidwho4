plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "cn.stu.edu.sicnu.tay.uicodeandmvc"
    compileSdk = 37 // 改为37，直接配合你下载的SDK和依赖库

    defaultConfig {
        applicationId = "cn.stu.edu.sicnu.tay.uicodeandmvc"
        minSdk = 24
        targetSdk = 37 // 同步改为37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true // 开启ViewBinding
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}