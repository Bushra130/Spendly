package com.example.spendly

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            processImageForOCR(bitmap)
        } else {
            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCameraForScan()
        } else {
            Toast.makeText(this, "Camera permission is required to scan bills", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val etDate = findViewById<EditText>(R.id.etDate)
        val btnSave = findViewById<Button>(R.id.btnSaveExpense)
        val btnScanBill = findViewById<Button>(R.id.btnScanBill)

        // Setup category dropdown
        val categories = arrayOf("Food", "Transport", "Bills", "Shopping", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = adapter

        // Set current date by default
        val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        etDate.setText(currentDate)

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
                etDate.setText(formattedDate)
            }, year, month, day).show()
        }

        // Handle Scan Bill Click
        btnScanBill.setOnClickListener {
            checkAndOpenCameraController()
        }

        // Handle save click
        btnSave.setOnClickListener {
            val amountStr = etAmount.text.toString()
            val titleStr = etTitle.text.toString()
            val categoryStr = spinnerCategory.selectedItem.toString()
            val dateStr = etDate.text.toString()

            if (amountStr.isEmpty() || titleStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = Expense(
                title = titleStr,
                amount = amountStr.toDouble(),
                category = categoryStr,
                date = dateStr,
                note = ""
            )

            viewModel.insert(expense)
            Toast.makeText(this, "Expense Saved!", Toast.LENGTH_SHORT).show()
            finish() // Return to Home screen
        }

        // Check if launched directly from Home screen's "Scan receipt" button
        if (intent.getBooleanExtra("AUTO_SCAN", false)) {
            btnScanBill.postDelayed({
                checkAndOpenCameraController()
            }, 300)
        }
    }

    private fun checkAndOpenCameraController() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCameraForScan()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCameraForScan() {
        takePictureLauncher.launch(null)
    }

    private fun processImageForOCR(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                var extractedTitle = ""
                var extractedAmount = ""
                var extractedDate = ""

                val allLines = mutableListOf<String>()
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        val text = line.text.trim()
                        if (text.isNotEmpty()) {
                            allLines.add(text)
                        }
                    }
                }

                // 1. Grab Title (first meaningful line)
                for (line in allLines) {
                    val cleanLine = line.replace(Regex("[^a-zA-Z ]"), "").trim()
                    if (cleanLine.length > 2 && !cleanLine.lowercase().contains("total") && !cleanLine.lowercase().contains("date")) {
                        extractedTitle = cleanLine
                        break
                    }
                }
                if (extractedTitle.isEmpty() && allLines.isNotEmpty()) {
                    extractedTitle = allLines[0]
                }

                // 2. Flexible Scan for Date formats (handles dates even if followed by time)
                val dateRegex = Regex("\\b(\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4})\\b")
                for (line in allLines) {
                    val match = dateRegex.find(line)
                    if (match != null) {
                        val rawDate = match.groupValues[1] // Extracts just the date part, ignoring trailing time
                        val possibleFormats = listOf(
                            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                            SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()),
                            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        )

                        for (format in possibleFormats) {
                            try {
                                format.isLenient = false
                                val parsedDate = format.parse(rawDate)
                                if (parsedDate != null) {
                                    extractedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(parsedDate)
                                    break
                                }
                            } catch (_: Exception) {
                            }
                        }
                        if (extractedDate.isNotEmpty()) break
                    }
                }

// Populate Date if found
                if (extractedDate.isNotEmpty()) {
                    findViewById<EditText>(R.id.etDate).setText(extractedDate)
                }
                // 3. Scan for Amount (bottom-up search for totals)
                for (line in allLines.reversed()) {
                    val numbers = Regex("\\d+\\.\\d{2}").findAll(line).map { it.value }.toList()
                    if (numbers.isNotEmpty()) {
                        extractedAmount = numbers.last()
                        break
                    }
                }
                if (extractedAmount.isEmpty()) {
                    for (line in allLines.reversed()) {
                        val numbers = Regex("\\d+").findAll(line).map { it.value }.toList()
                        if (numbers.isNotEmpty()) {
                            extractedAmount = numbers.last()
                            break
                        }
                    }
                }

                // Populate Title and Amount
                if (extractedTitle.isNotEmpty()) {
                    findViewById<EditText>(R.id.etTitle).setText(extractedTitle)
                }
                if (extractedAmount.isNotEmpty()) {
                    findViewById<EditText>(R.id.etAmount).setText(extractedAmount)
                }

                // 4. Smart Category Selection based on Title/Merchant keywords
                val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
                val titleLower = extractedTitle.lowercase()
                val categoryAdapter = spinnerCategory.adapter as ArrayAdapter<String>

                val matchedCategory = when {
                    titleLower.contains("store") || titleLower.contains("market") || titleLower.contains("shop") || titleLower.contains("retail") -> "Shopping"
                    titleLower.contains("cafe") || titleLower.contains("restaurant") || titleLower.contains("food") || titleLower.contains("coffee") -> "Food"
                    titleLower.contains("uber") || titleLower.contains("auto") || titleLower.contains("fuel") || titleLower.contains("petrol") -> "Transport"
                    titleLower.contains("bill") || titleLower.contains("electricity") || titleLower.contains("water") || titleLower.contains("wifi") -> "Bills"
                    else -> "Other"
                }

                val spinnerPosition = categoryAdapter.getPosition(matchedCategory)
                if (spinnerPosition >= 0) {
                    spinnerCategory.setSelection(spinnerPosition)
                }

                Toast.makeText(this, "Receipt scanned & parsed successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "OCR failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}