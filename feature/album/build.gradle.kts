plugins {
    alias(libs.plugins.tunes.android.feature)
}

android {
    namespace = "com.davidpedroza.tunes.feature.album"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:player"))
}
