import re

with open('app/src/main/java/com/example/ui/screens/BankSmsHistoryScreen.kt', 'r') as f:
    content = f.read()

target = r'''@Composable
fun BankSmsHistoryScreen\('''

replacement = r'''import com.example.ui.components.PendingReviewBottomSheet
import com.example.data.model.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankSmsHistoryScreen('''

content = re.sub(target, replacement, content)

target_body = r'''    val isScanning by viewModel.isScanning.collectAsState\(\)'''

replacement_body = r'''    val isScanning by viewModel.isScanning.collectAsState()
    val scope = rememberCoroutineScope()
    var transactionToReview by remember { mutableStateOf<Transaction?>(null) }'''

content = re.sub(target_body, replacement_body, content)

target_ingest = r'''                        BankSmsHistoryCard\(
                            item = sms,
                            onIngestClick = \{ onManualIngest\(sms\) \}
                        \)'''

replacement_ingest = r'''                        BankSmsHistoryCard(
                            item = sms,
                            onIngestClick = {
                                scope.launch {
                                    val parsedTx = viewModel.parseSmsManual(sms)
                                    if (parsedTx != null) {
                                        transactionToReview = parsedTx
                                    }
                                }
                            }
                        )'''
content = re.sub(target_ingest, replacement_ingest, content)

target_end = r'''            }
        }
    }
}

@Composable'''

replacement_end = r'''            }
        }
    }
    
    transactionToReview?.let { tx ->
        PendingReviewBottomSheet(
            transaction = tx,
            onDismiss = { transactionToReview = null },
            onConfirm = { updatedTx ->
                viewModel.insertTransaction(updatedTx)
                transactionToReview = null
            },
            onIgnore = {
                transactionToReview = null
            }
        )
    }
}

@Composable'''

content = re.sub(target_end, replacement_end, content)

with open('app/src/main/java/com/example/ui/screens/BankSmsHistoryScreen.kt', 'w') as f:
    f.write(content)
