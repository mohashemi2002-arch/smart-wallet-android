import re
def fix(file):
    with open(file, 'r') as f:
        content = f.read()
    
    # ensure import
    if "import com.example.utils.BankBinHelper" not in content:
        content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport com.example.utils.BankBinHelper")

    # fix the detectBank call
    content = content.replace(
        "val detectedBank = remember(cardNumber) {\n        com.example.utils.BankBinHelper.getBankInfoFromCardNumber(cardNumber)?.let { Pair(it.name, it.colorHex) } ?: Pair(\"\", \"#1E88E5\")\n    }",
        "val detectedBank: Pair<String, String> = remember(cardNumber) {\n        val info = BankBinHelper.getBankInfoFromCardNumber(cardNumber)\n        if (info != null) Pair(info.name, info.colorHex) else Pair(\"\", \"#1E88E5\")\n    }"
    )

    with open(file, 'w') as f:
        f.write(content)

fix('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt')
fix('app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt')
