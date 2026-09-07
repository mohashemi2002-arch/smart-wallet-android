import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

content = content.replace("java.io.File(", "File(")
content = content.replace("java.net.URI(", "URI(")

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
