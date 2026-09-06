import re
with open('app/src/main/java/com/example/ui/screens/PortfolioScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("FloatingActionButton(onClick = { /* TODO: Add new Asset */ }) {", "FloatingActionButton(onClick = { showAddDialog = true }) {")

state_addition = """    val isPremium by viewModel.isPremium.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        com.example.ui.components.AddAssetBottomSheet(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, quantity, priceToman, unit ->
                viewModel.addAsset(name, type, quantity, priceToman, unit)
                showAddDialog = false
            }
        )
    }
"""
content = content.replace("    val isPremium by viewModel.isPremium.collectAsState()", state_addition)

with open('app/src/main/java/com/example/ui/screens/PortfolioScreen.kt', 'w') as f:
    f.write(content)
