package com.chilllabs.giphyapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val limit = 20

    fun searchGifs(query: String, isNewSearch: Boolean = true) {
        if (query.isEmpty()) {
            println("Search query is empty, skipping")
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
                    offset = currentOffset
                )
                println("Received response with ${response.data.size} GIFs, total: ${response.pagination.total_count}")
                if (response.data.isNotEmpty()) {
                    val currentList = _gifs.value.orEmpty().toMutableList()
                    currentList.addAll(response.data)
                    _gifs.value = currentList
                    currentOffset += response.data.size
                    println("Updated offset to: $currentOffset")
                } else {
                    println("No more GIFs available for query: $query")
                    _error.value = "No more GIFs available for this query"
                }
                _isLoading.value = false
            } catch (e: HttpException) {
                _isLoading.value = false
                val errorBody = e.response()?.errorBody()?.string()
                println("HTTP Error: ${e.code()} - $errorBody")
                _error.value = when (e.code()) {
                    401 -> "Недействительный API-ключ. Проверьте ключ в Giphy Dashboard."
                    404 -> "Ресурс не найден: $errorBody"
                    else -> "HTTP Ошибка: ${e.code()} - ${e.message()}"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                println("API Error: ${e.message}")
                _error.value = "Ошибка: ${e.localizedMessage}"
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