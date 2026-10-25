import java.util.zip.ZipInputStream
import java.io.FileOutputStream
import java.net.URI
import java.io.File
import java.util.Properties
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
    val runNumber = System.getenv("GITHUB_RUN_NUMBER")
    versionCode = runNumber?.toIntOrNull() ?: 1
    versionName = "1.0.${runNumber ?: "1"}"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    buildConfigField("String", "UPDATE_JSON_URL", "\"https://raw.githubusercontent.com/mohashemi2002-arch/smart-wallet-android/main/update.json\"")
    buildConfigField("String", "GITHUB_OWNER", "\"mohashemi2002-arch\"")
    buildConfigField("String", "GITHUB_REPO", "\"smart-wallet-android\"")
  }

  signingConfigs {
    create("release") {
      val otaKeystore = file("${rootDir}/app/keystore/ota_release.jks")
      val otaKeystoreAlt = file("keystore/ota_release.jks")
      val otaFile = if (otaKeystore.exists()) otaKeystore else if (otaKeystoreAlt.exists()) otaKeystoreAlt else null

      if (otaFile != null) {
        storeFile = otaFile
        storePassword = "android123"
        keyAlias = "ota_key"
        keyPassword = "android123"
      } else {
        // 1. Check CI/CD Environment Variables (GitHub Secrets)
        val envKeystorePath = System.getenv("SIGNING_KEYSTORE_PATH") ?: System.getenv("KEYSTORE_PATH")
        val envStorePassword = System.getenv("SIGNING_KEYSTORE_PASSWORD") ?: System.getenv("STORE_PASSWORD")
        val envKeyAlias = System.getenv("SIGNING_KEY_ALIAS") ?: System.getenv("KEY_ALIAS")
        val envKeyPassword = System.getenv("SIGNING_KEY_PASSWORD") ?: System.getenv("KEY_PASSWORD")

        // 2. Fallback to local.properties
        val localProps = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
          localPropsFile.inputStream().use { stream ->
            localProps.load(stream)
          }
        }

        val keystorePath = envKeystorePath ?: localProps.getProperty("signing.keystore.path")
        val storePasswordVal = envStorePassword ?: localProps.getProperty("signing.keystore.password")
        val keyAliasVal = envKeyAlias ?: localProps.getProperty("signing.key.alias")
        val keyPasswordVal = envKeyPassword ?: localProps.getProperty("signing.key.password")

        if (!keystorePath.isNullOrBlank()) {
          val keystoreFile = file(keystorePath)
          if (keystoreFile.exists()) {
            storeFile = keystoreFile
            storePassword = storePasswordVal
            keyAlias = keyAliasVal
            keyPassword = keyPasswordVal
          }
        }
      }
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
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      val releaseConfig = signingConfigs.getByName("release")
      signingConfig = if (releaseConfig.storeFile != null && releaseConfig.storeFile!!.exists()) {
        releaseConfig
      } else {
        signingConfigs.getByName("debugConfig")
      }
    }
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
  implementation(libs.firebase.auth)
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
