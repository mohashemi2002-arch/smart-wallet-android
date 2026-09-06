#!/bin/bash
# Remove debtLoan stuff from FinanceRepository
sed -i '/val debtLoans:/d' app/src/main/java/com/example/data/FinanceRepository.kt
sed -i '/debtLoanDao.getAllDebtLoans()/d' app/src/main/java/com/example/data/FinanceRepository.kt
sed -i '/if (debtLoans.isEmpty()) {/,/        }/d' app/src/main/java/com/example/data/FinanceRepository.kt
sed -i '/fun addDebtLoan/,/    }/d' app/src/main/java/com/example/data/FinanceRepository.kt
sed -i '/fun updateDebtLoan/,/    }/d' app/src/main/java/com/example/data/FinanceRepository.kt
sed -i '/fun deleteDebtLoan/,/    }/d' app/src/main/java/com/example/data/FinanceRepository.kt
sed -i '/val debtLoanDao/d' app/src/main/java/com/example/data/FinanceRepository.kt

# Replace Asset types
sed -i 's/val sampleAssets = listOf(/val sampleAssets = listOf(\n                Asset(name = "طلا ۱۸ عیار", type = AssetType.GOLD_18K, totalQuantity = 25.5, currentUnitPriceToman = 4650000, unit = "گرم"),\n                Asset(name = "سکه طرح جدید (امامی)", type = AssetType.COIN_EMAMI, totalQuantity = 2.0, currentUnitPriceToman = 52000000, unit = "عدد"),\n                Asset(name = "دلار آمریکا", type = AssetType.CURRENCY_USD, totalQuantity = 1500.0, currentUnitPriceToman = 68500, unit = "دلار"),\n                Asset(name = "تتر (USDT)", type = AssetType.CRYPTO_USDT, totalQuantity = 800.0, currentUnitPriceToman = 68700, unit = "USDT")\n            )/g' app/src/main/java/com/example/data/FinanceRepository.kt
sed -i '/Asset(name = "طلا ۱۸ عیار", type = AssetType.GOLD/d' app/src/main/java/com/example/data/FinanceRepository.kt
sed -i '/Asset(name = "سکه طرح جدید/d' app/src/main/java/com/example/data/FinanceRepository.kt
sed -i '/Asset(name = "دلار آمریکا", type = AssetType.CURRENCY/d' app/src/main/java/com/example/data/FinanceRepository.kt
sed -i '/Asset(name = "تتر (USDT)", type = AssetType.CRYPTO/d' app/src/main/java/com/example/data/FinanceRepository.kt
sed -i '/Asset(name = "بیت‌کوین (BTC)", type = AssetType.CRYPTO/d' app/src/main/java/com/example/data/FinanceRepository.kt
