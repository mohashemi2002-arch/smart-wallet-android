with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

content = content.replace('dependencies {', 'dependencies {\n    implementation("org.apache.poi:poi-ooxml:5.2.5")\n')

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
