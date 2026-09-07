with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

target = """    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    buildConfigField("String", "GITHUB_OWNER", "\\"\\${System.getenv("GITHUB_OWNER") ?: ""}\\"")
    buildConfigField("String", "GITHUB_REPO", "\\"\\${System.getenv("GITHUB_REPO") ?: ""}\\"")
    buildConfigField("String", "GITHUB_TOKEN", "\\"\\${System.getenv("GITHUB_TOKEN") ?: ""}\\"")"""

replacement = """    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    
    val owner = System.getenv("GITHUB_OWNER") ?: ""
    val repo = System.getenv("GITHUB_REPO") ?: ""
    val token = System.getenv("GITHUB_TOKEN") ?: ""
    
    buildConfigField("String", "GITHUB_OWNER", "\\"$owner\\"")
    buildConfigField("String", "GITHUB_REPO", "\\"$repo\\"")
    buildConfigField("String", "GITHUB_TOKEN", "\\"$token\\"")"""

content = content.replace(target, replacement)
content = content.replace('versionName = "1.0"', 'versionName = "1.0.0"')

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
