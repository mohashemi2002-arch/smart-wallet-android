#!/bin/bash
cat << 'INNER_EOF' > replace_export.py
import re
with open("app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt", "r") as f:
    content = f.read()

content = re.sub(
r'    fun generateExport\(\): String \{.*?    \}',
'''    fun generateExport(): String {
        return Exporter.generateExportCsv(
            transactions = transactions.value,
            assets = assets.value
        )
    }''', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt", "w") as f:
    f.write(content)
INNER_EOF
python3 replace_export.py
