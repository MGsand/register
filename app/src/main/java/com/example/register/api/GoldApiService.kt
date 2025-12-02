package com.example.register.api

import retrofit2.http.GET
import retrofit2.http.Query
import com.example.register.data.response.GoldResponse

interface GoldApiService {
    @GET("scripts/XML_dynamic.asp")
    suspend fun getGoldRate(
        @Query("date_req1") date: String,
        @Query("date_req2") date2: String? = null,
        @Query("VAL_NM_RQ") valNmRq: String = "R01239"
    ): GoldResponse
}