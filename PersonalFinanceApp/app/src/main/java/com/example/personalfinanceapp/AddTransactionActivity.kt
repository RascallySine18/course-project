package com.example.personalfinanceapp

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.personalfinanceapp.data.AppDatabase
import com.example.personalfinanceapp.data.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences
    private lateinit var rootLayout: LinearLayout
    private lateinit var dateTextView: TextView
    private var selectedDate: Date = Date() // Переменная для хранения выбранной даты

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        rootLayout = findViewById(R.id.rootLayout)
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        db = AppDatabase.getDatabase(this)

        applyBackground()

        val email = intent.getStringExtra("USER_EMAIL") ?: run {
            Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val type = intent.getStringExtra("TYPE") ?: "expense"

        dateTextView = findViewById(R.id.dateTextView)
        val amountEdit = findViewById<EditText>(R.id.amountEdit)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val saveBtn = findViewById<Button>(R.id.saveBtn)

        // Установка начальной даты из интента или текущей даты
        val initialDateMillis = intent.getLongExtra("DATE", System.currentTimeMillis())
        selectedDate = Date(initialDateMillis)
        updateDateTextView(selectedDate)

        // Открытие DatePicker для выбора даты
        dateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val calendarNew = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                selectedDate = calendarNew.time
                updateDateTextView(selectedDate)
            }, year, month, day).show()
        }

        val categories = resources.getStringArray(R.array.categories)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        saveBtn.setOnClickListener {
            val amountText = amountEdit.text.toString()
            val category = categorySpinner.selectedItem.toString()

            if (amountText.isEmpty()) {
                Toast.makeText(this, "Заполните сумму", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Введите корректную сумму", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                db.transactionDao().insertTransaction(
                    Transaction(
                        userEmail = email,
                        type = type,
                        amount = amount,
                        category = category,
                        date = selectedDate // Используем обновлённую выбранную дату
                    )
                )
                runOnUiThread {
                    Toast.makeText(this@AddTransactionActivity, "Транзакция добавлена", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    private fun applyBackground() {
        val path = prefs.getString("background_path", null)
        if (path != null) {
            loadAndSetBackground(rootLayout, path)
        }
    }

    private fun loadAndSetBackground(layout: LinearLayout, path: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(path)
                layout.background = BitmapDrawable(resources, bitmap)
            } else {
                layout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                prefs.edit().remove("background_path").apply()
            }
        } catch (e: Exception) {
            layout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            prefs.edit().remove("background_path").apply()
        }
    }

    private fun updateDateTextView(date: Date) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        dateTextView.text = "Дата: ${dateFormat.format(date)}"
    }
}