package com.example.financetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financetracker.core.database.entity.TransactionEntity
import com.example.financetracker.core.model.MonthlySummary
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

    @Query("SELECT * FROM transactions WHERE accountId = :accountId OR targetAccountId = :accountId ORDER BY dateTime DESC LIMIT :limit")
    suspend fun getTransactionsByAccountId(accountId: Long, limit: Int = 10): List<TransactionEntity>

    // Доходы и расходы за период по всем счетам (переводы не учитываются в доходах/расходах)
    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0.0 END), 0.0) AS income,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0.0 END), 0.0) AS expense
        FROM transactions
        WHERE isDeleted = 0
            AND dateTime >= :startTime AND dateTime < :endTime
    """)
    fun getMonthlySummary(startTime: Long, endTime: Long): Flow<MonthlySummary>

    // Доходы и расходы за период для конкретного счёта
    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0.0 END), 0.0) AS income,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0.0 END), 0.0) AS expense
        FROM transactions 
        WHERE isDeleted = 0 
          AND dateTime >= :startTime AND dateTime < :endTime
          AND (accountId = :accountId OR targetAccountId = :accountId)
    """)
    fun getMonthlySummaryByAccountId(accountId: Long, startTime: Long, endTime: Long): Flow<MonthlySummary>
}