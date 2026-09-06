with open('app/src/main/java/com/example/ui/viewmodel/PortfolioViewModel.kt', 'r') as f:
    content = f.read()

add_asset_code = """
    fun addAsset(name: String, type: com.example.data.model.AssetType, quantity: Double, priceToman: Long, unit: String) {
        viewModelScope.launch {
            val asset = Asset(
                name = name,
                type = type,
                totalQuantity = quantity,
                currentUnitPriceToman = priceToman,
                unit = unit
            )
            assetDao.insert(asset)
        }
    }
"""

content = content.replace("    fun updateAssetPrice(", add_asset_code + "\n    fun updateAssetPrice(")

with open('app/src/main/java/com/example/ui/viewmodel/PortfolioViewModel.kt', 'w') as f:
    f.write(content)
