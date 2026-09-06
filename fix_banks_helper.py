with open('app/src/main/java/com/example/utils/BankBinHelper.kt', 'r') as f:
    content = f.read()

import re

# Add the function getAllUniqueBanks
new_func = """    fun getBankInfoFromCardNumber(cardNumber: String): BankInfo? {
        val normalized = normalizePersianDigits(cardNumber).replace("-", "").replace(" ", "")
        if (normalized.length >= 6) {
            val prefix = normalized.substring(0, 6)
            return binMap[prefix]
        }
        return null
    }

    fun getAllUniqueBanks(): List<String> {
        return binMap.values.map { it.name }.distinct().sorted()
    }
}"""

content = re.sub(r'    fun getBankInfoFromCardNumber.*?return null\n    }\n\}', new_func, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/utils/BankBinHelper.kt', 'w') as f:
    f.write(content)
