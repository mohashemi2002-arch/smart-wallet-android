with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

content = content.replace(
    'buildConfigField("String", "GITHUB_OWNER", "\\"\\${System.getenv(\\"GITHUB_OWNER\\") ?: \\"\\"}\\"")',
    'buildConfigField("String", "GITHUB_OWNER", "\\"mohashemi2002-arch\\"")'
)

content = content.replace(
    'buildConfigField("String", "GITHUB_REPO", "\\"\\${System.getenv(\\"GITHUB_REPO\\") ?: \\"\\"}\\"")',
    'buildConfigField("String", "GITHUB_REPO", "\\"smart-wallet-android\\"")'
)

content = content.replace(
    'buildConfigField("String", "GITHUB_TOKEN", "\\"\\${System.getenv(\\"GITHUB_TOKEN\\") ?: \\"\\"}\\"")',
    'buildConfigField("String", "GITHUB_TOKEN", "\\"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\\"")'
)

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
