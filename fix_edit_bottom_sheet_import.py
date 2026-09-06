import re

with open('app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.compose.material.icons.Icons", "import androidx.compose.foundation.verticalScroll\nimport androidx.compose.material.icons.Icons")

with open('app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt', 'w') as f:
    f.write(content)
