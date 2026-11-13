package com.example.spendly

import android.content.ContentValues
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.io.File
import java.io.FileOutputStream

class ReportsActivity : AppCompatActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val btnExport = findViewById<Button>(R.id.btnExport)

        // Update button text to reflect PDF export
        btnExport.text = "Export to PDF"

        // Observe expenses and group by category for the chart
        viewModel.allExpenses.observe(this) { expenses ->
            val categoryMap = mutableMapOf<String, Float>()

            for (expense in expenses) {
                val currentTotal = categoryMap[expense.category] ?: 0f
                categoryMap[expense.category] = currentTotal + expense.amount.toFloat()
            }

            val entries = ArrayList<PieEntry>()
            for ((category, total) in categoryMap) {
                entries.add(PieEntry(total, category))
            }

            val dataSet = PieDataSet(entries, "Categories")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
            dataSet.valueTextSize = 14f

            val pieData = PieData(dataSet)
            pieChart.data = pieData
            pieChart.description.isEnabled = false
            pieChart.centerText = "Expenses"
            pieChart.setCenterTextSize(16f)
            pieChart.animateY(1000)
            pieChart.invalidate() // Refresh chart
        }

        // Handle PDF Export Click
        btnExport.setOnClickListener {
            val expenses = viewModel.allExpenses.value
            if (expenses.isNullOrEmpty()) {
                Toast.makeText(this, "No expenses to export!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas: Canvas = page.canvas
                val paint = Paint()

                // Draw Title
                paint.textSize = 20f
                paint.isFakeBoldText = true
                paint.color = Color.rgb(15, 81, 50)
                canvas.drawText("Spendly - Expense Report", 40f, 50f, paint)

                // Draw Table Headers
                paint.textSize = 12f
                paint.isFakeBoldText = true
                paint.color = Color.BLACK
                var y = 100f
                canvas.drawText("Date", 40f, y, paint)
                canvas.drawText("Title", 140f, y, paint)
                canvas.drawText("Category", 380f, y, paint)
                canvas.drawText("Amount", 500f, y, paint)

                // Draw Header Divider Line
                paint.strokeWidth = 1.5f
                canvas.drawLine(40f, 110f, 555f, 110f, paint)

                // Draw Expense Items
                paint.isFakeBoldText = false
                y = 140f
                var totalSum = 0.0

                for (e in expenses) {
                    if (y > 780f) break
                    canvas.drawText(e.date, 40f, y, paint)
                    canvas.drawText(e.title, 140f, y, paint)
                    canvas.drawText(e.category, 380f, y, paint)
                    canvas.drawText("₹${e.amount}", 500f, y, paint)
                    totalSum += e.amount
                    y += 28f
                }

                // Draw Footer Total Line & Sum
                y += 10f
                paint.strokeWidth = 1f
                canvas.drawLine(40f, y, 555f, y, paint)
                y += 30f
                paint.textSize = 14f
                paint.isFakeBoldText = true
                paint.color = Color.rgb(15, 81, 50)
                canvas.drawText("Total Expenses: ₹$totalSum", 40f, y, paint)

                pdfDocument.finishPage(page)

                val fileName = "Spendly_Report_${System.currentTimeMillis()}.pdf"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                    }

                    val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            pdfDocument.writeTo(outputStream)
                        }
                        pdfDocument.close()
                        Toast.makeText(this, "PDF saved to Documents folder!", Toast.LENGTH_LONG).show()
                    } else {
                        pdfDocument.close()
                        Toast.makeText(this, "Failed to create PDF file entry", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    pdfDocument.close()
                    Toast.makeText(this, "Export requires Android 10+", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this, "PDF Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}