import re

files = [
    'app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt',
    'app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt',
    'app/src/main/java/com/example/ui/components/AddAssetBottomSheet.kt',
    'app/src/main/java/com/example/ui/components/AddChequeBottomSheet.kt',
    'app/src/main/java/com/example/ui/components/AddLoanBottomSheet.kt'
]

for file in files:
    try:
        with open(file, 'r') as f:
            content = f.read()

        # Update text field logic
        content = re.sub(
            r'onValueChange = \{ ([a-zA-Z0-9_]+) = it.filter \{ c -> c.isDigit\(\) \} \}',
            r'''onValueChange = { input -> 
                        if (input.all { it.isDigit() || it in '۰'..'۹' }) {
                            \1 = input
                        }
                    },
                    visualTransformation = com.example.ui.components.ThousandsSeparatorVisualTransformation()''',
            content
        )

        # Update extraction
        content = re.sub(
            r'([a-zA-Z0-9_]+)\.toLongOrNull\(\) \?: 0L',
            r'com.example.ui.components.PersianCurrencyFormatter.extractCleanAmount(\1)',
            content
        )
        
        with open(file, 'w') as f:
            f.write(content)
            
    except Exception as e:
        print(f"Error in {file}: {e}")

