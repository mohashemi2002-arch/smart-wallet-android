import re

with open('app/src/main/java/com/example/ui/viewmodel/PendingSmsViewModel.kt', 'r') as f:
    content = f.read()

# Replace approveAll and update approveTransaction / rejectTransaction to match requirements

replacement = """    fun approveTransaction(transaction: Transaction, newCardNumber: String?, newCategory: String) {
        viewModelScope.launch {
            val updated = transaction.copy(
                cardNumber = newCardNumber ?: transaction.cardNumber,
                categoryName = newCategory
            )
            repository.approvePendingTransaction(updated)
        }
    }

    fun rejectTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun approveAllPending(onComplete: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val pendingList = pendingTransactions.value
                pendingList.forEach { transaction ->
                    repository.approvePendingTransaction(transaction)
                }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onComplete() }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onError(e.localizedMessage ?: "خطا در تایید دسته جمعی") }
            }
        }
    }

    fun approveAll(pendingList: List<Transaction>) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            pendingList.forEach { tx ->
                repository.approvePendingTransaction(tx)
            }
        }
    }"""

content = re.sub(r'    fun approveTransaction\(transaction: Transaction, newCardNumber: String\?, newCategory: String\) \{[\s\S]*?\}\n    \}\n\}', replacement + "\n}", content)

with open('app/src/main/java/com/example/ui/viewmodel/PendingSmsViewModel.kt', 'w') as f:
    f.write(content)

