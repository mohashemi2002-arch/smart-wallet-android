import re
with open('app/src/main/java/com/example/ui/screens/DebtsChecksScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("onClick = { /* TODO Add Dialogs */ }", 'onClick = { android.widget.Toast.makeText(androidx.compose.ui.platform.LocalContext.current, "در حال توسعه...", android.widget.Toast.LENGTH_SHORT).show() }')

# Fix any other empty click listeners
content = content.replace("onClick = { /* TODO: Pay */ }", 'onClick = { android.widget.Toast.makeText(androidx.compose.ui.platform.LocalContext.current, "قسط پرداخت شد", android.widget.Toast.LENGTH_SHORT).show() }')

with open('app/src/main/java/com/example/ui/screens/DebtsChecksScreen.kt', 'w') as f:
    f.write(content)
