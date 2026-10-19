package com.example.data.repository

import com.example.data.dao.ExchangeRateDao
import com.example.data.model.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow

class ExchangeRateRepository(
    private val exchangeRateDao: ExchangeRateDao
) {
    val allRates: Flow<List<ExchangeRateEntity>> = exchangeRateDao.getAllRates()

    fun getHistoryForPair(source: String, target: String): Flow<List<ExchangeRateEntity>> =
        exchangeRateDao.getHistoryForPair(source, target)

    fun getRatesBetween(curr1: String, curr2: String): Flow<List<ExchangeRateEntity>> =
        exchangeRateDao.getRatesBetween(curr1, curr2)

    suspend fun getLatestActiveRate(source: String, target: String): ExchangeRateEntity? =
        exchangeRateDao.getLatestActiveRate(source, target)

    suspend fun saveRate(rate: ExchangeRateEntity): Long =
        exchangeRateDao.insertRate(rate)

    suspend fun updateRate(rate: ExchangeRateEntity) =
        exchangeRateDao.updateRate(rate)

    suspend fun deleteRate(rate: ExchangeRateEntity) =
        exchangeRateDao.deleteRate(rate)

    suspend fun deleteRateById(id: Long) =
        exchangeRateDao.deleteRateById(id)

    /**
     * محاسبه هوشمند نرخ مستقیم، معکوس و تبدیل متقاطع (Cross-Rate) بین هر دو ارز
     * @return Pair<نرخ مستقیم (۱ منبع = چند مقصد), نرخ معکوس (۱ مقصد = چند منبع)>
     */
    suspend fun resolveRateAndInverse(
        source: String,
        target: String,
        baseCurrency: String = "TOMAN",
        fallbackRatesToBase: Map<String, Double> = emptyMap()
    ): Pair<Double, Double> {
        val s = source.uppercase()
        val t = target.uppercase()
        val b = baseCurrency.uppercase()

        if (s == t) return Pair(1.0, 1.0)

        // 1. بررسی نرخ مستقیم منبع به مقصد
        val direct = exchangeRateDao.getLatestActiveRate(s, t)
        if (direct != null && direct.rate > 0) {
            val inv = if (direct.inverseRate > 0) direct.inverseRate else 1.0 / direct.rate
            return Pair(direct.rate, inv)
        }

        // 2. بررسی نرخ معکوس مقصد به منبع
        val reverse = exchangeRateDao.getLatestActiveRate(t, s)
        if (reverse != null && reverse.rate > 0) {
            val directRate = 1.0 / reverse.rate
            return Pair(directRate, reverse.rate)
        }

        // 3. محاسبه از طریق تبدیل متقاطع بر پایه ارز مبنا (Base Currency)
        // Rate(S -> Base) = rateS
        val rateS = if (s == b) 1.0 else {
            val r = exchangeRateDao.getLatestActiveRate(s, b)?.rate
            r ?: fallbackRatesToBase[s] ?: 1.0
        }

        // Rate(T -> Base) = rateT
        val rateT = if (t == b) 1.0 else {
            val r = exchangeRateDao.getLatestActiveRate(t, b)?.rate
            r ?: fallbackRatesToBase[t] ?: 1.0
        }

        if (rateT > 0 && rateS > 0) {
            val crossRate = rateS / rateT
            val crossInverse = rateT / rateS
            return Pair(crossRate, crossInverse)
        }

        return Pair(1.0, 1.0)
    }
}
