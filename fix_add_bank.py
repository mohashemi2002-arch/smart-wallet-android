with open('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f),""",
"""        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .imePadding()
                    .navigationBarsPadding(),""")

# Also add verticalScroll to Column inside Surface
content = content.replace(
"""                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {""",
"""                Column(
                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {""")

with open('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt', 'w') as f:
    f.write(content)
