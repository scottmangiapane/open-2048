plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlinx.kover") version "0.9.8"
}

android {
    namespace = "com.scottmangiapane.open2048"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.scottmangiapane.open2048"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("play") {
            dimension = "distribution"
        }
        create("github") {
            dimension = "distribution"
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
            // Enable JaCoCo/AGP coverage capture
            enableUnitTestCoverage = true
        }
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
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

kover {
    reports {
        // Shared filters for all reports
        filters {
            excludes {
                classes(
                    "**/R",
                    "**/R$*",
                    "**/BuildConfig",
                    "**/Manifest*",
                    "**/*Test*",
                    "android/**/*",
                    "com.scottmangiapane.open2048.databinding.*",
                    "com.scottmangiapane.open2048.BR",
                )
                annotatedBy("androidx.compose.ui.tooling.preview.Preview")
            }
        }

        // Configure variant-specific reports for flavored Android project
        variant("githubDebug") {
            html {
                title = "Open 2048 Coverage (GitHub Distribution)"
                onCheck = true
            }
            xml {
                onCheck = true
            }
            verify {
                rule {
                    bound {
                        minValue = 90
                    }
                }
            }
        }

        variant("playDebug") {
            html {
                title = "Open 2048 Coverage (Play Store Distribution)"
                onCheck = true
            }
            xml {
                onCheck = true
            }
            verify {
                rule {
                    bound {
                        minValue = 90
                    }
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)
    "playImplementation"(libs.billing.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.mockkAndroid)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
