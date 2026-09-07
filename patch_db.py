with open('app/src/main/java/com/example/data/database/AppDatabase.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'BankSmsSender::class\n    ],',
    'BankSmsSender::class,\n        MarketRateEntity::class\n    ],'
)
content = content.replace('version = 7', 'version = 8')
content = content.replace(
    'abstract fun bankSmsSenderDao(): BankSmsSenderDao',
    'abstract fun bankSmsSenderDao(): BankSmsSenderDao\n    abstract fun marketRateDao(): MarketRateDao'
)

with open('app/src/main/java/com/example/data/database/AppDatabase.kt', 'w') as f:
    f.write(content)
