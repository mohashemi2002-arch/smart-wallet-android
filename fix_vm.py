with open('app/src/main/java/com/example/ui/viewmodel/PendingSmsViewModel.kt', 'r') as f:
    content = f.read()

old_approve = """    fun approveTransaction(transaction: Transaction, newCardNumber: String?, newCategory: String) {
        viewModelScope.launch {
            val updated = transaction.copy(
                cardNumber = newCardNumber ?: transaction.cardNumber,
                categoryName = newCategory,
                status = com.example.data.model.TransactionStatus.CONFIRMED
            )
            repository.approvePendingTransaction(updated)
        }
    }"""

new_approve = """    fun approveTransaction(transaction: Transaction, newCardNumber: String?, newCategory: String) {
        viewModelScope.launch {
            val updated = transaction.copy(
                cardNumber = newCardNumber ?: transaction.cardNumber,
                categoryName = newCategory
                // Do NOT set status here, let repository handle it to prevent double confirmation issues if we add a check
            )
            repository.approvePendingTransaction(updated)
        }
    }"""

old_approve_all = """    fun approveAll(pendingList: List<Transaction>) {
        viewModelScope.launch {
            pendingList.forEach { tx ->
                repository.approvePendingTransaction(tx.copy(status = com.example.data.model.TransactionStatus.CONFIRMED))
            }
        }
    }"""

new_approve_all = """    fun approveAll(pendingList: List<Transaction>) {
        viewModelScope.launch {
            pendingList.forEach { tx ->
                repository.approvePendingTransaction(tx)
            }
        }
    }"""

content = content.replace(old_approve, new_approve)
content = content.replace(old_approve_all, new_approve_all)

with open('app/src/main/java/com/example/ui/viewmodel/PendingSmsViewModel.kt', 'w') as f:
    f.write(content)
