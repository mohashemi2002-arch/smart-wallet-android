import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

target = '''tasks.register("downloadBankLogos") {
    description = "Automatically downloads Iranian bank vector assets from GitHub repo without manual user action"
    val drawableDir = file("src/main/res/drawable")
    
    doLast {
        if (!drawableDir.exists()) drawableDir.mkdirs()
        
        val bankSlugs = listOf(
            "melli", "saderat", "mellat", "tejarat", "sepah", "keshavarzi",
            "maskan", "refah", "saman", "blubank", "pasargad", "parsian",
            "shahr", "ayandeh", "dey", "sina", "en", "karafarin", "resalat", "mehr"
        )
        
        val baseUrl = "https://raw.githubusercontent.com/amastaneh/IranianBankLogos/master/assets/png/"
        
        bankSlugs.forEach { slug ->
            val targetFile = file("${drawableDir.absolutePath}/ic_bank_$slug.png")
            if (!targetFile.exists()) {
                try {
                    val url = URL("$baseUrl$slug.png")
                    url.openStream().use { input ->
                        targetFile.outputStream().use { output -> input.copyTo(output) }
                    }
                    println("Successfully downloaded: ic_bank_$slug.png")
                } catch (e: Exception) {
                    println("Skipped or fallback active for: $slug (${e.message})")
                }
            }
        }
    }
}'''

replacement = '''val downloadBankLogos = tasks.register("downloadBankLogos") {
    description = "Automatically downloads Iranian bank vector assets from GitHub repo without manual user action"
    val resDir = layout.projectDirectory.dir("src/main/res/drawable")
    
    doLast {
        val drawableDir = resDir.asFile
        if (!drawableDir.exists()) drawableDir.mkdirs()
        
        val bankSlugs = listOf(
            "melli", "saderat", "mellat", "tejarat", "sepah", "keshavarzi",
            "maskan", "refah", "saman", "blubank", "pasargad", "parsian",
            "shahr", "ayandeh", "dey", "sina", "en", "karafarin", "resalat", "mehr"
        )
        
        val baseUrl = "https://raw.githubusercontent.com/amastaneh/IranianBankLogos/master/assets/png/"
        
        bankSlugs.forEach { slug ->
            val targetFile = java.io.File(drawableDir, "ic_bank_$slug.png")
            if (!targetFile.exists()) {
                try {
                    val url = java.net.URI("$baseUrl$slug.png").toURL()
                    url.openStream().use { input ->
                        targetFile.outputStream().use { output -> input.copyTo(output) }
                    }
                    println("Successfully downloaded: ic_bank_$slug.png")
                } catch (e: Exception) {
                    println("Skipped or fallback active for: $slug (${e.message})")
                }
            }
        }
    }
}'''

content = content.replace(target, replacement)

# Replace 'URL' with 'java.net.URI(..).toURL()' to avoid deprecation warnings
# Oh I already changed it inside replacement, but I also need to remove 'import java.net.URL'

content = content.replace("import java.net.URL\n", "")

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
