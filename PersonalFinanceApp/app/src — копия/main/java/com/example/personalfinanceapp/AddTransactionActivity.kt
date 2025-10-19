package com.example.personalfinanceapp

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.personalfinanceapp.data.AppDatabase
import com.example.personalfinanceapp.data.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences
    private lateinit var rootLayout: LinearLayout

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

        val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
        val amountEdit = findViewById<EditText>(R.id.amountEdit)
        val categoryEdit = findViewById<EditText>(R.id.categoryEdit)
        val saveBtn = findViewById<Button>(R.id.saveBtn)

        val types = arrayOf("Доход", "Расход")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                (view as? android.widget.TextView)?.setTextColor(resources.getColor(R.color.black))
                return view
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as? android.widget.TextView)?.apply {
                    setTextColor(resources.getColor(R.color.black))
                    setBackgroundColor(resources.getColor(R.color.light_gray))
                }
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = adapter

        typeSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedType = types[position]
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
            }
        }

        val transactionType = intent.getStringExtra("TYPE")
        typeSpinner.setSelection(if (transactionType == "expense") 1 else 0)

        saveBtn.setOnClickListener {
            val amountText = amountEdit.text.toString()
            val category = categoryEdit.text.toString()
            val type = if (typeSpinner.selectedItem.toString() == "Доход") "income" else "expense"

            if (amountText.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
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
                        date = Date()
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
}