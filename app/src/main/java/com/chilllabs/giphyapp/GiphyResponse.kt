package com.chilllabs.giphyapp

data class GiphyResponse(
    val data: List<GifData>
)

data class GifData(
    val id: String,
    val images: GifImages
)

data class GifImages(
    val fixed_height: GifImageUrl
)

data class GifImageUrl(
    val url: String
)