import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val configuredVersionCode = providers.gradleProperty("releaseVersionCode").orNull
val releaseVersionCode = configuredVersionCode?.toIntOrNull()
    ?: if (configuredVersionCode == null) 1 else throw GradleException(
        "releaseVersionCode must be a positive integer."
    )
if (releaseVersionCode <= 0) {
    throw GradleException("releaseVersionCode must be a positive integer.")
}
val releaseVersionName = providers.gradleProperty("releaseVersionName").orNull ?: "1.0"
val releaseSigningProperties = listOf(
    "releaseStoreFile",
    "releaseStorePassword",
    "releaseKeyAlias",
    "releaseKeyPassword",
)
val missingReleaseSigningProperties = releaseSigningProperties.filter {
    providers.gradleProperty(it).orNull.isNullOrBlank()
}
val releaseTasksRequested = gradle.startParameter.taskNames.any {
    it.contains("release", ignoreCase = true)
}

if (releaseTasksRequested && missingReleaseSigningProperties.isNotEmpty()) {
    throw GradleException(
        "Release builds require Gradle properties: ${missingReleaseSigningProperties.joinToString()}"
    )
}

android {
    namespace = "com.charr0max.polyglot"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.charr0max.polyglot"
        minSdk = 24
        targetSdk = 36
        versionCode = releaseVersionCode
        versionName = releaseVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = providers.gradleProperty("releaseStoreFile").orNull?.let(::file)
            storePassword = providers.gradleProperty("releaseStorePassword").orNull
            keyAlias = providers.gradleProperty("releaseKeyAlias").orNull
            keyPassword = providers.gradleProperty("releaseKeyPassword").orNull
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    testImplementation(libs.org.json)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}