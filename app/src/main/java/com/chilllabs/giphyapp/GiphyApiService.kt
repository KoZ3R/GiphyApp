package com.chilllabs.giphyapp

import retrofit2.http.GET
import retrofit2.http.Query

interface GiphyApiService {
    @GET("gifs/search")
    suspend fun searchGifs(
        @Query("api_key") apiKey: String = RetrofitClient.API_KEY,
        @Query("q") query: String,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0,
        @Query("rating") rating: String = "g",
        @Query("lang") lang: String = "en"
    ): GiphyResponse
}