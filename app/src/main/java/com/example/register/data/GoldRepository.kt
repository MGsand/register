package com.example.register.data

import com.example.register.api.GoldApiService
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class GoldRepository {

    private val apiService: GoldApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.cbr.ru/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
            .create(GoldApiService::class.java)
    }

    suspend fun getGoldRate(): Double {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val response = apiService.getGoldRate(today)

        return response.valCurs?.valute?.firstOrNull { it.charcode == "XAU" }?.value?.toDouble() ?: 4500.0
    }
}