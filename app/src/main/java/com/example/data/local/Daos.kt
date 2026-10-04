package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()

    @Query("UPDATE transactions SET category = :category WHERE merchant = :merchant")
    suspend fun updateCategoryForMerchant(merchant: String, category: String)
}

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY value DESC")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assets: List<AssetEntity>)

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)

    @Query("DELETE FROM assets")
    suspend fun clearAll()
}

@Dao
interface LiabilityDao {
    @Query("SELECT * FROM liabilities ORDER BY outstandingAmount DESC")
    fun getAllLiabilities(): Flow<List<LiabilityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiability(liability: LiabilityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(liabilities: List<LiabilityEntity>)

    @Update
    suspend fun updateLiability(liability: LiabilityEntity)

    @Delete
    suspend fun deleteLiability(liability: LiabilityEntity)

    @Query("DELETE FROM liabilities")
    suspend fun clearAll()
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY id ASC")
    fun getAllGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<SavingsGoalEntity>)

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals")
    suspend fun clearAll()
}

@Dao
interface CategoryRuleDao {
    @Query("SELECT * FROM category_rules")
    fun getAllRules(): Flow<List<CategoryRuleEntity>>

    @Query("SELECT * FROM category_rules WHERE pattern = :pattern LIMIT 1")
    suspend fun getRuleForPattern(pattern: String): CategoryRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: CategoryRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<CategoryRuleEntity>)
}
