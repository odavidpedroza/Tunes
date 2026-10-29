plugins {
    alias(libs.plugins.tunes.android.library)
    alias(libs.plugins.tunes.hilt)
}

android {
    namespace = "com.davidpedroza.tunes.core.data"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    api(libs.androidx.paging.runtime)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.androidx.paging.testing)
    testImplementation(libs.turbine)
}
