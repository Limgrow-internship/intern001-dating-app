import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.service)
}

fun getLocalProperty(key: String, defaultValue: String): String {
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
    }
    return properties.getProperty(key, defaultValue)
}

android {
    namespace = "com.intern001.dating"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.intern001.dating"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("releaseApp") {
            dimension = "distribution"
            applicationId = "com.intern001.dating"
        }
        create("debugApp") {
            dimension = "distribution"
            applicationId = "com.led.keyboard.neon.classic"
        }
    }

    buildTypes {
        debug {
            val devUrl = getLocalProperty("base.url.dev", "http://10.0.2.2:3000/")
            buildConfigField("String", "BASE_URL", "\"$devUrl\"")
            isDebuggable = true
        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://api.hearton.com/\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    variantFilter {
        val flavorNames = flavors.map { it.name }
        if ((buildType.name == "debug" && flavorNames.contains("releaseApp")) ||
            (buildType.name == "release" && flavorNames.contains("debugApp"))
        ) {
            ignore = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Ads
    implementation(libs.play.services.ads)

    // Google Sign-In
    implementation(libs.google.login)

    // Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")

    // play billing
    implementation(libs.billing.ktx)
    implementation(libs.billing)

    // Facebook
    implementation("com.facebook.android:facebook-login:[16.3.0]")

    // Flexbox
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    // Camera
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("com.google.guava:guava:31.0.1-android")

    // Glide for image loading
    implementation(libs.glide)
    ksp(libs.glide.compiler)

    // Glide transformations (blur, etc.)
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    // Material Components
    implementation("com.google.android.material:material:1.12.0")

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    implementation("jp.wasabeef:richeditor-android:2.0.0")

    implementation("io.socket:socket.io-client:2.1.0")
    implementation("com.airbnb.android:lottie:6.1.0")
}

// KtLint configuration
ktlint {
    version = "1.5.0"
    verbose.set(true)
    android.set(true)
    filter {
        exclude { element -> element.file.path.contains("generated/") }
    }
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

tasks.register("generateSupportedLangs") {
    doLast {
        val resDir = File("$projectDir/src/main/res")
        val langs = resDir.listFiles()
            ?.filter { it.isDirectory && it.name.startsWith("values-") }
            ?.map { it.name.removePrefix("values-") }
            ?.filter { it.length == 2 }
            ?.distinct()
            ?: emptyList()
        val outDir = File(resDir, "raw")
        outDir.mkdirs()
        val outFile = File(outDir, "supported_languages.json")
        outFile.writeText(
            "[${langs.joinToString(",") { "\"$it\"" }}]",
        )
        println("Supported languages: $langs")
    }
}

tasks.named("preBuild") {
    dependsOn("generateSupportedLangs", "copyGoogleServicesFiles")
}

tasks.register("copyGoogleServicesFiles") {
    doLast {
        val rawPremiumFile = File("$projectDir/src/main/res/raw/google_services_premium.json")
        val rawDatingFile = File("$projectDir/src/main/res/raw/google_services_dating.json")

        if (rawPremiumFile.exists()) {
            val debugAppDir = File("$projectDir/src/debugApp")
            debugAppDir.mkdirs()
            val debugAppFile = File(debugAppDir, "google-services.json")
            rawPremiumFile.copyTo(debugAppFile, overwrite = true)
        }

        if (rawDatingFile.exists()) {
            val releaseAppDir = File("$projectDir/src/releaseApp")
            releaseAppDir.mkdirs()
            val releaseAppFile = File(releaseAppDir, "google-services.json")
            rawDatingFile.copyTo(releaseAppFile, overwrite = true)
        }
    }
}
