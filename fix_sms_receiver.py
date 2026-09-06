with open('app/src/main/java/com/example/data/SmsReceiver.kt', 'r') as f:
    content = f.read()

new_on_receive = """    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val repository = FinanceRepository.getInstance(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                val activeSenders = repository.bankSmsSenderDao.getActiveSenders()
                
                for (sms in messages) {
                    val body = sms.messageBody ?: continue
                    val sender = sms.originatingAddress ?: ""
                    
                    // Check if sender is explicitly in the DB, OR if the DB is empty (fallback to old logic)
                    val isMappedSender = activeSenders.any { sender.contains(it.senderNumber) || it.senderNumber.contains(sender) }
                    
                    if (isMappedSender || (activeSenders.isEmpty() && isBankSmsFallback(body, sender))) {
                        Log.d("SmsReceiver", "Bank SMS detected from $sender: $body")
                        repository.processIncomingSms(body, sender)
                        showNotification(context)
                    }
                }
            }
        }
    }"""

import re
# Replace onReceive function
content = re.sub(r'override fun onReceive.*?    }', new_on_receive, content, flags=re.DOTALL)
content = content.replace("private fun isBankSms(", "private fun isBankSmsFallback(")

with open('app/src/main/java/com/example/data/SmsReceiver.kt', 'w') as f:
    f.write(content)
