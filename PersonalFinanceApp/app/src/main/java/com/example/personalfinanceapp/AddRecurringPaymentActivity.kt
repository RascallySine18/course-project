package com.example.personalfinanceapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
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
import com.example.personalfinanceapp.data.RecurringPayment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddRecurringPaymentActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences
    private lateinit var rootLayout: LinearLayout
    private lateinit var startDateTextView: Button
    private lateinit var endDateTextView: Button
    private lateinit var timeTextView: Button
    private var startDate: Date = Date()
    private var endDate: Date? = null
    private var timeOfDay: String = "00:00"
    private var editingPayment: RecurringPayment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recurring_payment)
        rootLayout = findViewById(R.id.rootLayout)
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        db = AppDatabase.getDatabase(this)

        applyBackground()

        val email = intent.getStringExtra("USER_EMAIL") ?: run {
            Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val paymentId = intent.getLongExtra("PAYMENT_ID", -1)
        if (paymentId != -1L) {
            loadPaymentForEdit(paymentId)
        }

        startDateTextView = findViewById(R.id.startDateTextView)
        endDateTextView = findViewById(R.id.endDateTextView)
        timeTextView = findViewById(R.id.timeTextView)
        val periodicitySpinner = findViewById<Spinner>(R.id.periodicitySpinner)
        val titleEdit = findViewById<EditText>(R.id.titleEdit)
        val amountEdit = findViewById<EditText>(R.id.amountEdit)
        val saveBtn = findViewById<Button>(R.id.saveBtn)

        // Установка начальной даты
        updateDateTextView(startDateTextView, startDate)
        updateDateTextView(endDateTextView, endDate)
        updateTimeTextView(timeTextView, timeOfDay)

        // DatePicker для даты начала
        startDateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = startDate
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val calendarNew = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                startDate = calendarNew.time
                updateDateTextView(startDateTextView, startDate)
            }, year, month, day).show()
        }

        // DatePicker для даты окончания (опционально)
        endDateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val calendarNew = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                endDate = calendarNew.time
                updateDateTextView(endDateTextView, endDate)
            }, year, month, day).show()
        }

        // TimePicker для времени
        timeTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                timeOfDay = String.format("%02d:%02d", selectedHour, selectedMinute)
                updateTimeTextView(timeTextView, timeOfDay)
            }, hour, minute, true).show()
        }

        // Спиннер для периодичности
        val periodicities = resources.getStringArray(R.array.periodicities)
        val periodicityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periodicities).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        periodicitySpinner.adapter = periodicityAdapter

        saveBtn.setOnClickListener {
            val title = titleEdit.text.toString()
            val amountText = amountEdit.text.toString()
            val periodicity = periodicitySpinner.selectedItem.toString()

            if (title.isEmpty() || amountText.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Введите корректную сумму", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val payment = editingPayment?.copy(
                title = title,
                periodicity = periodicity,
                startDate = startDate,
                timeOfDay = timeOfDay,
                endDate = endDate,
                amount = amount
            ) ?: RecurringPayment(
                userEmail = email,
                title = title,
                periodicity = periodicity,
                startDate = startDate,
                timeOfDay = timeOfDay,
                endDate = endDate,
                amount = amount
            )

            CoroutineScope(Dispatchers.IO).launch {
                if (editingPayment != null) {
                    db.recurringPaymentDao().updateRecurringPayment(payment)
                } else {
                    db.recurringPaymentDao().insertRecurringPayment(payment)
                }
                scheduleNotification(payment)
                runOnUiThread {
                    Toast.makeText(this@AddRecurringPaymentActivity, if (editingPayment != null) "Напоминание обновлено" else "Напоминание добавлено", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    private fun loadPaymentForEdit(paymentId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val payments = db.recurringPaymentDao().getRecurringPaymentsForUser(prefs.getString("current_user", "")!!).first()
            editingPayment = payments.find { payment -> payment.id == paymentId }
            editingPayment?.let { payment ->
                runOnUiThread {
                    findViewById<EditText>(R.id.titleEdit).setText(payment.title)
                    findViewById<EditText>(R.id.amountEdit).setText(payment.amount.toString())
                    val periodicitySpinner = findViewById<Spinner>(R.id.periodicitySpinner)
                    val periodicities = resources.getStringArray(R.array.periodicities)
                    periodicitySpinner.setSelection(periodicities.indexOf(payment.periodicity))
                    startDate = payment.startDate
                    timeOfDay = payment.timeOfDay
                    endDate = payment.endDate
                    updateDateTextView(findViewById(R.id.startDateTextView), startDate)
                    updateTimeTextView(findViewById(R.id.timeTextView), timeOfDay)
                    updateDateTextView(findViewById(R.id.endDateTextView), endDate)
                    findViewById<Button>(R.id.saveBtn).text = "Обновить"
                }
            }
        }
    }

    private fun scheduleNotification(payment: RecurringPayment) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("TITLE", payment.title)
            putExtra("AMOUNT", payment.amount)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            payment.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            time = payment.startDate
            val (hour, minute) = payment.timeOfDay.split(":").map { it.toInt() }
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val interval = when (payment.periodicity) {
            "Ежедневно" -> AlarmManager.INTERVAL_DAY
            "Еженедельно" -> AlarmManager.INTERVAL_DAY * 7
            "Ежемесячно" -> AlarmManager.INTERVAL_DAY * 30
            "Ежегодно" -> AlarmManager.INTERVAL_DAY * 365
            else -> AlarmManager.INTERVAL_DAY
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            interval,
            pendingIntent
        )
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

    private fun updateDateTextView(button: Button, date: Date?) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        button.text = if (date != null) "Дата: ${dateFormat.format(date)}" else "Выберите дату"
    }

    private fun updateTimeTextView(button: Button, time: String) {
        button.text = "Время: $time"
    }
}