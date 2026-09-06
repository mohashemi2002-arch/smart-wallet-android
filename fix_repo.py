with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

import re

# We want to replace everything from "val transactions = transactionDao.getAllTransactions().first()" to the end of seedInitialDataIfNeeded
# Let's find "private suspend fun seedInitialDataIfNeeded() {" up to its closing brace.
start_idx = content.find("private suspend fun seedInitialDataIfNeeded() {")
end_idx = content.find("suspend fun processIncomingSms")
if start_idx != -1 and end_idx != -1:
    new_method = """private suspend fun seedInitialDataIfNeeded() {
        val categories = categoryDao.getAllCategories().first()
        if (categories.isEmpty()) {
            val defaultCategories = listOf(
                Category(name = "خوراکی و سوپرمارکت", budgetLimitToman = 12000000, colorHex = "#FF5722", iconName = "restaurant"),
                Category(name = "حمل و نقل", budgetLimitToman = 4000000, colorHex = "#0288D1", iconName = "directions_car"),
                Category(name = "مسکن و اجاره", budgetLimitToman = 25000000, colorHex = "#7B1FA2", iconName = "home"),
                Category(name = "قبوض و خدمات", budgetLimitToman = 3000000, colorHex = "#00897B", iconName = "receipt"),
                Category(name = "سلامت و درمان", budgetLimitToman = 5000000, colorHex = "#E91E63", iconName = "local_hospital"),
                Category(name = "پوشاک و خرید", budgetLimitToman = 8000000, colorHex = "#F57C00", iconName = "shopping_bag"),
                Category(name = "تفریح و سرگرمی", budgetLimitToman = 6000000, colorHex = "#3F51B5", iconName = "movie"),
                Category(name = "پس‌انداز و سرمایه", budgetLimitToman = 15000000, colorHex = "#388E3C", iconName = "savings"),
                Category(name = "متفرقه", budgetLimitToman = 5000000, colorHex = "#607D8B", iconName = "category")
            )
            categoryDao.insertAll(defaultCategories)
        }
    }

    """
    content = content[:start_idx] + new_method + content[end_idx:]
    with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
        f.write(content)
