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

class LoginActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val currentUser = prefs.getString("current_user", null)
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)
        db = AppDatabase.getDatabase(this)

        val emailEdit = findViewById<EditText>(R.id.emailEdit)
        val passwordEdit = findViewById<EditText>(R.id.passwordEdit)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val registerBtn = findViewById<Button>(R.id.registerBtn)

        loginBtn.setOnClickListener {
            val email = emailEdit.text.toString()
            val password = passwordEdit.text.toString()

            val hashedPassword = hashPassword(password)
            CoroutineScope(Dispatchers.IO).launch {
                val user = db.userDao().getUserByEmail(email)
                if (user != null && user.passwordHash == hashedPassword) {
                    prefs.edit().putString("current_user", email).apply()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    runOnUiThread {
                        AlertDialog.Builder(this@LoginActivity)
                            .setTitle("Ошибка")
                            .setMessage("Неверный email или пароль")
                            .setPositiveButton("Продолжить") { dialog, _ -> dialog.dismiss() }
                            .setCancelable(false)
                            .show()
                    }
                }
            }
        }

        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(password.toByteArray())
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}