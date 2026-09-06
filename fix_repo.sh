#!/bin/bash
awk '
/val sampleAssets = listOf\(/ {
    print "            val sampleAssets = listOf("
    print "                Asset(name = \"طلا ۱۸ عیار\", type = AssetType.GOLD_18K, totalQuantity = 25.5, currentUnitPriceToman = 4650000, unit = \"گرم\"),"
    print "                Asset(name = \"سکه امامی\", type = AssetType.COIN_EMAMI, totalQuantity = 2.0, currentUnitPriceToman = 52000000, unit = \"عدد\")"
    print "            )"
    print "            assetDao.insertAll(sampleAssets)"
    in_assets = 1
    next
}
/assetDao.insertAll\(sampleAssets\)/ {
    if (in_assets) {
        in_assets = 0
        next
    }
}
{
    if (!in_assets && $0 !~ /^\s*\)\s*$/) {
        print
    }
}
' app/src/main/java/com/example/data/FinanceRepository.kt > temp.kt && mv temp.kt app/src/main/java/com/example/data/FinanceRepository.kt
