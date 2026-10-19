package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {

    @Query("SELECT * FROM exchange_rates ORDER BY timestamp DESC")
    fun getAllRates(): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM exchange_rates WHERE sourceCurrency = :source AND targetCurrency = :target ORDER BY timestamp DESC")
    fun getHistoryForPair(source: String, target: String): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM exchange_rates WHERE sourceCurrency = :source AND targetCurrency = :target AND isActive = 1 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestActiveRate(source: String, target: String): ExchangeRateEntity?

    @Query("SELECT * FROM exchange_rates WHERE (sourceCurrency = :curr1 AND targetCurrency = :curr2) OR (sourceCurrency = :curr2 AND targetCurrency = :curr1) ORDER BY timestamp DESC")
    fun getRatesBetween(curr1: String, curr2: String): Flow<List<ExchangeRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: ExchangeRateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<ExchangeRateEntity>)

    @Update
    suspend fun updateRate(rate: ExchangeRateEntity)

    @Delete
    suspend fun deleteRate(rate: ExchangeRateEntity)

    @Query("DELETE FROM exchange_rates WHERE id = :id")
    suspend fun deleteRateById(id: Long)
}
