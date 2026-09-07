with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

content = content.replace("java.util.zip.ZipInputStream(input)", "ZipInputStream(input)")
content = content.replace("java.io.FileOutputStream(targetFile)", "FileOutputStream(targetFile)")

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
