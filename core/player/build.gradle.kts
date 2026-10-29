plugins {
    alias(libs.plugins.tunes.android.library)
    alias(libs.plugins.tunes.hilt)
}

android {
    namespace = "com.davidpedroza.tunes.core.player"
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:common"))

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.common)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.turbine)
}
