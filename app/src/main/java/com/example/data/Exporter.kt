package com.example.data

import com.example.data.model.Asset
import com.example.data.model.DebtLoan
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Exporter {

    /**
     * Generates a CSV/Excel-compatible formatted string containing all transactions, assets, and debts.
     */
    fun generateExportCsv(
        transactions: List<Transaction>,
        assets: List<Asset>,
        debtLoans: List<DebtLoan>
    ): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)

        sb.append("=== گزارش مالی پولکی AI (Poolaki AI Financial Export) ===\n")
        sb.append("تاریخ خروجی: ").append(dateFormat.format(Date())).append("\n\n")

        // 1. Transactions Table
        sb.append("--- تراکنش‌ها (Transactions) ---\n")
        sb.append("شناسه,نوع,مبلغ (تومان),دسته‌بندی,فروشگاه/طرف حساب,شماره کارت,تاریخ,منبع\n")
        for (t in transactions) {
            val typeStr = if (t.type == TransactionType.EXPENSE) "هزینه" else "درآمد"
            val dateStr = dateFormat.format(Date(t.dateMillis))
            val sourceStr = if (t.isParsedFromSms) "پیامک بانکی" else "دستی"
            sb.append("${t.id},\"$typeStr\",${t.amountToman},\"${t.categoryName}\",\"${t.merchantName}\",\"${t.cardNumber ?: ""}\",\"$dateStr\",\"$sourceStr\"\n")
        }

        sb.append("\n")

        // 2. Non-cash Assets Portfolio Table
        sb.append("--- دارایی‌های غیرنقدی (Assets Portfolio) ---\n")
        sb.append("نام دارایی,نوع,مقدار,واحد,قیمت واحد (تومان),ارزش کل (تومان)\n")
        for (a in assets) {
            sb.append("\"${a.name}\",\"${a.type}\",${a.quantity},\"${a.unit}\",${a.currentPriceToman},${a.totalValueToman}\n")
        }

        sb.append("\n")

        // 3. Debts, Loans & Checks Table
        sb.append("--- اقساط، چک‌ها و بدهی‌ها (Loans, Checks & Debts) ---\n")
        sb.append("عنوان,نوع,مبلغ (تومان),تاریخ سررسید,وضعیت پرداخت,طرف حساب\n")
        for (d in debtLoans) {
            val statusStr = if (d.isPaid) "پرداخت شده" else "در انتظار پرداخت"
            val dueDateStr = dateFormat.format(Date(d.dueDateMillis))
            sb.append("\"${d.title}\",\"${d.type}\",${d.amountToman},\"$dueDateStr\",\"$statusStr\",\"${d.contactName ?: ""}\"\n")
        }

        return sb.toString()
    }
}
