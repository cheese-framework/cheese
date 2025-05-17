plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "net.codeocean.cheese"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.codeocean.cheese"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.12"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
        applicationVariants.all {
            this.outputs
                .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                .forEach { output ->
                    val outputFileName = "js.apk"
                    println("OutputFileName: $outputFileName")
                    output.outputFileName = outputFileName
                }
        }

        ndk {
            abiFilters.clear()
            abiFilters.addAll(listOf("x86_64", "arm64-v8a"))
        }

    }

    signingConfigs {
        create("config") {
            keyAlias = project.findProperty("MYAPP_KEY_ALIAS") as String? ?: "debug"
            keyPassword = project.findProperty("MYAPP_KEY_PASSWORD") as String? ?: "android"
            storeFile = file(project.findProperty("MYAPP_STORE_FILE") as String? ?: "${System.getenv("HOME")}/.android/debug.keystore")
            storePassword = project.findProperty("MYAPP_STORE_PASSWORD") as String? ?: "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            signingConfig = signingConfigs.getByName("config")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
        }
        jniLibs {
            // 排除重复的 .so 文件
            excludes += "lib/arm64-v8a/libtermux.so"
            excludes += "lib/x86_64/libtermux.so"

            // 选择第一个找到的 .so 文件
            pickFirsts += "lib/armeabi-v7a/libopencv_java4.so"
            pickFirsts += "lib/arm64-v8a/libopencv_java4.so"

            pickFirsts += "lib/arm64-v8a/libc++_shared.so"
            pickFirsts += "lib/armeabi-v7a/libc++_shared.so"
        }
    }

}

dependencies {
    implementation (project(":frontend:javascript"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}