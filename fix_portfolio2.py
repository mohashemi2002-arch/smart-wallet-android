with open('app/src/main/java/com/example/ui/screens/PortfolioScreen.kt', 'r') as f:
    content = f.read()

dialog_code = """
    if (showAddDialog) {
        com.example.ui.components.AddAssetBottomSheet(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, quantity, priceToman, unit ->
                portfolioViewModel.addAsset(name, type, quantity, priceToman, unit)
                showAddDialog = false
            }
        )
    }
}
"""

content = content.replace("    }\n}", dialog_code)

with open('app/src/main/java/com/example/ui/screens/PortfolioScreen.kt', 'w') as f:
    f.write(content)
