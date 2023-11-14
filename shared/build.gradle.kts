plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.charts.kt)
                api(libs.d2v.core)
                api(libs.d2v.random)
                api(libs.d2v.shape)
                api(libs.d2v.viz)
                api(libs.d2v.timer)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "io.data2viz.charts.demo"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
}
