import re
with open('app/src/main/java/com/example/ui/components/PendingReviewBottomSheet.kt', 'r') as f:
    content = f.read()

imports = "import androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.verticalScroll\n"
content = content.replace("import androidx.compose.ui.Modifier", imports + "import androidx.compose.ui.Modifier")

with open('app/src/main/java/com/example/ui/components/PendingReviewBottomSheet.kt', 'w') as f:
    f.write(content)
