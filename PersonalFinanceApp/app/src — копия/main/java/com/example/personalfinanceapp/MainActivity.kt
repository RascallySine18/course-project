package com.example.personalfinanceapp

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.personalfinanceapp.data.AppDatabase
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences
    private lateinit var balanceText: TextView
    private lateinit var rootLayout: LinearLayout
    private val addTransactionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            updateBalance()
        }
    }
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
            bitmap?.let { bmp ->
                val file = File(filesDir, "background_image.jpg")
                FileOutputStream(file).use { outputStream ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                }
                prefs.edit().putString("background_path", file.absolutePath).apply()
                loadAndSetBackground(rootLayout, file.absolutePath)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rootLayout = findViewById(R.id.rootLayout)
        db = AppDatabase.getDatabase(this)
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

        val email = prefs.getString("current_user", null) ?: run {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        balanceText = findViewById(R.id.balanceText)
        val addIncomeBtn = findViewById<Button>(R.id.addIncomeBtn)
        val addExpenseBtn = findViewById<Button>(R.id.addExpenseBtn)
        val viewTransactionsBtn = findViewById<Button>(R.id.viewTransactionsBtn)
        val changeBackgroundBtn = findViewById<Button>(R.id.changeBackgroundBtn)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        applyBackground()

        updateBalance()

        addIncomeBtn.setOnClickListener { addTransaction("income") }
        addExpenseBtn.setOnClickListener { addTransaction("expense") }
        viewTransactionsBtn.setOnClickListener { viewTransactions(email) }
        changeBackgroundBtn.setOnClickListener {
            getContent.launch("image/*")
        }
        logoutBtn.setOnClickListener {
            prefs.edit().remove("current_user").apply()
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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

    private fun addTransaction(type: String) {
        val intent = Intent(this, AddTransactionActivity::class.java).apply {
            putExtra("USER_EMAIL", prefs.getString("current_user", ""))
            putExtra("TYPE", type)
        }
        addTransactionResult.launch(intent)
    }

    private fun viewTransactions(email: String) {
        val intent = Intent(this, ViewTransactionsActivity::class.java).apply {
            putExtra("USER_EMAIL", email)
        }
        startActivity(intent)
    }

    private fun updateBalance() {
        lifecycleScope.launch {
            val email = prefs.getString("current_user", "") ?: return@launch
            val balance = db.transactionDao().getBalance(email)
            val formattedBalance = NumberFormat.getNumberInstance(Locale("ru")).format(balance ?: 0.0)
            balanceText.text = "Баланс: $formattedBalance ₽"
        }
    }
}