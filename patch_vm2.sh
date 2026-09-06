#!/bin/bash
sed -i 's/val totalQuantity/val quantity/g' app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
sed -i 's/totalQuantity = quantity/totalQuantity = quantity/g' app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
# Actually let me just replace the fun addAsset block
cat << 'INNER_EOF' > replace_asset.py
import re
with open("app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt", "r") as f:
    content = f.read()

# Fix the unbalanced braces
content = re.sub(r'    \}\n    \}\n    \}\n    \}', '    }', content)
content = re.sub(r'    \}\n    \}\n    \}', '    }', content)

content = re.sub(
r'    fun addAsset\(name: String, type: AssetType, quantity: Double, priceToman: Long, unit: String\) \{.*?(?=    fun updateCategoryBudget)',
'''    fun addAsset(name: String, type: AssetType, quantity: Double, priceToman: Long, unit: String) {
        viewModelScope.launch {
            val asset = Asset(
                name = name,
                type = type,
                totalQuantity = quantity,
                currentUnitPriceToman = priceToman,
                unit = unit
            )
            repository.addAsset(asset)
            _statusMessage.value = "دارایی جدید اضافه شد."
        }
    }

    fun deleteAsset(asset: Asset) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }

''', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt", "w") as f:
    f.write(content)
INNER_EOF
python3 replace_asset.py
