with open('app/src/main/java/com/example/ui/screens/SmsSenderPickerBottomSheet.kt', 'r') as f:
    content = f.read()

import re

# We will export a list of unique bank names from BankBinHelper
new_banks = "val banks = com.example.utils.BankBinHelper.getAllUniqueBanks()"

content = re.sub(r'val banks = listOf\([\s\S]*?\)', new_banks, content)

with open('app/src/main/java/com/example/ui/screens/SmsSenderPickerBottomSheet.kt', 'w') as f:
    f.write(content)
