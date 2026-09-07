import re

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

target = "private val marketRepository = MarketRepository.getInstance()"
replacement = """private val database = AppDatabase.getDatabase(application)
    private val marketRepository = MarketRepository.getInstance(database.marketRateDao(), database.assetDao())"""

content = content.replace(target, replacement)

# We also need to fix references to marketRepository inside FinanceViewModel.
# Previously it had marketRepository.rates, marketRepository.isRefreshing, etc.
# But now we only have getCachedRates() returning Flow<List<MarketRateEntity>>.
with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
