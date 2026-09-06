import re

with open('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt', 'r') as f:
    content = f.read()

content = content.replace("    // Modal: Select Bank Sender from Device SMS (Screenshot 7)", "            }\n        }\n    }\n\n    // Modal: Select Bank Sender from Device SMS (Screenshot 7)")

with open('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt', 'w') as f:
    f.write(content)
