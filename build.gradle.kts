plugins {
    id("com.android.application") version "9.4.1"
    id("org.jetbrains.dokka-android") version "0.9.18"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

android {
    compileSdk { version = release(36) }

    defaultConfig {
	minSdk { version = release(21) }
        targetSdk = 36
    }

    buildFeatures {
        viewBinding = true
    }

    signingConfigs {
        register("release") {
            storeFile = file("release.keystore")
            storePassword = System.getenv("KEYPWD")
            keyAlias = "releaseKey"
            keyPassword = System.getenv("KEYPWD")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
            //signingConfig = signingConfigs.release
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs(listOf("lib"))
        }
    }
    namespace = "com.ids1024.whitakerswords"
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_22
        targetCompatibility = JavaVersion.VERSION_22
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
}
