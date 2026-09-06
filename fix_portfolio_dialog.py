import re
with open('app/src/main/java/com/example/ui/screens/PortfolioScreen.kt', 'r') as f:
    content = f.read()

states = """    var selectedAssetForUpdate by remember { mutableStateOf<Asset?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        com.example.ui.components.AddAssetBottomSheet(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, quantity, priceToman, unit ->
                portfolioViewModel.addAsset(name, type, quantity, priceToman, unit)
                showAddDialog = false
            }
        )
    }"""
content = content.replace("    var selectedAssetForUpdate by remember { mutableStateOf<Asset?>(null) }", states)

with open('app/src/main/java/com/example/ui/screens/PortfolioScreen.kt', 'w') as f:
    f.write(content)
