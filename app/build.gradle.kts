import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    id("com.android.application")
    id("com.mikepenz.aboutlibraries.plugin")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.parcelize")
    kotlin("plugin.serialization")
    id("com.github.zellius.shortcut-helper")
    // Realm (EH)
    id("realm-android")
}

if (!gradle.startParameter.taskRequests.toString().contains("Debug")) {
    apply(plugin = "com.google.gms.google-services")
    // Firebase Crashlytics
    apply(plugin = "com.google.firebase.crashlytics")
}

shortcutHelper.setFilePath("./shortcuts.xml")

android {
    compileSdkVersion(AndroidConfig.compileSdk)
    buildToolsVersion(AndroidConfig.buildTools)
    ndkVersion = AndroidConfig.ndk

    defaultConfig {
        applicationId = "eu.kanade.tachiyomi.sy"
        minSdkVersion(AndroidConfig.minSdk)
        targetSdkVersion(AndroidConfig.targetSdk)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = 17
        versionName = "1.6.2"

        buildConfigField("String", "COMMIT_COUNT", "\"${getCommitCount()}\"")
        buildConfigField("String", "COMMIT_SHA", "\"${getGitSha()}\"")
        buildConfigField("String", "BUILD_TIME", "\"${getBuildTime()}\"")
        buildConfigField("boolean", "INCLUDE_UPDATER", "false")

        multiDexEnabled = true

        ndk {
            abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        create("releaseTest") {
            applicationIdSuffix = ".rt"
            //isMinifyEnabled = true
            //isShrinkResources = true
            isZipAlignEnabled = true
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"))
        }
        named("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isZipAlignEnabled = true
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"))
        }
    }

    flavorDimensions("default")

    productFlavors {
        create("standard") {
            buildConfigField("boolean", "INCLUDE_UPDATER", "true")
            dimension = "default"
        }
        create("fdroid") {
            dimension = "default"
        }
        create("dev") {
            resConfigs("en", "xxhdpi")
            dimension = "default"
        }
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("LICENSE.txt")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/*.kotlin_module")

        // Compatibility for two RxJava versions (EXH)
        exclude("META-INF/rxjava.properties")
    }

    dependenciesInfo {
        includeInApk = false
    }

    lintOptions {
        disable("MissingTranslation", "ExtraTranslation")
        isAbortOnError = false
        isCheckReleaseBuilds = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {

    // Source models and interfaces from Tachiyomi 1.x
    implementation("tachiyomi.sourceapi:source-api:1.1")

    // AndroidX libraries
    implementation("androidx.annotation:annotation:1.3.0-alpha01")
    implementation("androidx.appcompat:appcompat:1.3.0-rc01")
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha03")
    implementation("androidx.browser:browser:1.3.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0-beta01")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.1.0")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.recyclerview:recyclerview:1.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")

    val lifecycleVersion = "2.3.0"
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    // Job scheduling
    implementation("androidx.work:work-runtime-ktx:2.5.0")

    // UI library
    implementation("com.google.android.material:material:1.3.0")

    "standardImplementation"("com.google.firebase:firebase-core:18.0.3")

    // ReactiveX
    implementation("io.reactivex:rxandroid:1.2.1")
    implementation("io.reactivex:rxjava:1.3.8")
    implementation("com.jakewharton.rxrelay:rxrelay:1.2.0")
    implementation("com.github.pwittchen:reactivenetwork:0.13.0")

    // Network client
    val okhttpVersion = "4.9.1"
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:$okhttpVersion")
    implementation("com.squareup.okio:okio:2.10.0")

    // TLS 1.3 support for Android < 10
    implementation("org.conscrypt:conscrypt-android:2.5.1")

    // JSON
    val kotlinSerializationVersion = "1.1.0"
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$kotlinSerializationVersion")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.github.salomonbrys.kotson:kotson:2.5.0")

    // JavaScript engine
    implementation("com.squareup.duktape:duktape-android:1.3.0")

    // Disk
    implementation("com.jakewharton:disklrucache:2.0.2")
    implementation("com.github.tachiyomiorg:unifile:17bec43")
    implementation("com.github.junrar:junrar:7.4.0")

    // HTML parser
    implementation("org.jsoup:jsoup:1.13.1")

    // Database
    implementation("androidx.sqlite:sqlite-ktx:2.1.0")
    implementation("com.github.inorichi.storio:storio-common:8be19de@aar")
    implementation("com.github.inorichi.storio:storio-sqlite:8be19de@aar")
    implementation("com.github.requery:sqlite-android:3.35.5")

    // Preferences
    implementation("com.github.tfcporciuncula.flow-preferences:flow-preferences:1.3.4")

    // Model View Presenter
    val nucleusVersion = "3.0.0"
    implementation("info.android15.nucleus:nucleus:$nucleusVersion")
    implementation("info.android15.nucleus:nucleus-support-v7:$nucleusVersion")

    // Dependency injection
    implementation("com.github.inorichi.injekt:injekt-core:65b0440")

    // Image library
    val coilVersion = "1.2.0"
    implementation("io.coil-kt:coil:$coilVersion")
    implementation("io.coil-kt:coil-gif:$coilVersion")

    implementation("com.github.tachiyomiorg:subsampling-scale-image-view:547d9c0")

    // Logging
    implementation("com.jakewharton.timber:timber:4.7.1")

    // Crash reports
    //implementation("ch.acra:acra-http:5.7.0")

    // Sort
    implementation("com.github.gpanther:java-nat-sort:natural-comparator-1.1")

    // UI
    implementation("com.dmitrymalkovich.android:material-design-dimens:1.4")
    implementation("com.github.dmytrodanylyk.android-process-button:library:1.0.4")
    implementation("eu.davidea:flexible-adapter:5.1.0")
    implementation("eu.davidea:flexible-adapter-ui:1.0.0")
    implementation("com.nightlynexus.viewstatepageradapter:viewstatepageradapter:1.1.0")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.github.tachiyomiorg:DirectionalViewPager:1.0.0")
    implementation("dev.chrisbanes.insetter:insetter:0.5.0")

    // 3.2.0+ introduces weird UI blinking or cut off issues on some devices
    val materialDialogsVersion = "3.1.1"
    implementation("com.afollestad.material-dialogs:core:$materialDialogsVersion")
    implementation("com.afollestad.material-dialogs:input:$materialDialogsVersion")
    implementation("com.afollestad.material-dialogs:datetime:$materialDialogsVersion")

    // Conductor
    implementation("com.bluelinelabs:conductor:2.1.5")
    implementation("com.bluelinelabs:conductor-support:2.1.5") {
        exclude(group = "com.android.support")
    }
    implementation("com.github.tachiyomiorg:conductor-support-preference:2.0.1")

    // FlowBinding
    val flowbindingVersion = "0.12.0"
    implementation("io.github.reactivecircus.flowbinding:flowbinding-android:$flowbindingVersion")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-appcompat:$flowbindingVersion")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-recyclerview:$flowbindingVersion")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-swiperefreshlayout:$flowbindingVersion")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-viewpager:$flowbindingVersion")

    // Licenses
    implementation("com.mikepenz:aboutlibraries:${BuildPluginsVersion.ABOUTLIB_PLUGIN}")

    // Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.16.1")
    testImplementation("org.mockito:mockito-core:1.10.19")

    val robolectricVersion = "3.1.4"
    testImplementation("org.robolectric:robolectric:$robolectricVersion")
    testImplementation("org.robolectric:shadows-multidex:$robolectricVersion")
    testImplementation("org.robolectric:shadows-play-services:$robolectricVersion")

    implementation(kotlin("reflect", version = BuildPluginsVersion.KOTLIN))

    val coroutinesVersion = "1.4.3"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // For detecting memory leaks; see https://square.github.io/leakcanary/
    // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")

    // SY -->
    // [EXH] Android 7 SSL Workaround
    implementation("com.google.android.gms:play-services-safetynet:17.0.0")

    // Changelog
    implementation("com.github.gabrielemariotti.changeloglib:changelog:2.1.0")

    // Text distance (EH)
    implementation ("info.debatty:java-string-similarity:2.0.0")

    // Firebase (EH)
    implementation("com.google.firebase:firebase-analytics-ktx:18.0.3")
    implementation("com.google.firebase:firebase-crashlytics-ktx:17.4.1")

    // Better logging (EH)
    implementation("com.elvishew:xlog:1.9.0")

    // Debug utils (EH)
    val debugOverlayVersion = "1.1.3"
    debugImplementation("com.ms-square:debugoverlay:$debugOverlayVersion")
    "releaseTestImplementation"("com.ms-square:debugoverlay-no-op:$debugOverlayVersion")
    releaseImplementation("com.ms-square:debugoverlay-no-op:$debugOverlayVersion")
    testImplementation("com.ms-square:debugoverlay-no-op:$debugOverlayVersion")

    // RatingBar (SY)
    implementation("me.zhanghai.android.materialratingbar:library:1.4.0")

    // JsonReader for similar manga
    implementation("com.squareup.moshi:moshi:1.12.0")
    // SY <--
}

tasks {
    // See https://kotlinlang.org/docs/reference/experimental.html#experimental-status-of-experimental-api(-markers)
    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.Experimental",
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xuse-experimental=kotlin.ExperimentalStdlibApi",
            "-Xuse-experimental=kotlinx.coroutines.FlowPreview",
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=kotlinx.coroutines.InternalCoroutinesApi",
            "-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi",
            "-Xuse-experimental=coil.annotation.ExperimentalCoilApi",
        )
    }

    // Duplicating Hebrew string assets due to some locale code issues on different devices
    val copyHebrewStrings = task("copyHebrewStrings", type = Copy::class) {
        from("./src/main/res/values-he")
        into("./src/main/res/values-iw")
        include("**/*")
    }

    preBuild {
        dependsOn(formatKotlin, copyHebrewStrings)
    }
}


buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = BuildPluginsVersion.KOTLIN))
    }
}


// Git is needed in your system PATH for these commands to work.
// If it's not installed, you can return a random value as a workaround
fun getCommitCount(): String {
    return runCommand("git rev-list --count HEAD")
    // return "1"
}

fun getGitSha(): String {
    return runCommand("git rev-parse --short HEAD")
    // return "1"
}

fun getBuildTime(): String {
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.format(Date())
}

fun runCommand(command: String): String {
    val byteOut = ByteArrayOutputStream()
    project.exec {
        commandLine = command.split(" ")
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}
