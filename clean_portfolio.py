import re
with open('app/src/main/java/com/example/ui/screens/PortfolioScreen.kt', 'r') as f:
    content = f.read()

# Make sure `showAddDialog` is declared inside `PortfolioScreen`
content = content.replace("    var selectedAssetForUpdate by remember { mutableStateOf<Asset?>(null) }", "    var selectedAssetForUpdate by remember { mutableStateOf<Asset?>(null) }\n    var showAddDialog by remember { mutableStateOf(false) }\n")

# Remove the one that is randomly floating in the middle
content = re.sub(r"    val isPremium by viewModel\.isPremium\.collectAsState\(\)\n    var showAddDialog by remember \{ mutableStateOf\(false\) \}.*?showAddDialog = false\n            \}\n        \)\n    \}", "", content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/PortfolioScreen.kt', 'w') as f:
    f.write(content)
