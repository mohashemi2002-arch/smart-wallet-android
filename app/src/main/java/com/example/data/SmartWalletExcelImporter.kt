package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class SmartWalletExcelRow(
    val dateJalali: String,
    val type: String,
    val amountRial: String,
    val source: String,
    val category: String,
    val description: String
)

object SmartWalletExcelImporter {
    fun parse(context: Context, uri: Uri): List<SmartWalletExcelRow> {
        val sharedStrings = mutableListOf<String>()
        val rows = mutableListOf<SmartWalletExcelRow>()

        // 1. Read shared strings
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val zip = ZipInputStream(inputStream)
            var entry: ZipEntry? = zip.nextEntry
            while (entry != null) {
                if (entry.name == "xl/sharedStrings.xml") {
                    parseSharedStrings(zip, sharedStrings)
                    break
                }
                entry = zip.nextEntry
            }
        }

        // 2. Read sheet1
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val zip = ZipInputStream(inputStream)
            var entry: ZipEntry? = zip.nextEntry
            while (entry != null) {
                if (entry.name.startsWith("xl/worksheets/sheet") && entry.name.endsWith(".xml")) {
                    parseSheet(zip, sharedStrings, rows)
                    break
                }
                entry = zip.nextEntry
            }
        }

        return rows
    }

    private fun parseSharedStrings(inputStream: InputStream, sharedStrings: MutableList<String>) {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        var currentText = StringBuilder()
        var inT = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "t") {
                        inT = true
                        currentText = java.lang.StringBuilder()
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inT) {
                        currentText.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "t") {
                        inT = false
                        sharedStrings.add(currentText.toString())
                    }
                }
            }
            eventType = parser.next()
        }
    }

    private fun parseSheet(inputStream: InputStream, sharedStrings: List<String>, rows: MutableList<SmartWalletExcelRow>) {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        var inRow = false
        var inC = false
        var inV = false
        var currentCellRef = ""
        var currentCellType = ""
        var currentValue = ""

        var dateJalali = ""
        var type = ""
        var amountRial = ""
        var source = ""
        var category = ""
        var description = ""

        var isHeaderRow = true

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "row") {
                        inRow = true
                        dateJalali = ""
                        type = ""
                        amountRial = ""
                        source = ""
                        category = ""
                        description = ""
                    } else if (inRow && parser.name == "c") {
                        inC = true
                        currentCellRef = parser.getAttributeValue(null, "r") ?: ""
                        currentCellType = parser.getAttributeValue(null, "t") ?: ""
                    } else if (inC && parser.name == "v") {
                        inV = true
                        currentValue = ""
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inV) {
                        currentValue += parser.text
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "v") {
                        inV = false
                        var finalValue = currentValue
                        if (currentCellType == "s") {
                            val index = currentValue.toIntOrNull()
                            if (index != null && index >= 0 && index < sharedStrings.size) {
                                finalValue = sharedStrings[index]
                            }
                        }

                        val colMatch = Regex("[a-zA-Z]+").find(currentCellRef)
                        val col = colMatch?.value ?: ""
                        
                        when (col) {
                            "A" -> dateJalali = finalValue
                            "B" -> type = finalValue
                            "C" -> amountRial = finalValue
                            "D" -> source = finalValue
                            "E" -> category = finalValue
                            "F" -> description = finalValue
                        }
                    } else if (parser.name == "c") {
                        inC = false
                    } else if (parser.name == "row") {
                        inRow = false
                        if (isHeaderRow) {
                            isHeaderRow = false
                        } else {
                            if (dateJalali.isNotBlank() || amountRial.isNotBlank()) {
                                rows.add(SmartWalletExcelRow(dateJalali, type, amountRial, source, category, description))
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }
    }
}
