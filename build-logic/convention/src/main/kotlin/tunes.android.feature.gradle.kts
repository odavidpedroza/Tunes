// A feature module: Android library + Compose + Hilt + the UI-layer dependencies every screen needs.
plugins {
    id("tunes.android.library")
    id("tunes.android.compose")
    id("tunes.hilt")
    // Navigation routes are @Serializable classes.
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    "implementation"(project(":core:common"))
    "implementation"(project(":core:model"))
    "implementation"(project(":core:designsystem"))

    "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
    "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
    "implementation"(libs.findLibrary("androidx-navigation-compose").get())
    "implementation"(libs.findLibrary("hilt-navigation-compose").get())
    "implementation"(libs.findLibrary("kotlinx-serialization-json").get())

    "testImplementation"(project(":core:testing"))
    "testImplementation"(libs.findLibrary("turbine").get())
    "testImplementation"(libs.findLibrary("androidx-paging-testing").get())
    "androidTestImplementation"(project(":core:testing"))
}
