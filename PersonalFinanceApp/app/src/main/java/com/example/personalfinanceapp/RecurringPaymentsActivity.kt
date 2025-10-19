package com.example.personalfinanceapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinanceapp.data.AppDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.content.SharedPreferences

class RecurringPaymentsActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var adapter: RecurringPaymentAdapter
    private lateinit var prefs: SharedPreferences
    private lateinit var rootLayout: LinearLayout

    private val addOrEditResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Обновляем список после добавления/редактирования
            lifecycleScope.launch {
                db.recurringPaymentDao().getRecurringPaymentsForUser(prefs.getString("current_user", "")!!).collectLatest { payments ->
                    adapter.submitList(payments)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recurring_payments)
        rootLayout = findViewById(R.id.rootLayout)
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        db = AppDatabase.getDatabase(this)

        applyBackground()

        val email = prefs.getString("current_user", null) ?: run {
            finish()
            return
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recurringPaymentsRecyclerView)
        adapter = RecurringPaymentAdapter(
            onDeleteClick = { paymentId ->
                lifecycleScope.launch {
                    db.recurringPaymentDao().deleteRecurringPayment(paymentId)
                }
            },
            onEditClick = { paymentId ->
                val intent = Intent(this, AddRecurringPaymentActivity::class.java).apply {
                    putExtra("USER_EMAIL", email)
                    putExtra("PAYMENT_ID", paymentId)
                }
                addOrEditResult.launch(intent)
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            db.recurringPaymentDao().getRecurringPaymentsForUser(email).collectLatest { payments ->
                adapter.submitList(payments)
            }
        }

        findViewById<Button>(R.id.addRecurringPaymentBtn).setOnClickListener {
            val intent = Intent(this, AddRecurringPaymentActivity::class.java).apply {
                putExtra("USER_EMAIL", email)
            }
            addOrEditResult.launch(intent)
        }
    }

    private fun applyBackground() {
        val path = prefs.getString("background_path", null)
        if (path != null) {
            try {
                val file = File(path)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(path)
                    rootLayout.background = BitmapDrawable(resources, bitmap)
                } else {
                    rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    prefs.edit().remove("background_path").apply()
                }
            } catch (e: Exception) {
                rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                prefs.edit().remove("background_path").apply()
            }
        }
    }
}