with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                    onClick = {
                        // TODO: Call DB wipe (e.g., db.clearAllTables())
                        Toast.makeText(context, "داده‌ها با موفقیت حذف شدند.", Toast.LENGTH_SHORT).show()
                        showWipeDialog = false
                    },""",
"""                    onClick = {
                        settingsViewModel.wipeEntireDatabase {
                            Toast.makeText(context, "داده‌ها با موفقیت حذف شدند.", Toast.LENGTH_SHORT).show()
                            showWipeDialog = false
                        }
                    },"""
)
with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
