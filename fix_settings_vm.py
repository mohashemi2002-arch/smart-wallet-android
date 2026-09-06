import re

with open('app/src/main/java/com/example/ui/viewmodel/SettingsViewModel.kt', 'r') as f:
    content = f.read()

target = '''    fun wipeEntireDatabase(onComplete: () -> Unit) {'''

replacement = '''    fun importPoolakiExcel(uri: android.net.Uri, context: android.content.Context, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val rows = withContext(Dispatchers.IO) {
                    com.example.data.PoolakiExcelImporter.parse(context, uri)
                }
                
                if (rows.isEmpty()) {
                    onResult("فایلی انتخاب نشد یا فرمت پشتیبانی نمی‌شود.")
                    return@launch
                }
                
                val (txCount, accCount) = withContext(Dispatchers.IO) {
                    com.example.data.FinanceRepository.getInstance(context).importPoolakiExcel(rows)
                }
                onResult("$txCount تراکنش و $accCount حساب بانکی با موفقیت وارد شدند.")
            } catch (e: Exception) {
                e.printStackTrace()
                onResult("خطا در پردازش فایل: ${e.localizedMessage}")
            }
        }
    }

    fun wipeEntireDatabase(onComplete: () -> Unit) {'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/viewmodel/SettingsViewModel.kt', 'w') as f:
    f.write(content)
