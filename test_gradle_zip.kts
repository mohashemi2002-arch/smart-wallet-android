import java.net.URI
import java.util.zip.ZipInputStream
import java.io.File
import java.io.FileOutputStream

val url = URI("https://github.com/amastaneh/IranianBankLogos/archive/refs/heads/master.zip").toURL()
val drawableDir = File("logos_out")
drawableDir.mkdirs()

val slugMapping = mapOf(
    "bmi" to "melli",
    "bsi" to "saderat",
    "mellat" to "mellat",
    "tejarat" to "tejarat",
    "sepah" to "sepah",
    "bki" to "keshavarzi",
    "maskan" to "maskan",
    "rb" to "refah",
    "sb" to "saman",
    "bpi" to "pasargad",
    "parsian" to "parsian",
    "shahr" to "shahr",
    "ba" to "ayandeh",
    "day" to "dey",
    "sina" to "sina",
    "en" to "en",
    "kar" to "karafarin",
    "resalat" to "resalat",
    "mehriran" to "mehr",
    "sarmayeh" to "sarmayeh",
    "tourism" to "gardeshgari",
    "iz" to "iz",
    "me" to "khavarmiyaneh",
    "post" to "post",
    "tt" to "tt",
    "bim" to "bim",
    "edbi" to "edbi",
    "melal" to "melal"
)

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
                    FileOutputStream(targetFile).use { output ->
                        zis.copyTo(output)
                    }
                    println("Saved ic_bank_$slug.png")
                }
            }
            entry = zis.nextEntry
        }
    }
}
