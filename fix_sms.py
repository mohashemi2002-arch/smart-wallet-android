import re

files = [
    'app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt',
    'app/src/main/java/com/example/ui/components/PendingReviewBottomSheet.kt',
    'app/src/main/java/com/example/ui/components/AllTransactionsDialog.kt',
    'app/src/main/java/com/example/ui/screens/ReportsScreen.kt',
    'app/src/main/java/com/example/ui/screens/SmsReaderScreen.kt',
    'app/src/main/java/com/example/ui/screens/DebtsChecksScreen.kt',
    'app/src/main/java/com/example/ui/screens/BudgetScreen.kt',
    'app/src/main/java/com/example/ui/screens/MarketRatesScreen.kt',
    'app/src/main/java/com/example/ui/components/BankCardItem.kt'
]

for file in files:
    try:
        with open(file, 'r') as f:
            content = f.read()

        content = re.sub(r'PersianFormatters\.formatMoney\(\s*([a-zA-Z0-9_\.\(\)]+)\s*,\s*currencyUnit\s*\)', r'\1.formatAsToman() + if (currencyUnit == com.example.ui.components.CurrencyUnit.RIAL) " ریال" else " تومان"', content)
        content = re.sub(r'PersianFormatters\.formatMoney\(\s*([a-zA-Z0-9_\.\(\)]+)\s*\)', r'\1.formatAsToman() + " تومان"', content)
        
        with open(file, 'w') as f:
            f.write(content)
            
    except Exception as e:
        print(f"Error in {file}: {e}")

