import os
import re

files_to_check = [
    "app/src/main/java/com/example/data/database/AppDatabase.kt",
    "app/src/main/java/com/example/data/FinanceRepository.kt",
    "app/src/main/java/com/example/data/PdfReportGenerator.kt",
    "app/src/main/java/com/example/data/Exporter.kt",
    "app/src/main/java/com/example/data/PoolakiExcelImporter.kt",
    "app/src/main/java/com/example/ui/screens/BudgetScreen.kt",
    "app/src/main/java/com/example/ui/screens/SettingsScreen.kt",
    "app/src/main/java/com/example/ui/screens/DashboardScreen.kt",
    "app/src/main/java/com/example/ui/screens/SmsReaderScreen.kt",
    "app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt",
    "app/src/main/java/com/example/ui/viewmodel/SettingsViewModel.kt",
    "app/src/main/java/com/example/ui/components/Formatters.kt",
    "app/src/main/java/com/example/ui/theme/Theme.kt",
    "app/src/main/java/com/example/domain/worker/FinancialReminderWorker.kt",
    "app/src/main/java/com/example/MainActivity.kt",
    "app/src/main/res/values/strings.xml",
    "app/build.gradle.kts",
    "settings.gradle.kts",
    "metadata.json"
]

for file_path in files_to_check:
    if not os.path.exists(file_path):
        continue
    with open(file_path, 'r') as f:
        content = f.read()

    # Replacing variations
    content = content.replace("PoolakiPlusBadge", "SmartWalletPlusBadge")
    content = content.replace("poolaki_db", "smart_wallet_db")
    content = content.replace("Poolaki Export", "Smart Wallet Export")
    content = content.replace("Poolaki AI", "Smart Wallet")
    content = content.replace("Poolaki", "Smart Wallet")
    content = content.replace("poolaki", "smart wallet")
    content = content.replace("پولکی", "اسمارت ولت")
    content = content.replace("Smart WalletExcelImporter", "SmartWalletExcelImporter")
    content = content.replace("Smart WalletExcelRow", "SmartWalletExcelRow")
    content = content.replace("importSmart WalletExcel", "importSmartWalletExcel")
    
    with open(file_path, 'w') as f:
        f.write(content)

os.rename("app/src/main/java/com/example/data/PoolakiExcelImporter.kt", "app/src/main/java/com/example/data/SmartWalletExcelImporter.kt")
