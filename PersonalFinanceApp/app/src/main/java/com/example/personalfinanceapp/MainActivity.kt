package com.example.personalfinanceapp

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinanceapp.data.AppDatabase
import com.google.android.material.navigation.NavigationView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences
    private lateinit var balanceText: TextView
    private lateinit var rootLayout: CoordinatorLayout
    private lateinit var pieChart: PieChart
    private lateinit var totalSumText: TextView
    private lateinit var legendRecyclerView: RecyclerView
    private lateinit var legendAdapter: LegendAdapter
    private lateinit var periodSpinner: Spinner
    private lateinit var addTransactionBtn: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private var currentType: String = "expense"
    private var currentPeriod: String = "День"
    private var selectedYear: Int? = null

    private val addTransactionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            updateBalance()
            updatePieChart()
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
                loadAndSetBackground(rootLayout, navView.getHeaderView(0), file.absolutePath)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rootLayout = findViewById(R.id.rootLayout)
        db = AppDatabase.getDatabase(this)
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        createNotificationChannel()

        val email = prefs.getString("current_user", null) ?: run {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        balanceText = findViewById(R.id.balanceText)
        pieChart = findViewById(R.id.pieChart)
        totalSumText = findViewById(R.id.totalSumText)
        legendRecyclerView = findViewById(R.id.legendRecyclerView)
        periodSpinner = findViewById(R.id.periodSpinner)
        addTransactionBtn = findViewById(R.id.addTransactionBtn)
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)

        // Настройка диаграммы
        pieChart.isRotationEnabled = false // Отключаем вращение
        pieChart.description.isEnabled = false // Убираем "Description Label"
        pieChart.legend.isEnabled = false // Отключаем встроенную легенду
        pieChart.setDrawEntryLabels(false) // Убираем надписи на секторах
        pieChart.setUsePercentValues(false) // Отключаем проценты
        pieChart.setDrawHoleEnabled(true) // Включаем отверстие в центре для totalSumText
        pieChart.setHoleColor(ContextCompat.getColor(this, R.color.transparent)) // Прозрачное отверстие
        pieChart.setTransparentCircleColor(ContextCompat.getColor(this, R.color.transparent)) // Прозрачный круг
        pieChart.setCenterText("") // Убираем текст в центре диаграммы
        pieChart.setDrawCenterText(false) // Отключаем текст в центре

        legendAdapter = LegendAdapter()
        legendRecyclerView.adapter = legendAdapter
        legendRecyclerView.layoutManager = LinearLayoutManager(this)

        applyBackground()

        findViewById<ImageButton>(R.id.menuBtn).setOnClickListener {
            drawerLayout.openDrawer(navView)
        }

        findViewById<ImageButton>(R.id.historyBtn).setOnClickListener {
            viewTransactions(email)
        }

        findViewById<Button>(R.id.incomeSwitchBtn).setOnClickListener {
            currentType = "income"
            addTransactionBtn.text = "Добавить доход"
            addTransactionBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.button_dark_green)
            updatePieChart()
        }

        findViewById<Button>(R.id.expenseSwitchBtn).setOnClickListener {
            currentType = "expense"
            addTransactionBtn.text = "Добавить расход"
            addTransactionBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.button_dark_red)
            updatePieChart()
        }

        addTransactionBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java).apply {
                putExtra("USER_EMAIL", email)
                putExtra("TYPE", currentType)
            }
            addTransactionResult.launch(intent)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    prefs.edit().remove("current_user").apply()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_faq -> {
                    startActivity(Intent(this, FAQActivity::class.java))
                    true
                }
                R.id.nav_recurring_payments -> {
                    startActivity(Intent(this, RecurringPaymentsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        periodSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.periods,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        periodSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                currentPeriod = parent.getItemAtPosition(position).toString()
                if (currentPeriod == "Выбрать год") {
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(
                        this@MainActivity,
                        { _, year, _, _ ->
                            selectedYear = year
                            updatePieChart()
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                } else {
                    selectedYear = null
                    updatePieChart()
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

        updateBalance()
        updatePieChart()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recurring Payments"
            val descriptionText = "Notifications for recurring payments"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("recurring_payments_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updatePieChart() {
        lifecycleScope.launch {
            val email = prefs.getString("current_user", "") ?: return@launch
            val transactions = db.transactionDao().getTransactionsForUser(email).first()
            val filteredTransactions = transactions.filter { it.type == currentType && isInPeriod(it.date) }
            val categorySums = filteredTransactions.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
            val totalSum = categorySums.values.sum()

            val entries = categorySums.map { (category, sum) -> PieEntry(sum.toFloat(), category) }
            val colors = listOf(
                ContextCompat.getColor(this@MainActivity, R.color.color1),
                ContextCompat.getColor(this@MainActivity, R.color.color2),
                ContextCompat.getColor(this@MainActivity, R.color.color3),
                ContextCompat.getColor(this@MainActivity, R.color.color4),
                ContextCompat.getColor(this@MainActivity, R.color.color5),
                ContextCompat.getColor(this@MainActivity, R.color.color6)
            )
            val dataSet = PieDataSet(entries, "")
            dataSet.colors = colors.take(categorySums.size)
            dataSet.valueTextColor = ContextCompat.getColor(this@MainActivity, R.color.black)
            dataSet.valueTextSize = 12f
            dataSet.setDrawValues(false)

            val pieData = PieData(dataSet)
            pieChart.data = pieData
            pieChart.invalidate()

            val numberFormat = NumberFormat.getNumberInstance(Locale("ru")).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            val formattedSum = numberFormat.format(totalSum)
            totalSumText.text = "$formattedSum ₽"

            legendAdapter.submitList(categorySums.entries.mapIndexed { index, entry ->
                val percentage = if (totalSum > 0) (entry.value / totalSum * 100).toInt() else 0
                LegendItem(entry.key, entry.value, colors.getOrElse(index) { colors.last() }, percentage)
            })
        }
    }

    private fun isInPeriod(date: Date): Boolean {
        val cal = Calendar.getInstance()
        cal.time = date
        val now = Calendar.getInstance()

        return when (currentPeriod) {
            "День" -> cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
            "Неделя" -> cal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR) && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
            "Месяц" -> cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
            "Год" -> cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
            "Выбрать год" -> selectedYear?.let { cal.get(Calendar.YEAR) == it } ?: false
            else -> true
        }
    }

    private fun applyBackground() {
        val path = prefs.getString("background_path", null)
        if (path != null) {
            loadAndSetBackground(rootLayout, navView.getHeaderView(0), path)
        } else {
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            navView.getHeaderView(0).setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }
    }

    private fun loadAndSetBackground(layout: CoordinatorLayout, navHeader: View, path: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(path)
                layout.background = BitmapDrawable(resources, bitmap)
                navHeader.background = BitmapDrawable(resources, bitmap)
            } else {
                layout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                navHeader.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                prefs.edit().remove("background_path").apply()
            }
        } catch (e: Exception) {
            layout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            navHeader.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            prefs.edit().remove("background_path").apply()
        }
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
            val numberFormat = NumberFormat.getNumberInstance(Locale("ru")).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            val formattedBalance = numberFormat.format(balance ?: 0.0)
            balanceText.text = "Баланс: $formattedBalance ₽"
        }
    }
}