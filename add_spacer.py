import re
with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Add a spacer at the end of LazyColumn
content = content.replace("                            }\n                        }\n                    }\n                }\n            }\n        }\n    }", "                            }\n                        }\n                    }\n                }\n            }\n            item { Spacer(modifier = Modifier.height(96.dp)) }\n        }\n    }")
with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
