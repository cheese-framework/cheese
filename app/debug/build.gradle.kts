plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
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
            val flavorName = this.flavorName
            this.outputs
                .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                .forEach { output ->
                    val versionName = "1.0.12"
                    val outputFileName = "${flavorName}-${versionName}.apk"
                    println("OutputFileName: $outputFileName")
                    output.outputFileName = outputFileName
                }
        }
        flavorDimensions("abi")
        productFlavors {
            create("cheese_all") {
                dimension = "abi"
                ndk{
                    abiFilters.clear()
                    abiFilters.addAll(listOf("x86_64","arm64-v8a"))
                }
            }
            create("cheese_x86_64") {
                dimension = "abi"
                ndk{
                    abiFilters.clear()
                    abiFilters.add("x86_64")
                }
            }
            create("cheese_arm64-v8a") {
                dimension = "abi"
                ndk.abiFilters.clear()
                ndk.abiFilters.add("arm64-v8a")
            }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "META-INF/{AL2.0,LGPL2.1}"
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

//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_17
//        targetCompatibility = JavaVersion.VERSION_17
//        isCoreLibraryDesugaringEnabled = true
//    }
}
kotlin {
    jvmToolchain(17)
}
dependencies {
    implementation (project(":frontend:javascript"))
    // 建议升级到最新稳定版（2.7.7 存在已知的状态保存问题）
    implementation ("androidx.navigation:navigation-compose:2.8.9")
    implementation ("androidx.navigation:navigation-runtime-ktx:2.8.9")
//    implementation("androidx.navigation:navigation-compose:2.7.7")
//    implementation ("androidx.navigation:navigation-runtime-ktx:2.7.7")
    implementation("androidx.compose.material:material-icons-core:1.6.8")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation(platform("io.github.Rosemoe.sora-editor:bom:0.22.1"))
    implementation("io.github.Rosemoe.sora-editor:editor")
    implementation("io.github.Rosemoe.sora-editor:language-textmate")

    implementation ("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}