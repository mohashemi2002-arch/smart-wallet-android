package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.PersianFormatters
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {
    fun generateAndSharePdf(
        context: Context,
        transactions: List<Transaction>,
        totalInflow: Long,
        totalOutflow: Long,
        netBalance: Long
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        var page = document.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        val titlePaint = Paint(paint).apply {
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        var yPos = 50f
        
        // Title
        canvas.drawText("گزارش مالی اسمارت ولت", pageInfo.pageWidth / 2f, yPos, titlePaint)
        yPos += 40f

        // Summary
        paint.textSize = 14f
        canvas.drawText("تاریخ گزارش: ${PersianFormatters.formatDate(System.currentTimeMillis())}", pageInfo.pageWidth - 50f, yPos, paint)
        yPos += 30f
        canvas.drawText("کل درآمد: ${PersianFormatters.formatMoney(totalInflow)}", pageInfo.pageWidth - 50f, yPos, paint)
        yPos += 25f
        canvas.drawText("کل هزینه: ${PersianFormatters.formatMoney(totalOutflow)}", pageInfo.pageWidth - 50f, yPos, paint)
        yPos += 25f
        canvas.drawText("تراز خالص: ${PersianFormatters.formatMoney(netBalance)}", pageInfo.pageWidth - 50f, yPos, paint)
        yPos += 40f

        // Table Header
        paint.isFakeBoldText = true
        val colWidths = listOf(100f, 150f, 120f, 175f) // Date, Category, Amount, Type
        
        canvas.drawText("تاریخ", pageInfo.pageWidth - 50f, yPos, paint)
        canvas.drawText("دسته‌بندی", pageInfo.pageWidth - 50f - colWidths[0], yPos, paint)
        canvas.drawText("مبلغ", pageInfo.pageWidth - 50f - colWidths[0] - colWidths[1], yPos, paint)
        canvas.drawText("نوع", pageInfo.pageWidth - 50f - colWidths[0] - colWidths[1] - colWidths[2], yPos, paint)
        yPos += 20f
        
        canvas.drawLine(50f, yPos, pageInfo.pageWidth - 50f, yPos, paint)
        yPos += 20f

        // Table Rows
        paint.isFakeBoldText = false
        for (tx in transactions) {
            if (yPos > 800f) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f
            }

            canvas.drawText(PersianFormatters.formatDate(tx.dateMillis), pageInfo.pageWidth - 50f, yPos, paint)
            canvas.drawText(tx.categoryName, pageInfo.pageWidth - 50f - colWidths[0], yPos, paint)
            canvas.drawText(PersianFormatters.formatMoney(tx.amountToman).replace(" تومان", ""), pageInfo.pageWidth - 50f - colWidths[0] - colWidths[1], yPos, paint)
            
            val typeStr = if (tx.type == TransactionType.EXPENSE) "هزینه" else "درآمد"
            canvas.drawText(typeStr, pageInfo.pageWidth - 50f - colWidths[0] - colWidths[1] - colWidths[2], yPos, paint)
            
            yPos += 25f
        }

        document.finishPage(page)

        try {
            val cacheDir = File(context.cacheDir, "reports")
            cacheDir.mkdirs()
            val file = File(cacheDir, "smart wallet_report.pdf")
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()

            // Share Intent
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری گزارش PDF"))
            
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
        }
    }
}
