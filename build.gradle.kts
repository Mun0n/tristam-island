plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // AGP 9 bundles Kotlin 2.2.10. This classpath raises it to the catalog version.
        // Keep the version in sync with kotlin in gradle/libs.versions.toml.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}
