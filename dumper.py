import json
import os

files = [
    "app/src/main/java/com/example/data/dao/BankAccountDao.kt",
    "app/src/main/java/com/example/data/dao/TransactionDao.kt",
    "app/src/main/java/com/example/data/FinanceRepository.kt",
    "app/src/main/java/com/example/ui/viewmodel/PendingSmsViewModel.kt",
    "app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt"
]

out = {}
for f in files:
    with open(f, 'r') as fh:
        out[f] = fh.read()

with open('dump.json', 'w') as fh:
    json.dump(out, fh)
