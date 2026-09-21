package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.StyleSphereDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.AccountType
import com.example.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabaseTest {

    private lateinit var db: StyleSphereDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, StyleSphereDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadTransaction() = runBlocking {
        val tx = TransactionEntity(
            id = "TX-TEST-001",
            type = TransactionType.INCOME.name,
            amount = 1500.0,
            dateMillis = System.currentTimeMillis(),
            timeString = "12:00 PM",
            categoryName = "Product Sale",
            accountId = "acc_cash",
            description = "Test Sale",
            reference = "INV-001",
            notes = "Cash in hand",
            createdAtMillis = System.currentTimeMillis()
        )

        db.transactionDao().insertTransaction(tx)
        val fetched = db.transactionDao().getTransactionById("TX-TEST-001")

        assertNotNull(fetched)
        assertEquals(1500.0, fetched?.amount ?: 0.0, 0.001)
        assertEquals("Product Sale", fetched?.categoryName)
    }

    @Test
    fun insertAndReadAccount() = runBlocking {
        val acc = AccountEntity(
            id = "acc_bkash_test",
            name = "bKash Test",
            type = AccountType.BKASH.name,
            accountNumber = "01700000000",
            openingBalance = 5000.0,
            currentBalance = 5000.0,
            totalMoneyIn = 0.0,
            totalMoneyOut = 0.0,
            transactionCount = 0,
            isSystemDefault = false
        )

        db.accountDao().insertAccount(acc)
        val all = db.accountDao().getAllAccounts().first()

        assertEquals(1, all.size)
        assertEquals("bKash Test", all[0].name)
        assertEquals(5000.0, all[0].currentBalance, 0.001)
    }
}
