plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.navhost.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.navhost.android"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.material)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.work.runtime.ktx)
    debugImplementation(libs.compose.ui.tooling)

    implementation ("androidx.compose.material:material:1.2.1")
    implementation ("io.coil-kt:coil-compose:2.0.0")
    //noinspection GradleCompatible
    implementation ("com.google.android.exoplayer:exoplayer:2.16.1")
    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("androidx.annotation:annotation:1.6.0")
    implementation ("androidx.activity:activity-compose:1.7.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    /**
     *  for roo,
     */
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    // To use Kotlin annotation processing tool (kapt)
    kapt("androidx.room:room-compiler:$room_version")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")


    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Hilt Android 相关依赖
    implementation("com.google.dagger:hilt-android:2.40.5")
    implementation("androidx.hilt:hilt-navigation-fragment:1.0.0")

    implementation("com.google.accompanist:accompanist-pager:0.24.2-alpha")
    implementation("com.google.accompanist:accompanist-placeholder-material:0.24.2-alpha")

    /**
     *  for markdown
      */
    val markwon_version = "4.6.2"

    implementation ("io.noties.markwon:core:$markwon_version")
    implementation ("io.noties.markwon:editor:$markwon_version")
    implementation ("io.noties.markwon:ext-latex:$markwon_version")
    implementation ("io.noties.markwon:ext-strikethrough:$markwon_version")
    implementation ("io.noties.markwon:ext-tables:$markwon_version")
    implementation ("io.noties.markwon:ext-tasklist:$markwon_version")
    implementation ("io.noties.markwon:html:$markwon_version")
    implementation ("io.noties.markwon:image:$markwon_version")
    implementation ("io.noties.markwon:image-coil:$markwon_version")
    implementation ("io.noties.markwon:image-glide:$markwon_version")
    implementation ("io.noties.markwon:image-picasso:$markwon_version")
    implementation ("io.noties.markwon:inline-parser:$markwon_version")
    implementation ("io.noties.markwon:linkify:$markwon_version")
    implementation ("io.noties.markwon:recycler:$markwon_version")
    implementation ("io.noties.markwon:recycler-table:$markwon_version")
    implementation ("io.noties.markwon:simple-ext:$markwon_version")
    implementation("io.noties.markwon:syntax-highlight:$markwon_version") {
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
    implementation("com.marosseleng.android:compose-material3-datetime-pickers:0.7.2")

    // for JSON 序列化和反序列化
    implementation ("com.google.code.gson:gson:2.9.0")

    // for work
    implementation ("androidx.work:work-runtime-ktx:2.7.1")

}

