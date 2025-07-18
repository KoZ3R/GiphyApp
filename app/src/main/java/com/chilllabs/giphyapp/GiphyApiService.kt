package com.chilllabs.giphyapp

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface GiphyApiService {
    @GET("v1/gifs/search")
    suspend fun searchGifs(
        @Query("api_key") apiKey: String = RetrofitClient.API_KEY,
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("rating") rating: String = "g",
        @Query("lang") lang: String = "en"
    ): GiphyResponse
    data class GiphyResponse(val data: List<GifData>)
    data class GifData(val id: String, val title: String, val images: Images)
    data class Images(
        @SerializedName("fixed_height") val fixedHeight: FixedHeight
    )
    data class FixedHeight(val url: String)


}