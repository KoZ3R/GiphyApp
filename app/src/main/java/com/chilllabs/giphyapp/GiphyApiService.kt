package com.chilllabs.giphyapp

import retrofit2.http.GET
import retrofit2.http.Query

interface GiphyApiService {
    @GET("gifs/search")
    suspend fun searchGifs(
        @Query("q") query: String,
        @Query("api_key") apiKey: String = RetrofitClient.API_KEY,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("rating") rating: String = "g",
        @Query("lang") lang: String = "en"
    ): GifResponse

    data class GifResponse(
        val data: List<GifData>,
        val pagination: Pagination
    )
    data class Pagination(
        val total_count: Int,
        val count: Int,
        val offset: Int
    )
    data class GifData(
        val id: String,
        val title: String,
        val images: Images
    )
    data class Images(
        val fixed_height: Image
    )
    data class Image(
        val url: String
    )
}