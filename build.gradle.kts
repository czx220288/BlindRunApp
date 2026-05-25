// 项目根目录 build.gradle.kts
buildscript {
    val compose_version by extra("1.5.4")
    val hilt_version by extra("2.48")

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")  // 升级到 1.9.20
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hilt_version")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://repo1.maven.org/maven2/")
        maven("https://maven.aliyun.com/repository/public")
        maven { url = uri("https://dl.bintray.com/amap/maven") }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}