package com.example.core.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.core.utils.CurrencyFormatter
import com.example.core.utils.DateUtils
import com.example.domain.model.*
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object PdfExporter {

    fun generateFinancialReportPdf(
        context: Context,
        period: ReportPeriod,
        summary: FinancialSummary,
        accounts: List<Account>,
        transactions: List<Transaction>,
        orders: List<Order>,
        couriers: List<CourierSummary>,
        categoryExpenses: List<CategoryExpenseItem>
    ): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard width in points
        val pageHeight = 842 // A4 standard height in points

        // Paints
        val titlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(14, 116, 144) // Teal
            textSize = 12f
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 13f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 10f
            isAntiAlias = true
        }

        val boldBodyPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val greenPaint = Paint().apply {
            color = Color.rgb(5, 150, 105)
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val redPaint = Paint().apply {
            color = Color.rgb(220, 38, 38)
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
            isAntiAlias = true
        }

        val bgBoxPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // --- PAGE 1: Financial Overview & Account Summary ---
        val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1 = page1.canvas

        var y = 40f

        // Top Brand Header Bar
        canvas1.drawRect(RectF(30f, y, (pageWidth - 30).toFloat(), y + 60f), bgBoxPaint)
        canvas1.drawText("StyleSphere Management", 45f, y + 25f, titlePaint)
        canvas1.drawText("Business Financial & Operations Report", 45f, y + 45f, subtitlePaint)

        // Metadata right-aligned
        val metaDate = "Generated: ${DateUtils.formatDate(System.currentTimeMillis())}"
        val metaPeriod = "Period: ${period.displayName}"
        canvas1.drawText(metaPeriod, (pageWidth - 200).toFloat(), y + 25f, boldBodyPaint)
        canvas1.drawText(metaDate, (pageWidth - 200).toFloat(), y + 45f, bodyPaint)

        y += 85f

        // Section: Key Performance Metrics
        canvas1.drawText("FINANCIAL OVERVIEW", 30f, y, headerPaint)
        canvas1.drawLine(30f, y + 5f, (pageWidth - 30).toFloat(), y + 5f, linePaint)
        y += 20f

        // 3 Cards: Income, Expense, Net Profit
        val cardWidth = (pageWidth - 60 - 20) / 3f

        // Card 1: Income
        drawStatCard(canvas1, 30f, y, cardWidth, 60f, "Total Income", CurrencyFormatter.formatBDT(summary.totalIncome), Color.rgb(5, 150, 105))
        // Card 2: Expense
        drawStatCard(canvas1, 30f + cardWidth + 10f, y, cardWidth, 60f, "Total Expense", CurrencyFormatter.formatBDT(summary.totalExpense), Color.rgb(220, 38, 38))
        // Card 3: Net Profit
        drawStatCard(canvas1, 30f + (cardWidth + 10f) * 2, y, cardWidth, 60f, "Net Profit", CurrencyFormatter.formatBDT(summary.netProfit), if (summary.netProfit >= 0) Color.rgb(5, 150, 105) else Color.rgb(220, 38, 38))

        y += 75f

        // Secondary Metrics Row
        val smallCardWidth = (pageWidth - 60 - 30) / 4f
        drawStatCard(canvas1, 30f, y, smallCardWidth, 48f, "Profit Margin", "${String.format(Locale.US, "%.1f", summary.profitMarginPercent)}%", Color.rgb(14, 116, 144))
        drawStatCard(canvas1, 30f + smallCardWidth + 10f, y, smallCardWidth, 48f, "Total Orders", "${summary.totalOrders}", Color.rgb(30, 41, 59))
        drawStatCard(canvas1, 30f + (smallCardWidth + 10f) * 2, y, smallCardWidth, 48f, "Delivered Orders", "${summary.deliveredOrders}", Color.rgb(5, 150, 105))
        drawStatCard(canvas1, 30f + (smallCardWidth + 10f) * 3, y, smallCardWidth, 48f, "Avg Order Value", CurrencyFormatter.formatBDT(summary.averageOrderValue), Color.rgb(30, 41, 59))

        y += 68f

        // Section: Account & Wallet Balances
        canvas1.drawText("ACCOUNT & WALLET BALANCES", 30f, y, headerPaint)
        canvas1.drawLine(30f, y + 5f, (pageWidth - 30).toFloat(), y + 5f, linePaint)
        y += 20f

        // Table Header
        canvas1.drawText("Account Name", 35f, y, boldBodyPaint)
        canvas1.drawText("Type", 200f, y, boldBodyPaint)
        canvas1.drawText("Money In", 300f, y, boldBodyPaint)
        canvas1.drawText("Money Out", 400f, y, boldBodyPaint)
        canvas1.drawText("Current Balance", 480f, y, boldBodyPaint)
        canvas1.drawLine(30f, y + 6f, (pageWidth - 30).toFloat(), y + 6f, linePaint)
        y += 18f

        accounts.forEach { acc ->
            canvas1.drawText(acc.name, 35f, y, bodyPaint)
            canvas1.drawText(acc.type.name, 200f, y, bodyPaint)
            canvas1.drawText(CurrencyFormatter.formatBDT(acc.totalMoneyIn), 300f, y, greenPaint)
            canvas1.drawText(CurrencyFormatter.formatBDT(acc.totalMoneyOut), 400f, y, redPaint)
            canvas1.drawText(CurrencyFormatter.formatBDT(acc.currentBalance), 480f, y, boldBodyPaint)
            y += 16f
        }

        y += 15f

        // Section: Category Expense Breakdown
        canvas1.drawText("EXPENSE BREAKDOWN BY CATEGORY", 30f, y, headerPaint)
        canvas1.drawLine(30f, y + 5f, (pageWidth - 30).toFloat(), y + 5f, linePaint)
        y += 20f

        categoryExpenses.take(8).forEach { cat ->
            canvas1.drawText(cat.categoryName, 35f, y, bodyPaint)
            canvas1.drawText("${cat.transactionCount} transactions", 240f, y, bodyPaint)
            canvas1.drawText(CurrencyFormatter.formatBDT(cat.amount), 380f, y, redPaint)
            canvas1.drawText("${String.format(Locale.US, "%.1f", cat.percentage)}%", 490f, y, boldBodyPaint)
            y += 15f
        }

        // Footer Page 1
        canvas1.drawText("StyleSphere Management System • Confidential • Page 1 of 2", 30f, (pageHeight - 30).toFloat(), bodyPaint)
        pdfDocument.finishPage(page1)

        // --- PAGE 2: Orders, Couriers & Recent Transactions ---
        val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        val canvas2 = page2.canvas

        y = 40f
        canvas2.drawText("COURIER PERFORMANCE & COD SUMMARY", 30f, y, headerPaint)
        canvas2.drawLine(30f, y + 5f, (pageWidth - 30).toFloat(), y + 5f, linePaint)
        y += 20f

        // Courier Table
        canvas2.drawText("Courier", 35f, y, boldBodyPaint)
        canvas2.drawText("Parcels", 130f, y, boldBodyPaint)
        canvas2.drawText("Delivered", 190f, y, boldBodyPaint)
        canvas2.drawText("Cancelled/Ret", 260f, y, boldBodyPaint)
        canvas2.drawText("Total COD", 360f, y, boldBodyPaint)
        canvas2.drawText("Pending Coll.", 470f, y, boldBodyPaint)
        canvas2.drawLine(30f, y + 6f, (pageWidth - 30).toFloat(), y + 6f, linePaint)
        y += 18f

        couriers.forEach { cour ->
            canvas2.drawText(cour.courierOption.displayName, 35f, y, bodyPaint)
            canvas2.drawText("${cour.totalParcels}", 130f, y, bodyPaint)
            canvas2.drawText("${cour.delivered}", 190f, y, greenPaint)
            canvas2.drawText("${cour.cancelled + cour.returned}", 260f, y, redPaint)
            canvas2.drawText(CurrencyFormatter.formatBDT(cour.totalCodAmount), 360f, y, boldBodyPaint)
            canvas2.drawText(CurrencyFormatter.formatBDT(cour.pendingCollection), 470f, y, bodyPaint)
            y += 16f
        }

        y += 25f

        // Section: Transaction Audit Trail
        canvas2.drawText("RECENT AUDITED TRANSACTIONS", 30f, y, headerPaint)
        canvas2.drawLine(30f, y + 5f, (pageWidth - 30).toFloat(), y + 5f, linePaint)
        y += 20f

        canvas2.drawText("ID / Date", 35f, y, boldBodyPaint)
        canvas2.drawText("Type", 140f, y, boldBodyPaint)
        canvas2.drawText("Category / Description", 210f, y, boldBodyPaint)
        canvas2.drawText("Account", 380f, y, boldBodyPaint)
        canvas2.drawText("Amount", 480f, y, boldBodyPaint)
        canvas2.drawLine(30f, y + 6f, (pageWidth - 30).toFloat(), y + 6f, linePaint)
        y += 18f

        transactions.take(16).forEach { tx ->
            val dateStr = DateUtils.formatShortDate(tx.dateMillis)
            canvas2.drawText("${tx.id} ($dateStr)", 35f, y, bodyPaint)
            val typePaint = when (tx.type) {
                TransactionType.INCOME -> greenPaint
                TransactionType.EXPENSE -> redPaint
                TransactionType.TRANSFER -> boldBodyPaint
            }
            canvas2.drawText(tx.type.name, 140f, y, typePaint)
            val desc = if (tx.description.isNotBlank()) tx.description else tx.categoryName
            val shortDesc = if (desc.length > 28) desc.take(25) + "..." else desc
            canvas2.drawText(shortDesc, 210f, y, bodyPaint)
            canvas2.drawText(tx.accountId.removePrefix("acc_").uppercase(), 380f, y, bodyPaint)
            val amtPrefix = when (tx.type) {
                TransactionType.INCOME -> "+"
                TransactionType.EXPENSE -> "-"
                TransactionType.TRANSFER -> ""
            }
            canvas2.drawText("$amtPrefix${CurrencyFormatter.formatBDT(tx.amount)}", 480f, y, typePaint)
            y += 16f
        }

        // Footer Page 2
        canvas2.drawText("StyleSphere Management System • Confidential • Page 2 of 2", 30f, (pageHeight - 30).toFloat(), bodyPaint)
        pdfDocument.finishPage(page2)

        // Write to cache file
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(reportsDir, "StyleSphere_Report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    private fun drawStatCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        value: String,
        valueColor: Int
    ) {
        val rect = RectF(x, y, x + width, y + height)
        val bgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val titlePaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = valueColor
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        canvas.drawRoundRect(rect, 6f, 6f, bgPaint)
        canvas.drawRoundRect(rect, 6f, 6f, borderPaint)
        canvas.drawText(title, x + 8f, y + 16f, titlePaint)
        canvas.drawText(value, x + 8f, y + 36f, valuePaint)
    }
}
