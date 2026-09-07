import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

target = r'val downloadBankLogos = tasks\.register\("downloadBankLogos"\) \{.*?(?=\n// Hook directly)'
replacement = '''val downloadBankLogos = tasks.register("downloadAndSetupBankLogos") {
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
                java.util.zip.ZipInputStream(input).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val name = entry.name
                        if (name.startsWith("IranianBankLogos-master/resources/images/") && name.endsWith("-300-wc.png")) {
                            val baseName = name.substringAfterLast("/").removeSuffix("-300-wc.png")
                            val slug = slugMapping[baseName]
                            if (slug != null) {
                                val targetFile = File(drawableDir, "ic_bank_$slug.png")
                                if (!targetFile.exists()) {
                                    java.io.FileOutputStream(targetFile).use { output ->
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
}'''

content = re.sub(target, replacement, content, flags=re.DOTALL)
content = content.replace('dependsOn("downloadBankLogos")', 'dependsOn("downloadAndSetupBankLogos")')

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
