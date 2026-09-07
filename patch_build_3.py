with open('app/build.gradle.kts', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if "buildConfigField" not in line:
        new_lines.append(line)
        if "testInstrumentationRunner =" in line:
            new_lines.append('    buildConfigField("String", "GITHUB_OWNER", "\\"${System.getenv("GITHUB_OWNER") ?: ""}\\"")\n')
            new_lines.append('    buildConfigField("String", "GITHUB_REPO", "\\"${System.getenv("GITHUB_REPO") ?: ""}\\"")\n')
            new_lines.append('    buildConfigField("String", "GITHUB_TOKEN", "\\"${System.getenv("GITHUB_TOKEN") ?: ""}\\"")\n')

with open('app/build.gradle.kts', 'w') as f:
    f.writelines(new_lines)
