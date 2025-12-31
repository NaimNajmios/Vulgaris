package com.najmi.vulgaris.data.api

import com.najmi.vulgaris.data.model.CerebrasRequest
import com.najmi.vulgaris.data.model.CerebrasResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CerebrasApi {
    
    companion object {
        const val BASE_URL = "https://api.cerebras.ai/v1/"
    }
    
    @POST("chat/completions")
    suspend fun generateContent(
        @Body request: CerebrasRequest,
        @Header("Authorization") authHeader: String
    ): CerebrasResponse
}
