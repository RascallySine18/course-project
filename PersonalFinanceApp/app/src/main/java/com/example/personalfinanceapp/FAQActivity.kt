package com.example.personalfinanceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalfinanceapp.ui.theme.PersonalFinanceAppTheme

class FAQActivity : ComponentActivity() {

    data class FAQItem(val question: String, val answer: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalFinanceAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    FAQScreen()
                }
            }
        }
    }

    @Composable
    fun FAQScreen() {
        val faqItems = listOf(
            FAQItem(
                "Как добавить транзакцию?",
                "Нажмите кнопку \"Добавить доход\" или \"Добавить расход\" в нижней части экрана, выберите категорию, введите сумму и дату."
            ),
            FAQItem(
                "Как переключаться между доходами и расходами?",
                "Используйте кнопки \"Доходы\" и \"Расходы\" в верхней части экрана для переключения отображаемых данных."
            ),
            FAQItem(
                "Как просмотреть историю транзакций?",
                "Нажмите на иконку истории в левом верхнем углу рядом с кнопкой меню."
            ),
            FAQItem(
                "Как настроить уведомления для регулярных платежей?",
                "Откройте боковое меню, нажав на иконку с тремя линиями, и выберите \"Регулярные платежи\". Затем в нижней части экрана нажмите \"Добавить\"."
            ),
            FAQItem(
                "Как выйти из аккаунта?",
                "Откройте боковое меню, нажав на иконку с тремя линиями, и выберите \"Выход из аккаунта\"."
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.White)
        ) {
            Text(
                text = "Часто задаваемые вопросы",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp
                ),
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(faqItems) { faqItem ->
                    FAQCard(faqItem)
                }
            }
        }
    }

    @Composable
    fun FAQCard(faqItem: FAQItem) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5) // light_gray
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.medium // Rounded corners similar to cardCornerRadius="16dp"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = faqItem.question,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = faqItem.answer,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    color = Color.Black
                )
            }
        }
    }
}