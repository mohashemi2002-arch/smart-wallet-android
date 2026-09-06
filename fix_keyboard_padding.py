import re

# Adding WindowInsets imports if missing
with open('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.foundation.layout.navigationBarsPadding" not in content:
    content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.layout.navigationBarsPadding\nimport androidx.compose.foundation.layout.imePadding")

with open('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt', 'w') as f:
    f.write(content)
