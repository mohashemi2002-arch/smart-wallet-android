with open('app/src/main/java/com/example/ui/screens/DebtsChecksScreen.kt', 'r') as f:
    content = f.read()

# I will strip everything after "    if (showAddLoanDialog) {"
idx = content.find("    if (showAddLoanDialog) {")
if idx != -1:
    content = content[:idx]

# This content corresponds to the end of the top-level Column in `DebtsChecksScreen`.
# Wait, let's see how `DebtsChecksScreen` originally looked.
