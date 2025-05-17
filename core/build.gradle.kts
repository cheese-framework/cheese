plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "net.codeocean.cheese.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures{

        aidl= true
    }
}

dependencies {
    api (libs.xlog)
    api (project(":shared"))
    api (project(":ncnn"))
    api (project(":mlkit"))
    api (project(":opencv"))
    api ("com.google.zxing:core:3.5.3")
    api("com.github.termux.termux-app:termux-shared:v0.118.1")
    implementation ("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    api("com.google.code.gson:gson:2.10")
    api ("io.github.petterpx:floatingx:2.2.6")
    api ("io.github.petterpx:floatingx-compose:2.2.6")
    api("net.lingala.zip4j:zip4j:1.3.1")
    api("com.github.gzu-liyujiang:Android_CN_OAID:4.2.7")
    api("com.huawei.hms:ads-identifier:3.4.62.300")
    api("com.squareup.okhttp3:okhttp:4.12.0")
    api ("com.github.getActivity:XXPermissions:18.5")
    api ("com.github.getActivity:Toaster:12.6")
    api ("com.github.getActivity:EasyWindow:10.62")
    api ("org.java-websocket:Java-WebSocket:1.5.7")
    api("org.tomlj:tomlj:1.1.1")
    api("com.github.Vove7.Android-Accessibility-Api:accessibility-api:4.1.2")
    api ("androidx.webkit:webkit:1.10.0")
//    api (libs.koin.android)
    // To recognize Latin script
//    api ("com.google.mlkit:text-recognition:16.0.0")
//
//    // To recognize Chinese script
//    api ("com.google.mlkit:text-recognition-chinese:16.0.0")

    implementation("androidx.compose.runtime:runtime:1.6.6")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}