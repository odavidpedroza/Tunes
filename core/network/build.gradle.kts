plugins {
    alias(libs.plugins.tunes.android.library)
    alias(libs.plugins.tunes.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.davidpedroza.tunes.core.network"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.okhttp.mockwebserver)
}
