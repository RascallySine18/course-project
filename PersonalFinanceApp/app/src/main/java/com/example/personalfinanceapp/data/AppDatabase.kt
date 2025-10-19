package com.example.personalfinanceapp.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

@Database(entities = [User::class, Transaction::class, RecurringPayment::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringPaymentDao(): RecurringPaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN category TEXT NOT NULL DEFAULT 'Другое'")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Пустая миграция
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE recurring_payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userEmail TEXT NOT NULL,
                        title TEXT NOT NULL,
                        periodicity TEXT NOT NULL,
                        startDate INTEGER NOT NULL,
                        timeOfDay TEXT NOT NULL,
                        endDate INTEGER,
                        category TEXT NOT NULL,
                        amount REAL NOT NULL,
                        comment TEXT
                    )
                """)
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Создаем новую таблицу без category и comment
                database.execSQL("""
                    CREATE TABLE recurring_payments_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userEmail TEXT NOT NULL,
                        title TEXT NOT NULL,
                        periodicity TEXT NOT NULL,
                        startDate INTEGER NOT NULL,
                        timeOfDay TEXT NOT NULL,
                        endDate INTEGER,
                        amount REAL NOT NULL
                    )
                """)
                // Копируем данные из старой таблицы
                database.execSQL("""
                    INSERT INTO recurring_payments_new (id, userEmail, title, periodicity, startDate, timeOfDay, endDate, amount)
                    SELECT id, userEmail, title, periodicity, startDate, timeOfDay, endDate, amount FROM recurring_payments
                """)
                // Удаляем старую таблицу
                database.execSQL("DROP TABLE recurring_payments")
                // Переименовываем новую таблицу
                database.execSQL("ALTER TABLE recurring_payments_new RENAME TO recurring_payments")
            }
        }
    }
}