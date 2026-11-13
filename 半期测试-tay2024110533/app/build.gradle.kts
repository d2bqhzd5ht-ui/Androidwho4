plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "cn.stu.edu.sicnu.tay.bookmanager"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cn.stu.edu.sicnu.tay.bookmanager"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
}
