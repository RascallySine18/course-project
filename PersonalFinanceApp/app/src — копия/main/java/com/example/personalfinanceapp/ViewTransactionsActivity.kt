package com.example.personalfinanceapp

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinanceapp.data.AppDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class ViewTransactionsActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var adapter: TransactionAdapter
    private lateinit var prefs: SharedPreferences
    private lateinit var rootLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_transactions)
        rootLayout = findViewById(R.id.rootLayout)
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        db = AppDatabase.getDatabase(this)

        applyBackground()

        val email = intent.getStringExtra("USER_EMAIL") ?: run {
            finish()
            return
        }

        val recyclerView = findViewById<RecyclerView>(R.id.transactionsRecyclerView)
        adapter = TransactionAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            db.transactionDao().getTransactionsForUser(email).collectLatest { transactions ->
                adapter.submitList(transactions)
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