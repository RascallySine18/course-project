package com.example.personalfinanceapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinanceapp.data.AppDatabase
import com.example.personalfinanceapp.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

class RegisterActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        db = AppDatabase.getDatabase(this)

        val emailEdit = findViewById<EditText>(R.id.emailEdit)
        val passwordEdit = findViewById<EditText>(R.id.passwordEdit)
        val registerBtn = findViewById<Button>(R.id.registerBtn)

        registerBtn.setOnClickListener {
            val email = emailEdit.text.toString().trim()
            val password = passwordEdit.text.toString()

            if (!isValidEmail(email) || !isValidPassword(password)) {
                AlertDialog.Builder(this)
                    .setTitle("Некорректные данные")
                    .setMessage("Требования к логину и паролю:\n\n" +
                            "- Логин (email): до 40 символов, только латинские буквы, цифры и символы (без кириллицы).\n\n" +
                            "- Пароль: минимум 8 символов, минимум одна заглавная буква, одна строчная буква, одна цифра, один специальный символ (!@#$%^&*), только латинские буквы (без кириллицы).")
                    .setPositiveButton("Продолжить") { dialog, _ -> dialog.dismiss() }
                    .setCancelable(false)
                    .show()
                return@setOnClickListener
            }

            val hashedPassword = hashPassword(password)
            CoroutineScope(Dispatchers.IO).launch {
                val existingUser = db.userDao().getUserByEmail(email)
                if (existingUser == null) {
                    db.userDao().insertUser(User(email, hashedPassword))
                    runOnUiThread {
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    }
                } else {
                    runOnUiThread {
                        AlertDialog.Builder(this@RegisterActivity)
                            .setTitle("Ошибка")
                            .setMessage("Пользователь уже существует")
                            .setPositiveButton("Продолжить") { dialog, _ -> dialog.dismiss() }
                            .setCancelable(false)
                            .show()
                    }
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]{1,40}@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]{8,}$".toRegex()
        return password.matches(passwordRegex)
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(password.toByteArray())
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}