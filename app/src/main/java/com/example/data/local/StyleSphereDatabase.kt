package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import com.example.data.sample.SampleDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        AccountEntity::class,
        OrderEntity::class,
        ProductEntity::class,
        CustomerEntity::class,
        SupplierEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class StyleSphereDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun orderDao(): OrderDao
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao

    companion object {
        @Volatile
        private var INSTANCE: StyleSphereDatabase? = null

        fun getInstance(context: Context): StyleSphereDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StyleSphereDatabase::class.java,
                    "stylesphere_business.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Trigger async seeding on database creation
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(getInstance(context.applicationContext))
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(database: StyleSphereDatabase) {
            try {
                if (database.accountDao().getCount() == 0) {
                    val defaultAccounts = SampleDataProvider.getDefaultAccounts().map {
                        AccountEntity.fromDomain(it)
                    }
                    database.accountDao().insertAccounts(defaultAccounts)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
