package com.example.financetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.financetracker.core.database.entity.AccountAliasEntity
import com.example.financetracker.core.database.entity.CategoryKeywordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KeywordDao {

    @Query("SELECT * FROM category_keywords ORDER BY usageCount DESC, lastUsedAt DESC")
    fun getAllCategoryKeywords(): Flow<List<CategoryKeywordEntity>>

    @Query("SELECT * FROM account_aliases ORDER BY usageCount DESC, lastUsedAt DESC")
    fun getAllAccountAliases(): Flow<List<AccountAliasEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategoryKeywordIgnore(entity: CategoryKeywordEntity): Long

    @Query(
        """
            UPDATE category_keywords
            SET usageCount = usageCount + 1, lastUsedAt = :timestamp
            WHERE keywordStem = :stem AND categoryId = :categoryId
        """
    )
    suspend fun incrementCategoryKeyword(stem: String, categoryId: Long, timestamp: Long)

    /**
     * Атомарно добавляет новое слово категории или увеличивает счетчик использований
     */
    @Transaction
    suspend fun upsertCategoryKeyword(
        stem: String,
        categoryId: Long,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val insertedRowId = insertCategoryKeywordIgnore(
            CategoryKeywordEntity(
                keywordStem = stem,
                categoryId = categoryId,
                usageCount = 1,
                lastUsedAt = timestamp
            )
        )
        if (insertedRowId == -1L) {
            incrementCategoryKeyword(stem, categoryId, timestamp)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccountAliasIgnore(entity: AccountAliasEntity): Long

    @Query(
        """
            UPDATE account_aliases
            SET usageCount = usageCount + 1, lastUsedAt = :timestamp
            WHERE aliasStem = :stem AND accountId = :accountId
        """
    )
    suspend fun incrementAccountAlias(stem: String, accountId: Long, timestamp: Long)

    /**
     * Атомарно добавляет новый алиас счета или увеличивает счетчик использований.
     */
    @Transaction
    suspend fun upsertAccountAlias(
        stem: String,
        accountId: Long,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val insertedRowId = insertAccountAliasIgnore(
            AccountAliasEntity(
                aliasStem = stem,
                accountId = accountId,
                usageCount = 1,
                lastUsedAt = timestamp
            )
        )
        if (insertedRowId == -1L) {
            incrementAccountAlias(stem, accountId, timestamp)
        }
    }

    @Query("DELETE FROM category_keywords WHERE keywordStem = :stem AND categoryId = :categoryId")
    suspend fun deleteCategoryKeyword(stem: String, categoryId: Long)

    @Query("DELETE FROM account_aliases WHERE aliasStem = :stem AND accountId = :accountId")
    suspend fun deleteAccountAlias(stem: String, accountId: Long)
}