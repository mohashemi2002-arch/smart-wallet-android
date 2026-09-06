import re
with open('app/src/main/java/com/example/ui/viewmodel/ReportsViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "transactionDao.getAllTransactions()",
    "transactionDao.getConfirmedTransactionsFlow()"
)

with open('app/src/main/java/com/example/ui/viewmodel/ReportsViewModel.kt', 'w') as f:
    f.write(content)
