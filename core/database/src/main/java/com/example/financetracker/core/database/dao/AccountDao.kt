package com.example.financetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financetracker.core.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE isDeleted = 0")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Query("UPDATE accounts SET balance = balance + :amountDelta WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, amountDelta: Double)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)

    // Запрос списка корзины
    @Query("SELECT * FROM accounts WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE isDeleted = 1")
    suspend fun getDeletedAccountsList(): List<AccountEntity>

    // Поиск счёта в корзине по имени
    @Query("SELECT * FROM accounts WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) AND isDeleted = 1 LIMIT 1")
    suspend fun findDeletedAccountByName(name: String): AccountEntity?

    // Мягкое удаление и восстановление:
    @Query("UPDATE accounts SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteAccount(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE accounts SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreAccount(id: Long)
}