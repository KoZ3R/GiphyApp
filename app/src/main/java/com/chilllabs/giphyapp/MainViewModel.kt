package com.chilllabs.giphyapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.URLEncoder

class MainViewModel : ViewModel() {
    private val _gifs = MutableLiveData<List<GiphyApiService.GifData>>()
    val gifs: LiveData<List<GiphyApiService.GifData>> = _gifs

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var currentQuery = ""
    private var currentOffset = 0
    private val limit = 50

    fun searchGifs(query: String, isNewSearch: Boolean = true) {
        if (query.isEmpty()) {
            println("Search query is empty, skipping")
            _error.postValue("Please enter a search query")
            return
        }

        if (isNewSearch) {
            println("Starting new search for query: $query")
            currentQuery = query
            currentOffset = 0
            _gifs.value = emptyList()
        } else {
            println("Loading more for query: $query, offset: $currentOffset")
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val encodedQuery = URLEncoder.encode(query, "UTF-8").replace("+", "%20")
                println("Full request URL: https://api.giphy.com/v1/gifs/search?q=$encodedQuery&api_key=${RetrofitClient.API_KEY}&limit=$limit&offset=$currentOffset&rating=g&lang=en")
                val response = RetrofitClient.giphyService.searchGifs(
                    query = encodedQuery,
                    offset = currentOffset,
                    limit = limit
                )
                println("Received response with ${response.data.size} GIFs, total_count: ${response.pagination.total_count}, offset: ${response.pagination.offset}")
                val currentList = _gifs.value.orEmpty().toMutableList()
                currentList.addAll(response.data)
                _gifs.postValue(currentList)
                currentOffset += limit
                println("Updated offset to: $currentOffset, total GIFs in list: ${currentList.size}")
                _isLoading.postValue(false)
                if (response.data.isEmpty() && currentOffset < response.pagination.total_count) {
                    println("No GIFs at offset $currentOffset, but total_count (${response.pagination.total_count}) not reached, retrying")
                    loadMoreGifs()
                } else if (response.data.isEmpty() && currentList.isEmpty()) {
                    println("No GIFs found for query: $query")
                    _error.postValue("No GIFs found for this query")
                }
            } catch (e: HttpException) {
                _isLoading.postValue(false)
                val errorBody = e.response()?.errorBody()?.string()
                println("HTTP Error: ${e.code()} - $errorBody")
                if (e.code() == 429) {
                    println("Rate limit exceeded, retrying after 5 seconds")
                    delay(5000)
                    searchGifs(query, isNewSearch)
                } else {
                    _error.postValue(when (e.code()) {
                        401 -> "Invalid API key. Check your key in Giphy Dashboard."
                        404 -> "Resource not found: $errorBody"
                        else -> "HTTP Error: ${e.code()} - ${e.message()}"
                    })
                }
            } catch (e: Exception) {
                _isLoading.postValue(false)
                println("API Error: ${e.message}")
                _error.postValue("Error: ${e.localizedMessage}")
            }
        }
    }

    fun loadMoreGifs() {
        if (_isLoading.value == true) {
            println("Already loading, skipping")
            return
        }
        searchGifs(currentQuery, isNewSearch = false)
    }
}