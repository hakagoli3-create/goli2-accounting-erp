package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AccountingDao
import com.example.data.dao.ExpenseAndIncomeDao
import com.example.data.dao.InventoryDao
import com.example.data.dao.InvoiceDao
import com.example.data.dao.PartyDao
import com.example.data.dao.TreasuryDao
import com.example.data.dao.UserDao
import com.example.data.model.AccountGroupEntity
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ChequeEntity
import com.example.data.model.ExpenseCategoryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.GeneralAccountEntity
import com.example.data.model.IncomeCategoryEntity
import com.example.data.model.IncomeEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.JournalItemEntity
import com.example.data.model.PartyEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SubsidiaryAccountEntity
import com.example.data.model.UserEntity
import com.example.data.model.WarehouseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountGroupEntity::class,
        GeneralAccountEntity::class,
        SubsidiaryAccountEntity::class,
        JournalEntryEntity::class,
        JournalItemEntity::class,
        WarehouseEntity::class,
        ProductEntity::class,
        InventoryMovementEntity::class,
        PartyEntity::class,
        CashDeskEntity::class,
        BankAccountEntity::class,
        ChequeEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        ExpenseCategoryEntity::class,
        ExpenseEntity::class,
        IncomeCategoryEntity::class,
        IncomeEntity::class,
        UserEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountingDao(): AccountingDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun partyDao(): PartyDao
    abstract fun treasuryDao(): TreasuryDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun expenseAndIncomeDao(): ExpenseAndIncomeDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE warehouses ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS expense_categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        code TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        iconName TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        categoryId INTEGER NOT NULL,
                        categoryTitle TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        jalaliDate TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        paymentMethod TEXT NOT NULL,
                        cashDeskId INTEGER,
                        bankAccountId INTEGER,
                        accountName TEXT NOT NULL,
                        title TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        voucherId INTEGER
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_categoryId ON expenses(categoryId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_jalaliDate ON expenses(jalaliDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_timestamp ON expenses(timestamp)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS income_categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        code TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        iconName TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS incomes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        categoryId INTEGER NOT NULL,
                        categoryTitle TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        jalaliDate TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        depositMethod TEXT NOT NULL,
                        cashDeskId INTEGER,
                        bankAccountId INTEGER,
                        accountName TEXT NOT NULL,
                        title TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        voucherId INTEGER
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_incomes_categoryId ON incomes(categoryId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_incomes_jalaliDate ON incomes(jalaliDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_incomes_timestamp ON incomes(timestamp)")
            }
        }

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        username TEXT NOT NULL,
                        passwordHash TEXT NOT NULL,
                        fullName TEXT NOT NULL,
                        role TEXT NOT NULL,
                        mobile TEXT NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_username ON users(username)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "goli_accounting_erp.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default Iranian chart of accounts, warehouses, products, parties, and users
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).let { appDb ->
                                    DatabaseSeeder.seedDefaultData(appDb)
                                }
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).let { appDb ->
                                    DatabaseSeeder.ensureDefaultUsers(appDb)
                                }
                            }
                        }
                    })
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
