package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class TransactionCategory(
    val id: String,
    val name: String,
    val type: String, // "EXPENSE" or "INCOME"
    val iconKey: String,
    val colorHex: String,
    val isCustom: Boolean = false
)

class CategoryRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _categoriesFlow = MutableStateFlow<List<TransactionCategory>>(emptyList())
    val categoriesFlow: StateFlow<List<TransactionCategory>> = _categoriesFlow.asStateFlow()

    companion object {
        private const val PREFS_NAME = "smart_wallet_categories_pref"
        private const val KEY_CUSTOM_CATEGORIES = "custom_categories_json"

        val DEFAULT_EXPENSE_CATEGORIES = listOf(
            TransactionCategory("exp_cash", "نقدی", "EXPENSE", "cash", "#F59E0B"),
            TransactionCategory("exp_food", "خوردنی", "EXPENSE", "food", "#F97316"),
            TransactionCategory("exp_fuel", "بنزین", "EXPENSE", "fuel", "#EF4444"),
            TransactionCategory("exp_car", "خودرو", "EXPENSE", "car", "#06B6D4"),
            TransactionCategory("exp_home", "خونه", "EXPENSE", "home", "#3B82F6"),
            TransactionCategory("exp_toll", "عوارض آزادراهی", "EXPENSE", "toll", "#EC4899"),
            TransactionCategory("exp_smoke", "سیگار", "EXPENSE", "smoke", "#A855F7"),
            TransactionCategory("exp_shopping", "پوشاک", "EXPENSE", "shopping", "#8B5CF6"),
            TransactionCategory("exp_health", "سلامت و درمان", "EXPENSE", "health", "#10B981"),
            TransactionCategory("exp_bills", "قبوض و خدمات", "EXPENSE", "receipt", "#14B8A6"),
            TransactionCategory("exp_edu", "آموزش", "EXPENSE", "school", "#6366F1"),
            TransactionCategory("exp_entertainment", "تفریح و سرگرمی", "EXPENSE", "entertainment", "#3B82F6")
        )

        val DEFAULT_INCOME_CATEGORIES = listOf(
            TransactionCategory("inc_salary", "حقوق", "INCOME", "payments", "#22C55E"),
            TransactionCategory("inc_reward", "پاداش", "INCOME", "gift", "#10B981"),
            TransactionCategory("inc_other", "سایر دخل‌ها", "INCOME", "bank", "#14B8A6"),
            TransactionCategory("inc_cash", "نقدی", "INCOME", "cash", "#F59E0B"),
            TransactionCategory("inc_father", "پدر", "INCOME", "heart", "#22C55E"),
            TransactionCategory("inc_transfer", "جابجایی داخلی", "INCOME", "transfer", "#3B82F6"),
            TransactionCategory("inc_invest", "سود سرمایه‌گذاری", "INCOME", "savings", "#84CC16")
        )
    }

    init {
        loadCategories()
    }

    private fun loadCategories() {
        val customList = mutableListOf<TransactionCategory>()
        val jsonStr = prefs.getString(KEY_CUSTOM_CATEGORIES, null)
        if (!jsonStr.isNullOrBlank()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    customList.add(
                        TransactionCategory(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            type = obj.optString("type", "EXPENSE"),
                            iconKey = obj.optString("iconKey", "snowflake"),
                            colorHex = obj.optString("colorHex", "#84CC16"),
                            isCustom = true
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore parse errors and fallback
            }
        }

        val all = mutableListOf<TransactionCategory>()
        all.addAll(DEFAULT_EXPENSE_CATEGORIES)
        all.addAll(DEFAULT_INCOME_CATEGORIES)
        all.addAll(customList)
        _categoriesFlow.value = all
    }

    fun getCategories(type: String): List<TransactionCategory> {
        return _categoriesFlow.value.filter { it.type.equals(type, ignoreCase = true) }
    }

    fun findCategoryByName(name: String, type: String? = null): TransactionCategory? {
        val list = _categoriesFlow.value
        return if (type != null) {
            list.firstOrNull { it.name == name && it.type.equals(type, ignoreCase = true) }
                ?: list.firstOrNull { it.name == name }
        } else {
            list.firstOrNull { it.name == name }
        }
    }

    fun saveCategory(
        id: String? = null,
        name: String,
        type: String,
        iconKey: String,
        colorHex: String
    ): TransactionCategory {
        val cleanName = name.trim()
        val customList = getCustomCategories().toMutableList()

        val categoryId = id ?: "cust_${System.currentTimeMillis()}"
        val existingIndex = customList.indexOfFirst { it.id == categoryId }

        val newCat = TransactionCategory(
            id = categoryId,
            name = cleanName,
            type = type,
            iconKey = iconKey,
            colorHex = colorHex,
            isCustom = true
        )

        if (existingIndex >= 0) {
            customList[existingIndex] = newCat
        } else {
            customList.add(newCat)
        }

        saveCustomCategories(customList)
        loadCategories()
        return newCat
    }

    fun deleteCategory(id: String) {
        val customList = getCustomCategories().filterNot { it.id == id }
        saveCustomCategories(customList)
        loadCategories()
    }

    private fun getCustomCategories(): List<TransactionCategory> {
        return _categoriesFlow.value.filter { it.isCustom }
    }

    private fun saveCustomCategories(list: List<TransactionCategory>) {
        val array = JSONArray()
        list.forEach { cat ->
            array.put(JSONObject().apply {
                put("id", cat.id)
                put("name", cat.name)
                put("type", cat.type)
                put("iconKey", cat.iconKey)
                put("colorHex", cat.colorHex)
            })
        }
        prefs.edit().putString(KEY_CUSTOM_CATEGORIES, array.toString()).apply()
    }
}
