import re

with open('gradle/libs.versions.toml', 'r') as f:
    content = f.read()

content = content.replace('[versions]\n', '[versions]\nbiometric = "1.2.0-alpha05"\n')
content = content.replace('[libraries]\n', '[libraries]\nandroidx-biometric = { group = "androidx.biometric", name = "biometric", version.ref = "biometric" }\n')

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(content)
