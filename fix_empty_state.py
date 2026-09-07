with open('app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt', 'r') as f:
    content = f.read()

target = r'''                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Text("همه پیامک‌ها بررسی شدند.\nصندوق خالی است.", textAlign = TextAlign.Center, color = Color.LightGray)
                }'''

replacement = r'''                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Text("همه پیامک‌ها بررسی شدند.\nصندوق خالی است.", textAlign = TextAlign.Center, color = Color.LightGray)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = onNavigateToHistory,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5C82E6))
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مشاهده کل پیامک‌های بانکی")
                    }
                }'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt', 'w') as f:
    f.write(content)
