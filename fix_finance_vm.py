with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

replacement = """    val transactions: StateFlow<List<Transaction>> = repository.confirmedTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTransactions: StateFlow<List<Transaction>> = repository.pendingTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())"""

start_idx = content.find("    val transactions: StateFlow<List<Transaction>> = repository.allTransactions")
end_idx = content.find("    val categories: StateFlow<List<Category>>", start_idx)

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + replacement + "\n\n" + content[end_idx:]
    with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
        f.write(content)
