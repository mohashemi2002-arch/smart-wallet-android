import re

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

# Add updateBankAccount
new_method = """    fun updateBankAccount(bankAccount: BankAccount) {
        viewModelScope.launch {
            repository.updateBankAccount(bankAccount)
            _statusMessage.value = "اطلاعات کارت با موفقیت بروزرسانی شد."
        }
    }

    fun deleteBankAccount(bankAccount: BankAccount)"""

content = content.replace("    fun deleteBankAccount(bankAccount: BankAccount)", new_method)

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
