plugins {
    alias(libs.plugins.tunes.android.library)
    alias(libs.plugins.tunes.android.compose)
}

android {
    namespace = "com.davidpedroza.tunes.core.designsystem"
}

dependencies {
    implementation(project(":core:common"))

    api(libs.androidx.compose.material.icons.extended)
    api(libs.coil.compose)
    api(libs.coil.network.okhttp)
}
