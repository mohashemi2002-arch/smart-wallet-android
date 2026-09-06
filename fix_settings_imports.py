import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Add imports
if "androidx.activity.compose.rememberLauncherForActivityResult" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.activity.compose.rememberLauncherForActivityResult\nimport androidx.activity.result.contract.ActivityResultContracts")

if "androidx.compose.material.icons.filled.Upload" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Warning", "import androidx.compose.material.icons.filled.Warning\nimport androidx.compose.material.icons.filled.Upload")

content = content.replace('Icons.Default.UploadFile', 'Icons.Default.Upload')

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)

with open('app/build.gradle.kts', 'r') as f:
    build_content = f.read()

build_content = build_content.replace('    implementation("org.apache.poi:poi-ooxml:5.2.5")\n', '')

with open('app/build.gradle.kts', 'w') as f:
    f.write(build_content)
