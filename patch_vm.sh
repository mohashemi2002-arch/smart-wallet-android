#!/bin/bash
sed -i '/val debtLoans:/d' app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
sed -i '/fun addDebtLoan/,/    }/d' app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
sed -i '/fun toggleDebtPaid/,/    }/d' app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
sed -i '/fun deleteDebtLoan/,/    }/d' app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
# Also check assets changes
sed -i 's/val quantity:/val totalQuantity:/g' app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
sed -i 's/quantity = quantity/totalQuantity = totalQuantity/g' app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
sed -i 's/currentPriceToman = priceToman/currentUnitPriceToman = priceToman/g' app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
