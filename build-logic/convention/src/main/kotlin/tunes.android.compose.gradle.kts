import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

pluginManager.withPlugin("com.android.library") {
    extensions.configure<LibraryExtension> { buildFeatures.compose = true }
}
pluginManager.withPlugin("com.android.application") {
    extensions.configure<ApplicationExtension> { buildFeatures.compose = true }
}

dependencies {
    val bom = libs.findLibrary("androidx-compose-bom").get()
    "implementation"(platform(bom))
    "androidTestImplementation"(platform(bom))
    "implementation"(libs.findLibrary("androidx-compose-ui").get())
    "implementation"(libs.findLibrary("androidx-compose-ui-graphics").get())
    "implementation"(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
    "implementation"(libs.findLibrary("androidx-compose-foundation").get())
    "implementation"(libs.findLibrary("androidx-compose-material3").get())
    "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
    "debugImplementation"(libs.findLibrary("androidx-compose-ui-test-manifest").get())
    "androidTestImplementation"(libs.findLibrary("androidx-compose-ui-test-junit4").get())
    "androidTestImplementation"(libs.findLibrary("androidx-test-ext-junit").get())
    "androidTestImplementation"(libs.findLibrary("androidx-test-runner").get())

    "androidTestImplementation"(libs.findLibrary("androidx-test-espresso-core").get())  // Compose UI test pulls espresso-core 3.5.0, which crashes on API 35+ (InputManager.getInstance)
}
