import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.blindrun.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.blindrun.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // 从 local.properties 读取高德 API Key
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        // 高德 Android SDK Key（用于地图和定位）
        val amapKey = properties.getProperty("AMAP_KEY", "")
        manifestPlaceholders["AMAP_KEY"] = amapKey
        buildConfigField("String", "AMAP_KEY", "\"$amapKey\"")

        // 高德 Web API Key（用于路径规划等服务）
        val amapWebKey = properties.getProperty("AMAP_WEB_KEY", "")
        buildConfigField("String", "AMAP_WEB_KEY", "\"$amapWebKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {
    val compose_version = rootProject.extra["compose_version"]
    val hilt_version = rootProject.extra["hilt_version"]

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_version")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Hilt - 使用 KSP 替代 kapt
    implementation("com.google.dagger:hilt-android:$hilt_version")
    ksp("com.google.dagger:hilt-compiler:$hilt_version")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // 高德地图 3D SDK（包含定位）
    implementation("com.amap.api:3dmap:10.0.600")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

// KSP 配置（替代 kapt）
ksp {
    arg("dagger.fastInit", "ENABLED")
}