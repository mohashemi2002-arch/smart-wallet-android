import re

with open('app/src/main/java/com/example/data/dao/TransactionDao.kt', 'r') as f:
    content = f.read()

content = content.replace('fun getConfirmedTransactionsBetween(startMillis: Long, endMillis: Long)', 'fun getConfirmedTransactionsInRange(startMillis: Long, endMillis: Long)')

with open('app/src/main/java/com/example/data/dao/TransactionDao.kt', 'w') as f:
    f.write(content)


with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    repo_content = f.read()

repo_content = repo_content.replace('fun getConfirmedTransactionsBetween(startMillis: Long, endMillis: Long)', 'fun getConfirmedTransactionsInRange(startMillis: Long, endMillis: Long)')
repo_content = repo_content.replace('transactionDao.getConfirmedTransactionsBetween(startMillis, endMillis)', 'transactionDao.getConfirmedTransactionsInRange(startMillis, endMillis)')

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(repo_content)
