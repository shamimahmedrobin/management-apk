package com.example.core.export

import android.content.Context
import com.example.core.utils.CurrencyFormatter
import com.example.core.utils.DateUtils
import com.example.domain.model.*
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExcelExporter {

    fun generateExcelReport(
        context: Context,
        summary: FinancialSummary,
        accounts: List<Account>,
        transactions: List<Transaction>,
        orders: List<Order>,
        couriers: List<CourierSummary>,
        products: List<Product>,
        customers: List<Customer>
    ): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(reportsDir, "StyleSphere_Report_${System.currentTimeMillis()}.xlsx")

        val sheetNames = listOf(
            "Summary",
            "Income",
            "Expenses",
            "Transfers",
            "Orders",
            "Courier",
            "Products",
            "Customers"
        )

        val incomeList = transactions.filter { it.type == TransactionType.INCOME }
        val expenseList = transactions.filter { it.type == TransactionType.EXPENSE }
        val transferList = transactions.filter { it.type == TransactionType.TRANSFER }

        val sheetDataList = listOf(
            buildSummarySheet(summary, accounts),
            buildIncomeSheet(incomeList),
            buildExpensesSheet(expenseList),
            buildTransfersSheet(transferList),
            buildOrdersSheet(orders),
            buildCourierSheet(couriers),
            buildProductsSheet(products),
            buildCustomersSheet(customers)
        )

        ZipOutputStream(FileOutputStream(file)).use { zip ->
            // 1. [Content_Types].xml
            zip.putNextEntry(ZipEntry("[Content_Types].xml"))
            zip.write(buildContentTypesXml(sheetNames.size).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 2. _rels/.rels
            zip.putNextEntry(ZipEntry("_rels/.rels"))
            zip.write(buildRootRelsXml().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 3. xl/workbook.xml
            zip.putNextEntry(ZipEntry("xl/workbook.xml"))
            zip.write(buildWorkbookXml(sheetNames).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 4. xl/_rels/workbook.xml.rels
            zip.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
            zip.write(buildWorkbookRelsXml(sheetNames.size).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 5. Each sheet
            for (i in sheetNames.indices) {
                zip.putNextEntry(ZipEntry("xl/worksheets/sheet${i + 1}.xml"))
                zip.write(sheetDataList[i].toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }

        return file
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun buildRow(rowNum: Int, cells: List<String>): String {
        val sb = StringBuilder()
        sb.append("<row r=\"").append(rowNum).append("\">")
        for (i in cells.indices) {
            val colLetter = getColumnLetter(i)
            val cellRef = "$colLetter$rowNum"
            val value = escapeXml(cells[i])
            sb.append("<c r=\"").append(cellRef).append("\" t=\"inlineStr\"><is><t>").append(value).append("</t></is></c>")
        }
        sb.append("</row>")
        return sb.toString()
    }

    private fun getColumnLetter(colIndex: Int): String {
        return if (colIndex < 26) {
            ('A'.code + colIndex).toChar().toString()
        } else {
            val first = ('A'.code + (colIndex / 26) - 1).toChar()
            val second = ('A'.code + (colIndex % 26)).toChar()
            "$first$second"
        }
    }

    private fun wrapWorksheet(rowsXml: String): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheetData>
$rowsXml
</sheetData>
</worksheet>"""
    }

    private fun buildSummarySheet(summary: FinancialSummary, accounts: List<Account>): String {
        val rows = mutableListOf<String>()
        var r = 1
        rows.add(buildRow(r++, listOf("StyleSphere Management - Executive Financial Summary")))
        rows.add(buildRow(r++, listOf("Generated", DateUtils.formatDate(System.currentTimeMillis()))))
        rows.add(buildRow(r++, listOf("")))

        rows.add(buildRow(r++, listOf("Metric", "Value (BDT / Unit)")))
        rows.add(buildRow(r++, listOf("Total Income", CurrencyFormatter.formatAmountOnly(summary.totalIncome))))
        rows.add(buildRow(r++, listOf("Total Expense", CurrencyFormatter.formatAmountOnly(summary.totalExpense))))
        rows.add(buildRow(r++, listOf("Net Profit", CurrencyFormatter.formatAmountOnly(summary.netProfit))))
        rows.add(buildRow(r++, listOf("Profit Margin (%)", String.format(Locale.US, "%.2f", summary.profitMarginPercent))))
        rows.add(buildRow(r++, listOf("Gross Sales", CurrencyFormatter.formatAmountOnly(summary.grossSales))))
        rows.add(buildRow(r++, listOf("Product Costs", CurrencyFormatter.formatAmountOnly(summary.productCost))))
        rows.add(buildRow(r++, listOf("Delivery Costs", CurrencyFormatter.formatAmountOnly(summary.deliveryCost))))
        rows.add(buildRow(r++, listOf("Advertising Cost", CurrencyFormatter.formatAmountOnly(summary.advertisingCost))))
        rows.add(buildRow(r++, listOf("Other Expenses", CurrencyFormatter.formatAmountOnly(summary.otherExpenses))))
        rows.add(buildRow(r++, listOf("Total Orders", summary.totalOrders.toString())))
        rows.add(buildRow(r++, listOf("Delivered Orders", summary.deliveredOrders.toString())))
        rows.add(buildRow(r++, listOf("Cancelled Orders", summary.cancelledOrders.toString())))
        rows.add(buildRow(r++, listOf("Returned Orders", summary.returnedOrders.toString())))
        rows.add(buildRow(r++, listOf("Average Order Value", CurrencyFormatter.formatAmountOnly(summary.averageOrderValue))))
        rows.add(buildRow(r++, listOf("")))

        rows.add(buildRow(r++, listOf("Account Balances Summary")))
        rows.add(buildRow(r++, listOf("Account", "Type", "Money In (৳)", "Money Out (৳)", "Current Balance (৳)")))
        accounts.forEach { acc ->
            rows.add(buildRow(r++, listOf(
                acc.name,
                acc.type.name,
                CurrencyFormatter.formatAmountOnly(acc.totalMoneyIn),
                CurrencyFormatter.formatAmountOnly(acc.totalMoneyOut),
                CurrencyFormatter.formatAmountOnly(acc.currentBalance)
            )))
        }

        return wrapWorksheet(rows.joinToString("\n"))
    }

    private fun buildIncomeSheet(incomeList: List<Transaction>): String {
        val rows = mutableListOf<String>()
        var r = 1
        rows.add(buildRow(r++, listOf("Transaction ID", "Date", "Time", "Category", "Amount (৳)", "Account", "Reference", "Order ID", "Customer", "Notes")))
        incomeList.forEach { tx ->
            rows.add(buildRow(r++, listOf(
                tx.id,
                DateUtils.formatDate(tx.dateMillis),
                tx.timeString,
                tx.categoryName,
                CurrencyFormatter.formatAmountOnly(tx.amount),
                tx.accountId,
                tx.reference,
                tx.orderId ?: "",
                tx.customerName ?: "",
                tx.notes
            )))
        }
        return wrapWorksheet(rows.joinToString("\n"))
    }

    private fun buildExpensesSheet(expenseList: List<Transaction>): String {
        val rows = mutableListOf<String>()
        var r = 1
        rows.add(buildRow(r++, listOf("Transaction ID", "Date", "Time", "Category", "Amount (৳)", "Payment Account", "Description", "Reference", "Notes")))
        expenseList.forEach { tx ->
            rows.add(buildRow(r++, listOf(
                tx.id,
                DateUtils.formatDate(tx.dateMillis),
                tx.timeString,
                tx.categoryName,
                CurrencyFormatter.formatAmountOnly(tx.amount),
                tx.accountId,
                tx.description,
                tx.reference,
                tx.notes
            )))
        }
        return wrapWorksheet(rows.joinToString("\n"))
    }

    private fun buildTransfersSheet(transferList: List<Transaction>): String {
        val rows = mutableListOf<String>()
        var r = 1
        rows.add(buildRow(r++, listOf("Transfer ID", "Date", "Time", "From Account", "To Account", "Amount (৳)", "Reference", "Description", "Notes")))
        transferList.forEach { tx ->
            rows.add(buildRow(r++, listOf(
                tx.id,
                DateUtils.formatDate(tx.dateMillis),
                tx.timeString,
                tx.accountId,
                tx.destinationAccountId ?: "",
                CurrencyFormatter.formatAmountOnly(tx.amount),
                tx.reference,
                tx.description,
                tx.notes
            )))
        }
        return wrapWorksheet(rows.joinToString("\n"))
    }

    private fun buildOrdersSheet(orders: List<Order>): String {
        val rows = mutableListOf<String>()
        var r = 1
        rows.add(buildRow(r++, listOf("Order ID", "Date", "Customer Name", "Phone", "Address", "Product", "Qty", "Cost (৳)", "Selling Price (৳)", "Delivery Charge (৳)", "Courier", "Payment Method", "Status", "Profit (৳)", "Notes")))
        orders.forEach { o ->
            rows.add(buildRow(r++, listOf(
                o.id,
                DateUtils.formatDate(o.orderDateMillis),
                o.customerName,
                o.phoneNumber,
                o.deliveryAddress,
                o.productName,
                o.quantity.toString(),
                CurrencyFormatter.formatAmountOnly(o.productCost),
                CurrencyFormatter.formatAmountOnly(o.sellingPrice),
                CurrencyFormatter.formatAmountOnly(o.deliveryCharge),
                o.courier.displayName,
                o.paymentMethod.displayName,
                o.status.displayName,
                CurrencyFormatter.formatAmountOnly(o.estimatedProfit),
                o.notes
            )))
        }
        return wrapWorksheet(rows.joinToString("\n"))
    }

    private fun buildCourierSheet(couriers: List<CourierSummary>): String {
        val rows = mutableListOf<String>()
        var r = 1
        rows.add(buildRow(r++, listOf("Courier Name", "Total Parcels", "Delivered", "Cancelled", "Returned", "Pending", "Total COD (৳)", "Received Amount (৳)", "Pending Collection (৳)", "Courier Charges (৳)")))
        couriers.forEach { c ->
            rows.add(buildRow(r++, listOf(
                c.courierOption.displayName,
                c.totalParcels.toString(),
                c.delivered.toString(),
                c.cancelled.toString(),
                c.returned.toString(),
                c.pending.toString(),
                CurrencyFormatter.formatAmountOnly(c.totalCodAmount),
                CurrencyFormatter.formatAmountOnly(c.receivedAmount),
                CurrencyFormatter.formatAmountOnly(c.pendingCollection),
                CurrencyFormatter.formatAmountOnly(c.courierCharges)
            )))
        }
        return wrapWorksheet(rows.joinToString("\n"))
    }

    private fun buildProductsSheet(products: List<Product>): String {
        val rows = mutableListOf<String>()
        var r = 1
        rows.add(buildRow(r++, listOf("Product ID", "Name", "SKU", "Category", "Purchase Cost (৳)", "Selling Price (৳)", "Current Stock", "Min Stock", "Total Sold", "Status", "Profit Margin (%)")))
        products.forEach { p ->
            rows.add(buildRow(r++, listOf(
                p.id,
                p.name,
                p.sku,
                p.category,
                CurrencyFormatter.formatAmountOnly(p.purchaseCost),
                CurrencyFormatter.formatAmountOnly(p.sellingPrice),
                p.currentStock.toString(),
                p.minimumStock.toString(),
                p.totalSold.toString(),
                p.status.displayName,
                String.format(Locale.US, "%.1f", p.profitMarginPercent)
            )))
        }
        return wrapWorksheet(rows.joinToString("\n"))
    }

    private fun buildCustomersSheet(customers: List<Customer>): String {
        val rows = mutableListOf<String>()
        var r = 1
        rows.add(buildRow(r++, listOf("Customer ID", "Name", "Phone", "Address", "Total Orders", "Total Purchase (৳)", "Due Amount (৳)", "Notes")))
        customers.forEach { c ->
            rows.add(buildRow(r++, listOf(
                c.id,
                c.name,
                c.phone,
                c.address,
                c.totalOrders.toString(),
                CurrencyFormatter.formatAmountOnly(c.totalPurchase),
                CurrencyFormatter.formatAmountOnly(c.dueAmount),
                c.notes
            )))
        }
        return wrapWorksheet(rows.joinToString("\n"))
    }

    private fun buildContentTypesXml(sheetCount: Int): String {
        val sb = StringBuilder("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
""")
        for (i in 1..sheetCount) {
            sb.append("<Override PartName=\"/xl/worksheets/sheet$i.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>\n")
        }
        sb.append("</Types>")
        return sb.toString()
    }

    private fun buildRootRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
    }

    private fun buildWorkbookXml(sheetNames: List<String>): String {
        val sb = StringBuilder("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets>
""")
        sheetNames.forEachIndexed { index, name ->
            val sheetId = index + 1
            sb.append("<sheet name=\"").append(escapeXml(name)).append("\" sheetId=\"").append(sheetId).append("\" r:id=\"rId").append(sheetId).append("\"/>\n")
        }
        sb.append("""</sheets>
</workbook>""")
        return sb.toString()
    }

    private fun buildWorkbookRelsXml(sheetCount: Int): String {
        val sb = StringBuilder("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
""")
        for (i in 1..sheetCount) {
            sb.append("<Relationship Id=\"rId$i\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet$i.xml\"/>\n")
        }
        sb.append("</Relationships>")
        return sb.toString()
    }
}
