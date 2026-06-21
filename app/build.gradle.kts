plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

import java.util.Properties

data class ReleaseSigningConfig(
    val storeFile: String,
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String,
)

fun resolveConfigValue(project: org.gradle.api.Project, key: String): String? {
    val projectProperty = project.providers.gradleProperty(key).orNull?.trim()
    if (!projectProperty.isNullOrBlank()) {
        return projectProperty
    }

    val envProperty = System.getenv(key)?.trim()
    if (!envProperty.isNullOrBlank()) {
        return envProperty
    }

    val localPropertiesFile = project.projectDir.resolve("local.properties")
    if (localPropertiesFile.exists()) {
        val localProperties = Properties().apply {
            localPropertiesFile.inputStream().use { load(it) }
        }
        val localValue = localProperties.getProperty(key)?.trim()
        if (!localValue.isNullOrBlank()) {
            return localValue
        }
    }

    return null
}

val serverBaseUrl: String = run {
    val projectProperty = providers.gradleProperty("BACKEND_BASE_URL").orNull?.trim()
    if (!projectProperty.isNullOrBlank()) {
        projectProperty
    } else {
        val envProperty = System.getenv("BACKEND_BASE_URL")?.trim()
        if (!envProperty.isNullOrBlank()) {
            envProperty
        } else {
            val localPropertiesFile = projectDir.resolve("local.properties")
            val localValue = if (localPropertiesFile.exists()) {
                val localProperties = Properties().apply {
                    localPropertiesFile.inputStream().use { load(it) }
                }
                localProperties.getProperty("serverBaseUrl")?.trim()
            } else {
                null
            }

            if (!localValue.isNullOrBlank()) {
                localValue
            } else {
                "http://10.0.2.2:5000/"
            }
        }
    }
}

val releaseSigningConfig: ReleaseSigningConfig? = run {
    val storeFile = resolveConfigValue(project, "releaseStoreFile")
    val storePassword = resolveConfigValue(project, "releaseStorePassword")
    val keyAlias = resolveConfigValue(project, "releaseKeyAlias")
    val keyPassword = resolveConfigValue(project, "releaseKeyPassword")
    if (
        storeFile.isNullOrBlank() ||
        storePassword.isNullOrBlank() ||
        keyAlias.isNullOrBlank() ||
        keyPassword.isNullOrBlank()
    ) {
        null
    } else {
        ReleaseSigningConfig(
            storeFile = storeFile,
            storePassword = storePassword,
            keyAlias = keyAlias,
            keyPassword = keyPassword,
        )
    }
}

android {
    namespace = "rs.ac.bg.matf"
    compileSdk = 36

    defaultConfig {
        applicationId = "rs.ac.bg.matf"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField(
            "String",
            "BACKEND_BASE_URL",
            "\"${serverBaseUrl.replace("\"", "\\\"")}\""
        )
    }

    signingConfigs {
        create("release") {
            releaseSigningConfig?.let { signing ->
                storeFile = file(signing.storeFile)
                storePassword = signing.storePassword
                keyAlias = signing.keyAlias
                keyPassword = signing.keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (releaseSigningConfig != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

gradle.taskGraph.whenReady {
    val releaseTasksRequested = allTasks.any { task ->
        val taskName = task.name.lowercase()
        taskName == "assemblerelease" ||
            taskName == "bundlerelease" ||
            taskName == "installrelease" ||
            taskName == "packagerelease" ||
            taskName == "signreleasebundle" ||
            taskName == "publishrelease"
    }
    if (releaseTasksRequested && releaseSigningConfig == null) {
        throw GradleException(
            "Release signing is not configured. Set releaseStoreFile, releaseStorePassword, " +
                "releaseKeyAlias and releaseKeyPassword in app/local.properties or via env vars."
        )
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.7")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
}
