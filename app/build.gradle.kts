plugins {
    alias(libs.plugins.tunes.android.application)
    alias(libs.plugins.tunes.android.compose)
    alias(libs.plugins.tunes.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.davidpedroza.tunes"

    defaultConfig {
        applicationId = "com.davidpedroza.tunes"
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:player"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:songs"))
    implementation(project(":feature:player"))
    implementation(project(":feature:album"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)

    testImplementation(project(":core:testing"))

    androidTestImplementation(libs.androidx.test.espresso.core)
}
