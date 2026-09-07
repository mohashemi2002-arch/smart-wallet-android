with open('app/build.gradle.kts', 'r') as f:
    lines = f.readlines()
    
with open('app/build.gradle.kts', 'w') as f:
    for line in lines:
        if 'GITHUB_OWNER' in line:
            f.write('    buildConfigField("String", "GITHUB_OWNER", "\\"mohashemi2002-arch\\"")\n')
        elif 'GITHUB_REPO' in line:
            f.write('    buildConfigField("String", "GITHUB_REPO", "\\"smart-wallet-android\\"")\n')
        elif 'GITHUB_TOKEN' in line:
            f.write('    buildConfigField("String", "GITHUB_TOKEN", "\\"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\\"")\n')
        else:
            f.write(line)
