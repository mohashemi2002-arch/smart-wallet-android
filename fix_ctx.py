import re
with open('app/src/main/java/com/example/ui/screens/DebtsChecksScreen.kt', 'r') as f:
    content = f.read()

# Replace all occurrences of `val ctx = androidx.compose.ui.platform.LocalContext.current` inside the items mapping and put it at the start of the `items` block.
content = content.replace("val ctx = androidx.compose.ui.platform.LocalContext.current", "")
content = content.replace("        items(installments.take(5)) { inst ->", "        items(installments.take(5)) { inst ->\n            val ctx = androidx.compose.ui.platform.LocalContext.current")
content = content.replace("        items(cheques) { cheque ->", "        items(cheques) { cheque ->\n            val ctx = androidx.compose.ui.platform.LocalContext.current")

with open('app/src/main/java/com/example/ui/screens/DebtsChecksScreen.kt', 'w') as f:
    f.write(content)
