plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.chaquo.python")
}

android {
    namespace = "org.thamba.guard"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.thamba.guard"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"

        // Chaquopy needs to know which processors to build for: arm64 for real phones,
        // x86_64 for the emulator.
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

// The decision engine is plain Python and lives outside the app, in ../engine,
// so the same files are tested on the laptop and run on the phone.
//
// The build needs a Python on this computer with the same version as below. If
// "python" is not on your PATH, point to it in either of these ways:
//   gradlew assembleDebug -PbuildPython=C:/path/to/python.exe
//   or set the environment variable BUILD_PYTHON
val buildPythonPath: String? = providers.gradleProperty("buildPython").orNull ?: System.getenv("BUILD_PYTHON")

chaquopy {
    defaultConfig {
        version = "3.13"
        if (buildPythonPath != null) {
            buildPython(buildPythonPath)
        }
    }
    sourceSets {
        getByName("main") {
            srcDir("../../engine")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}
