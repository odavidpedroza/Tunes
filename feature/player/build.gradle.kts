plugins {
    alias(libs.plugins.tunes.android.feature)
}

android {
    namespace = "com.davidpedroza.tunes.feature.player"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:player"))
}
