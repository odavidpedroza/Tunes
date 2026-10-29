plugins {
    alias(libs.plugins.tunes.android.feature)
}

android {
    namespace = "com.davidpedroza.tunes.feature.songs"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:player"))

    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.paging.runtime)

    testImplementation(libs.androidx.paging.testing)
}
