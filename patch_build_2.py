import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

target = 'testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"'
replacement = 'testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"\n    buildConfigField("String", "GITHUB_OWNER", "\\\\"\\\\${System.getenv(\\\\"GITHUB_OWNER\\\\") ?: \\\\\"\\\\\"}\\\\"")\n    buildConfigField("String", "GITHUB_REPO", "\\\\"\\\\${System.getenv(\\\\"GITHUB_REPO\\\\") ?: \\\\\"\\\\\"}\\\\"")\n    buildConfigField("String", "GITHUB_TOKEN", "\\\\"\\\\${System.getenv(\\\\"GITHUB_TOKEN\\\\") ?: \\\\\"\\\\\"}\\\\"")'

content = content.replace(target, replacement)

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
