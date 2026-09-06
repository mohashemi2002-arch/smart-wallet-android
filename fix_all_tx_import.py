import re

with open('app/src/main/java/com/example/ui/components/AllTransactionsDialog.kt', 'r') as f:
    content = f.read()

content = content.replace("import com.example.ui.util.CategoryIconMap", "import com.example.ui.components.CategoryIconMap")
content = content.replace("import com.example.ui.util.PersianFormatters", "import com.example.ui.components.PersianFormatters")

with open('app/src/main/java/com/example/ui/components/AllTransactionsDialog.kt', 'w') as f:
    f.write(content)
