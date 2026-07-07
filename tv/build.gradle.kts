// tv/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "mx.utng.smarthealthmonitor.lmrr.tv"
    compileSdk = 35

    defaultConfig {
        applicationId = "mx.utng.smarthealthmonitor.lmrr.tv"
        minSdk  = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Jetpack Compose para TV
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)

    // Leanback Library — el estándar de Android TV
    implementation("androidx.leanback:leanback:1.2.0")
    // Glide para cargar imágenes en las cards
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // Compartir Room + Repository con módulo app
    // NOTA: implementation(project(":app")) requiere que :app sea un módulo 'library'.
    // Se habilitará cuando se extraiga la lógica compartida a un módulo :core o :data.
    // implementation(project(":app"))
    // ViewModel + Coroutines
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    // AppCompat (requerido por FragmentActivity y Leanback)
    implementation("androidx.appcompat:appcompat:1.7.0")
    // Core KTX
    implementation("androidx.core:core-ktx:1.16.0")
}