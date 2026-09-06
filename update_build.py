with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

content = content.replace('dependencies {', 'dependencies {\n    implementation(libs.androidx.biometric)')

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
