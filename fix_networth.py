with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "    val totalNetWorthToman: StateFlow<Long> = combine(totalAssetsValueToman, totalIncomeToman, totalExpenseToman) { assetsVal, income, expense ->\n        assetsVal + income - expense\n    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)",
    """    val totalBankBalances: StateFlow<Long> = bankAccounts.map { list ->
        list.sumOf { it.balanceToman }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalNetWorthToman: StateFlow<Long> = combine(totalAssetsValueToman, totalBankBalances) { assetsVal, bankBal ->
        assetsVal + bankBal
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)"""
)

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
