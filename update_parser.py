with open('app/src/main/java/com/example/data/SmsAiParser.kt', 'r') as f:
    content = f.read()

content = content.replace("fun parseSmsWithRegex(smsText: String): ParsedSmsResult {", "fun parseSmsWithRegex(smsText: String, senderAddress: String? = null): ParsedSmsResult {")
content = content.replace("suspend fun parseSms(smsText: String, apiKey: String = \"\"): ParsedSmsResult {", "suspend fun parseSms(smsText: String, apiKey: String = \"\", senderAddress: String? = null): ParsedSmsResult {")
content = content.replace("return parseSmsWithRegex(smsText)", "return parseSmsWithRegex(smsText, senderAddress)")

melli_check = """        val isMelli = normalizedText.contains("بانک ملی") || normalizedText.contains("Melli") || normalizedText.contains("بانک ملي")"""
melli_check_new = """        val isMelliSender = senderAddress != null && (senderAddress.contains("30009417") || senderAddress.contains("09830009417"))
        val isMelliText = normalizedText.contains("بانک ملی") || normalizedText.contains("Melli") || normalizedText.contains("بانک ملي")
        val isMelli = isMelliSender || isMelliText"""

content = content.replace(melli_check, melli_check_new)

with open('app/src/main/java/com/example/data/SmsAiParser.kt', 'w') as f:
    f.write(content)
