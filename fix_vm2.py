import re

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

target = '''    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>>'''

replacement = '''    val allTransactions: StateFlow<List<Transaction>> = repository.confirmedTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>>'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
