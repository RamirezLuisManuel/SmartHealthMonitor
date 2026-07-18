import java.util.Properties

// ── Leer credenciales de local.properties (nunca en el código fuente) ─────────
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

// tv/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

android {
    namespace = "mx.utng.smarthealthmonitor.lmrr.tv"
    compileSdk = 36

    defaultConfig {
        applicationId = "mx.utng.smarthealthmonitor.lmrr.tv"
        minSdk  = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // ── Neon PostgreSQL credentials (desde local.properties) ──────────────
        buildConfigField("String", "NEON_HOST",
            "\"${localProperties["NEON_HOST"] ?: ""}\"")
        buildConfigField("String", "NEON_API_KEY",
            "\"${localProperties["NEON_API_KEY"] ?: ""}\"")
        buildConfigField("String", "NEON_CONN_STRING",
            "\"${localProperties["NEON_CONN_STRING"] ?: ""}\"")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    // Jetpack Compose para TV
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // Leanback Library — el estándar de Android TV
    implementation(libs.androidx.leanback)
    // Glide para cargar imágenes en las cards
    implementation(libs.glide)
    // Compartir Room + Repository con módulo app
    // NOTA: implementation(project(":app")) requiere que :app sea un módulo 'library'.
    // Se habilitará cuando se extraiga la lógica compartida a un módulo :core o :data.
    // implementation(project(":app"))
    // ViewModel + Coroutines
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // AppCompat (requerido por FragmentActivity y Leanback)
    implementation(libs.androidx.appcompat)
    // Core KTX
    implementation(libs.androidx.core.ktx)
    // Fragment KTX — requerido para viewModels() delegate
    implementation(libs.androidx.fragment.ktx)
    // Room — base de datos local del módulo TV
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Eclipse Paho MQTT para Android
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")
    // Kotlinx Serialization para JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Retrofit
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}