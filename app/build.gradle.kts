import java.io.FileInputStream
import java.util.Properties

plugins {
    jacoco
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.gms)
    alias(libs.plugins.sonarqube)
}

android {
    signingConfigs {
        create("build") {
            storeFile =
                file("${rootProject.projectDir}/keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    namespace = "com.github.se.orator"
    compileSdk = 34

    // Load API keys from local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    val symblAppId: String = localProperties.getProperty("SYMBL_APP_ID") ?: ""
    val symblAppSecret: String = localProperties.getProperty("SYMBL_APP_SECRET") ?: ""
    val gptApiKey: String = localProperties.getProperty("GPT_API_KEY") ?: ""
    val organizationId: String = localProperties.getProperty("GPT_ORGANIZATION_ID") ?: ""

    defaultConfig {
        applicationId = "com.github.se.orator"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Pass API keys to the manifest
        manifestPlaceholders["SYMBL_APP_ID"] = symblAppId
        manifestPlaceholders["SYMBL_APP_SECRET"] = symblAppSecret
        manifestPlaceholders["GPT_API_KEY"] = gptApiKey
        manifestPlaceholders["GPT_ORGANIZATION_ID"] = organizationId
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("build")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
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
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        packagingOptions {
            jniLibs {
                useLegacyPackaging = true
            }
        }
    }

    // Robolectric testing setup
    sourceSets.getByName("testDebug") {
        val test = sourceSets.getByName("test")
        java.setSrcDirs(test.java.srcDirs)
        res.setSrcDirs(test.res.srcDirs)
        resources.setSrcDirs(test.resources.srcDirs)
    }

    sourceSets.getByName("test") {
        java.setSrcDirs(emptyList<File>())
        res.setSrcDirs(emptyList<File>())
        resources.setSrcDirs(emptyList<File>())
    }
}

dependencies {
    // Core Libraries
    implementation(libs.core.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.kotlinx.serialization.json)

    // Jetpack Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui.test.android)
    implementation(libs.androidx.runtime.livedata)
    testImplementation(libs.test.core.ktx)
    testImplementation(libs.ui.test.junit4.android)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.material)

    // Google Maps and Services
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(libs.maps.compose.utils)
    implementation(libs.play.services.auth)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.ui.auth)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.appcheck)

    // Coil
    implementation(libs.coil.compose)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Compose Material Icons for Mic and MicOff
    implementation(libs.androidx.material.icons.extended)

    // Testing Libraries

    testImplementation(libs.junit)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)
    testImplementation(libs.json)

    // UI Testing Libraries
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    configurations.all {
        resolutionStrategy {
            force(libs.androidx.espresso.core)
        }
    }
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Mockito for Unit Testing
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockito.kotlin)

    // Robolectric
    testImplementation(libs.robolectric)

    // Kaspresso
    androidTestImplementation(libs.kaspresso)
    androidTestImplementation(libs.kaspresso.allure.support)
    androidTestImplementation(libs.kaspresso.compose.support)

    // Coroutines Testing
    testImplementation(libs.kotlinx.coroutines.test)

    // for exectutor testing (NetworkChangeReceiverTest)
    testImplementation("androidx.arch.core:core-testing:2.2.0")
}

tasks.withType<Test> {
    // Configure Jacoco for each test
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    mustRunAfter("testDebugUnitTest", "connectedDebugAndroidTest")

    reports {
        xml.required = true
        html.required = true
    }

    val fileFilter = listOf(
        "/R.class",
        "/R$*.class",
        "/BuildConfig.*",
        "/Manifest*.*",
        "/Test.*",
        "android//.",
        "/sigchecks/",
    )
    val debugTree = fileTree("${project.buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.buildDir) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("outputs/code_coverage/debugAndroidTest/connected/*/coverage.ec")
    })
}


sonar {
    properties {
        property("sonar.projectKey", "SWENT-TEAM17_demo-repository")
        property("sonar.organization", "swent-team17")
        property("sonar.host.url", "https://sonarcloud.io")

        property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}