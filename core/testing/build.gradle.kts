// Test doubles and helpers shared by the unit and UI tests of the feature modules.
plugins {
    alias(libs.plugins.tunes.android.library)
}

android {
    namespace = "com.davidpedroza.tunes.core.testing"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:model"))
    api(project(":core:data"))
    api(project(":core:player"))

    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    implementation(libs.androidx.paging.common)
}
