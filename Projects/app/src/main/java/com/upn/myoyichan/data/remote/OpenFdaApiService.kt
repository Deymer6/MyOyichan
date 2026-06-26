package com.upn.myoyichan.data.remote

import com.upn.myoyichan.data.remote.model.OpenFdaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFdaApiService {

    @GET("drug/label.json")
    suspend fun buscarMedicamento(
        @Query("search") search: String,
        @Query("limit") limit: Int = 1
    ): OpenFdaResponse
}
