package com.example.financetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financetracker.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // Последние операции с лимитом, для гл экрана
    @Query("SELECT * FROM TRANSACTIONS WHERE isDeleted = 0 ORDER BY dateTime DESC LIMIT :limit")
    fun getRecentTransaction(limit: Int = 10): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE accountId = :accountId OR targetAccountId = :accountId")
    suspend fun deleteTransactionsByAccountId(accountId: Long)

    // Мягкое удаление и восстановление связанных операций счёта
    @Query("UPDATE transactions SET isDeleted = 1 WHERE accountId = :accountId OR targetAccountId = :accountId")
    suspend fun softDeleteTransactionsByAccountId(accountId: Long)

    @Query("UPDATE transactions SET isDeleted = 0 WHERE accountId = :accountId OR targetAccountId = :accountId")
    suspend fun restoreTransactionsByAccountId(accountId: Long)
}