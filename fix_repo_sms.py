with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

content = content.replace("val result = SmsAiParser.parseSms(smsText)", "val result = SmsAiParser.parseSms(smsText, senderAddress = senderAddress)")

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(content)
