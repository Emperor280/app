plugins {
    id("com.android.application")
}

android {
    namespace = "dev.local.noswipehaptics"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.local.noswipehaptics"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Legacy Xposed API, supported by LSPosed. Only needed at compile time.
    compileOnly("de.robv.android.xposed:api:82")
}
