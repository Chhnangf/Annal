import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"
    id("kotlin-parcelize")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    sourceSets {

        androidMain.dependencies {

            /** Jetpack Compose */
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.material3)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.datastore.core)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.androidx.material3.android)
            // Unknown using another material version
            implementation(libs.androidx.compose.material.material2)
            implementation(libs.coil.compose)
            //noinspection GradleCompatible
            implementation(libs.androidx.activity.compose.v172)

            /** Android */
            implementation(libs.com.google.android.exoplayer.exoplayer)
            implementation(libs.androidx.core.ktx.v1101)
            implementation(libs.androidx.annotation.annotation)
            implementation(libs.androidx.lifecycle.lifecycle.viewmodel.compose)

            /** Coroutines */
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.android)

            /** Hilt Android */
            implementation(libs.hilt.android)
            implementation(libs.androidx.hilt.navigation.fragment)

            /** Accompanist */
            implementation(libs.accompanist.pager)
            implementation(libs.accompanist.placeholder.material)

            /** Markdown in markwon */
            val markwonVersion = "4.6.2"
            implementation(libs.core)
            implementation(libs.editor)
            implementation(libs.ext.latex)
            implementation(libs.ext.strikethrough)
            implementation(libs.ext.tables)
            implementation(libs.ext.tasklist)
            implementation(libs.html)
            implementation(libs.image)
            implementation(libs.image.coil)
            implementation(libs.image.glide)
            implementation(libs.image.picasso)
            implementation(libs.inline.parser)
            implementation(libs.linkify)
            implementation(libs.recycler)
            implementation(libs.recycler.table)
            implementation(libs.simple.ext)
            implementation("io.noties.markwon:syntax-highlight:$markwonVersion") {
                exclude(group = "org.jetbrains", module = "annotations-java5")
            }

            implementation(libs.compose.material3.datetime.pickers)

            /** Data serialization support: Gson(Old) */
            // TODO: Suggest use kotlinx.serialization(Enable plugin!!!)
            implementation(libs.gson)

            /** Work */
            implementation (libs.androidx.work.runtime.ktx)
            implementation(libs.androidx.appcompat)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(projects.shared)
        }
    }
}

android {
    namespace = "com.chhangf.annal"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.chhangf.annal"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
    buildTypes {
        getByName("release").isMinifyEnabled = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

dependencies {
    /**
     * Room(sqlite):
     * Unable to annotation ksp in sourceSet.
     * kapt is too old in kotlin, we suggest use ksp to process.
     */
    // val room_version = "2.6.1"
    implementation(libs.androidx.room.runtime)
    // Using new KSP (Kotlin Symbol Processing)
    ksp(libs.androidx.room.compiler)
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)

}