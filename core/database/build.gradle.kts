plugins {
    alias(libs.plugins.tunes.android.library)
    alias(libs.plugins.tunes.hilt)
    alias(libs.plugins.room)
}

android {
    namespace = "com.davidpedroza.tunes.core.database"
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":core:model"))

    api(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)
    api(libs.androidx.paging.common)
    implementation(libs.kotlinx.coroutines.core)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
