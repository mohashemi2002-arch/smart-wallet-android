with open('app/src/main/java/com/example/ui/viewmodel/SettingsViewModel.kt', 'r') as f:
    content = f.read()

import_statement = """import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.data.database.AppDatabase"""

content = content.replace("import kotlinx.coroutines.launch", "import kotlinx.coroutines.launch\n" + import_statement)

method = """
    fun wipeEntireDatabase(onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(getApplication()).clearAllTables()
            }
            onComplete()
        }
    }
}"""
content = content.replace("    }\n}", "    }\n" + method)

with open('app/src/main/java/com/example/ui/viewmodel/SettingsViewModel.kt', 'w') as f:
    f.write(content)
