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
    version = 1,
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

                if (database.transactionDao().getCount() == 0) {
                    val sampleTxs = SampleDataProvider.getSampleTransactions().map {
                        TransactionEntity.fromDomain(it)
                    }
                    database.transactionDao().insertTransactions(sampleTxs)
                }

                if (database.orderDao().getCount() == 0) {
                    val sampleOrders = SampleDataProvider.getSampleOrders().map {
                        OrderEntity.fromDomain(it)
                    }
                    database.orderDao().insertOrders(sampleOrders)
                }

                if (database.productDao().getCount() == 0) {
                    val sampleProducts = SampleDataProvider.getSampleProducts().map {
                        ProductEntity.fromDomain(it)
                    }
                    database.productDao().insertProducts(sampleProducts)
                }

                if (database.customerDao().getCount() == 0) {
                    val sampleCustomers = SampleDataProvider.getSampleCustomers().map {
                        CustomerEntity.fromDomain(it)
                    }
                    database.customerDao().insertCustomers(sampleCustomers)
                }

                if (database.supplierDao().getCount() == 0) {
                    val sampleSuppliers = SampleDataProvider.getSampleSuppliers().map {
                        SupplierEntity.fromDomain(it)
                    }
                    database.supplierDao().insertSuppliers(sampleSuppliers)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
