import java.util.zip.ZipInputStream
import java.io.FileOutputStream
import java.net.URI
import java.io.File
import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.example"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.aistudio.smartwallet.ai.qmfzzm"
    minSdk = 24
    targetSdk = 35
    versionCode = 12
    versionName = "1.5.6"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    buildConfigField("String", "UPDATE_JSON_URL", "\"https://raw.githubusercontent.com/mohashemi2002-arch/smart-wallet-android/main/update.json\"")
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {

    implementation(libs.androidx.biometric)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.biometric:biometric:1.1.0")
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  // Uncomment to use Firestore:
  // implementation(libs.firebase.firestore)

  // Uncomment ALL FOUR of the following dependencies together to use Firebase Auth and Google
  // Sign-In via Credential Manager:
  // implementation(libs.firebase.auth)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  implementation("org.jsoup:jsoup:1.17.2")
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

val downloadBankLogos = tasks.register("downloadAndSetupBankLogos") {
    description = "Downloads and extracts Iranian bank logos from GitHub zip archive"
    val resDir = layout.projectDirectory.dir("src/main/res/drawable")
    
    doLast {
        val drawableDir = resDir.asFile
        if (!drawableDir.exists()) drawableDir.mkdirs()
        
        val slugMapping = mapOf(
            "bmi" to "melli", "bsi" to "saderat", "mellat" to "mellat",
            "tejarat" to "tejarat", "sepah" to "sepah", "bki" to "keshavarzi",
            "maskan" to "maskan", "rb" to "refah", "sb" to "saman",
            "bpi" to "pasargad", "parsian" to "parsian", "shahr" to "shahr",
            "ba" to "ayandeh", "day" to "dey", "sina" to "sina",
            "en" to "en", "kar" to "karafarin", "resalat" to "resalat",
            "mehriran" to "mehr", "sarmayeh" to "sarmayeh", "tourism" to "gardeshgari",
            "iz" to "iz", "me" to "khavarmiyaneh", "post" to "post",
            "tt" to "tt", "bim" to "bim", "edbi" to "edbi", "melal" to "melal"
        )
        
        try {
            val url = URI("https://github.com/amastaneh/IranianBankLogos/archive/refs/heads/master.zip").toURL()
            url.openStream().use { input ->
                ZipInputStream(input).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val name = entry.name
                        if (name.startsWith("IranianBankLogos-master/resources/images/") && name.endsWith("-300-wc.png")) {
                            val baseName = name.substringAfterLast("/").removeSuffix("-300-wc.png")
                            val slug = slugMapping[baseName]
                            if (slug != null) {
                                val targetFile = File(drawableDir, "ic_bank_$slug.png")
                                if (!targetFile.exists()) {
                                    FileOutputStream(targetFile).use { output ->
                                        zis.copyTo(output)
                                    }
                                }
                            }
                        }
                        entry = zis.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            println("Logo download failed: ${e.message}")
        }
    }
}
// Hook directly into project compilation
tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn("downloadAndSetupBankLogos")
}
