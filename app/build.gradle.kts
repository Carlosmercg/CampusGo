plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.campusgo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.campusgo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    android {
        buildFeatures {
            viewBinding = true
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // Dependencias base de Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.common.ktx)
    implementation(libs.firebase.auth.ktx)


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 🔹 Dependencias adicionales necesarias para la app

    // RecyclerView para listas de productos, chats y categorías
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // ViewBinding para facilitar la manipulación de vistas
    implementation("androidx.databinding:viewbinding:7.1.2")

    // Glide para manejo eficiente de imágenes en lugar de Picasso
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // LiveData y ViewModel para gestión de UI
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")

    // OkHttp para conexiones HTTP en caso de uso de API
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // Gson para trabajar con JSON en caso de integración con API
    implementation("com.google.code.gson:gson:2.8.9")

    // Kotlin Coroutines para tareas asíncronas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")

}