import re

with open('app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt', 'r') as f:
    content = f.read()

target = r'''fun PendingSmsInboxScreen\(
    viewModel: PendingSmsViewModel,
    onNavigateBack: \(\) -> Unit
\) \{'''

replacement = r'''fun PendingSmsInboxScreen(
    viewModel: PendingSmsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit = {}
) {'''

content = re.sub(target, replacement, content)

target_topbar = r'''                        TextButton\(onClick = \{ viewModel\.approveAllPending\(onComplete = \{\}, onError = \{\}\) \}\) \{
                            Text\("تایید همه", color = Color\(0xFF5C82E6\), fontWeight = FontWeight\.Bold\)
                        \}
                    \}
                \}'''

replacement_topbar = r'''                        TextButton(onClick = { viewModel.approveAllPending(onComplete = {}, onError = {}) }) {
                            Text("تایید همه", color = Color(0xFF5C82E6), fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(Icons.Default.History, contentDescription = "تاریخچه پیامک‌های بانکی")
                        }
                    }
                }'''

content = re.sub(target_topbar, replacement_topbar, content)

with open('app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt', 'w') as f:
    f.write(content)
