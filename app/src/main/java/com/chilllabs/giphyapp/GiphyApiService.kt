package com.chilllabs.giphyapp

import retrofit2.http.GET
import retrofit2.http.Query

interface GiphyApiService {
    @GET("gifs/search")
    suspend fun searchGifs(
        @Query("api_key") apiKey: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): GiphyResponse
}