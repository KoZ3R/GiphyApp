package com.chilllabs.giphyapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import java.net.HttpURLConnection
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: MainViewModel
    private lateinit var giphyApiService: GiphyApiService
    private lateinit var gifsObserver: Observer<List<GiphyApiService.GifData>>
    private lateinit var isLoadingObserver: Observer<Boolean>
    private lateinit var errorObserver: Observer<String>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        giphyApiService = mockk()
        viewModel = MainViewModel()
        gifsObserver = mockk(relaxed = true)
        isLoadingObserver = mockk(relaxed = true)
        errorObserver = mockk(relaxed = true)
        viewModel.gifs.observeForever(gifsObserver)
        viewModel.isLoading.observeForever(isLoadingObserver)
        viewModel.error.observeForever(errorObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        viewModel.gifs.removeObserver(gifsObserver)
        viewModel.isLoading.removeObserver(isLoadingObserver)
        viewModel.error.removeObserver(errorObserver)
    }

    @Test
    fun `searchGifs with valid query emits gifs`() = runTest {
        // Arrange
        val query = "cats"
        val gifData = listOf(
            GiphyApiService.GifData(
                id = "1",
                title = "Cat GIF",
                images = GiphyApiService.Images(
                    fixed_height = GiphyApiService.Image(url = "https://giphy.com/cat1.gif")
                )
            )
        )
        val response = GiphyApiService.GifResponse(
            data = gifData,
            pagination = GiphyApiService.Pagination(total_count = 100, count = 20, offset = 0)
        )
        coEvery { giphyApiService.searchGifs(query = any(), offset = any()) } returns response

        // Act
        viewModel.searchGifs(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify { isLoadingObserver.onChanged(true) }
        coVerify { gifsObserver.onChanged(gifData) }
        coVerify { isLoadingObserver.onChanged(false) }
        assertEquals(gifData, viewModel.gifs.value)
        assertFalse(viewModel.isLoading.value!!)
    }

    @Test
    fun `searchGifs with empty query does nothing`() = runTest {
        // Arrange
        val query = ""

        // Act
        viewModel.searchGifs(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify(exactly = 0) { giphyApiService.searchGifs(any(), any()) }
        coVerify(exactly = 0) { gifsObserver.onChanged(any()) }
        coVerify(exactly = 0) { isLoadingObserver.onChanged(any()) }
        assertEquals(emptyList(), viewModel.gifs.value)
    }

    @Test
    fun `searchGifs with HTTP 401 error emits error`() = runTest {
        // Arrange
        val query = "cats"
        val httpException = mockk<HttpException> {
            coEvery { code() } returns HttpURLConnection.HTTP_UNAUTHORIZED
            coEvery { response()?.errorBody()?.string() } returns "Invalid API key"
        }
        coEvery { giphyApiService.searchGifs(query = any(), offset = any()) } throws httpException

        // Act
        viewModel.searchGifs(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify { isLoadingObserver.onChanged(true) }
        coVerify { errorObserver.onChanged("Недействительный API-ключ. Проверьте ключ в Giphy Dashboard.") }
        coVerify { isLoadingObserver.onChanged(false) }
        assertTrue(viewModel.gifs.value.isNullOrEmpty())
        assertFalse(viewModel.isLoading.value!!)
    }

    @Test
    fun `loadMoreGifs appends new gifs`() = runTest {
        // Arrange
        val query = "cats"
        val initialGifs = listOf(
            GiphyApiService.GifData(
                id = "1",
                title = "Cat GIF 1",
                images = GiphyApiService.Images(
                    fixed_height = GiphyApiService.Image(url = "https://giphy.com/cat1.gif")
                )
            )
        )
        val moreGifs = listOf(
            GiphyApiService.GifData(
                id = "2",
                title = "Cat GIF 2",
                images = GiphyApiService.Images(
                    fixed_height = GiphyApiService.Image(url = "https://giphy.com/cat2.gif")
                )
            )
        )
        val initialResponse = GiphyApiService.GifResponse(
            data = initialGifs,
            pagination = GiphyApiService.Pagination(total_count = 100, count = 20, offset = 0)
        )
        val moreResponse = GiphyApiService.GifResponse(
            data = moreGifs,
            pagination = GiphyApiService.Pagination(total_count = 100, count = 20, offset = 20)
        )
        coEvery { giphyApiService.searchGifs(query = any(), offset = 0) } returns initialResponse
        coEvery { giphyApiService.searchGifs(query = any(), offset = 20) } returns moreResponse

        // Act
        viewModel.searchGifs(query)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.loadMoreGifs()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify { gifsObserver.onChanged(initialGifs) }
        coVerify { gifsObserver.onChanged(initialGifs + moreGifs) }
        assertEquals(initialGifs + moreGifs, viewModel.gifs.value)
        assertFalse(viewModel.isLoading.value!!)
    }

    @Test
    fun `loadMoreGifs with empty response emits error`() = runTest {
        // Arrange
        val query = "cats"
        val initialGifs = listOf(
            GiphyApiService.GifData(
                id = "1",
                title = "Cat GIF 1",
                images = GiphyApiService.Images(
                    fixed_height = GiphyApiService.Image(url = "https://giphy.com/cat1.gif")
                )
            )
        )
        val initialResponse = GiphyApiService.GifResponse(
            data = initialGifs,
            pagination = GiphyApiService.Pagination(total_count = 1, count = 1, offset = 0)
        )
        val emptyResponse = GiphyApiService.GifResponse(
            data = emptyList(),
            pagination = GiphyApiService.Pagination(total_count = 1, count = 0, offset = 1)
        )
        coEvery { giphyApiService.searchGifs(query = any(), offset = 0) } returns initialResponse
        coEvery { giphyApiService.searchGifs(query = any(), offset = 1) } returns emptyResponse

        // Act
        viewModel.searchGifs(query)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.loadMoreGifs()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify { gifsObserver.onChanged(initialGifs) }
        coVerify { errorObserver.onChanged("No more GIFs available for this query") }
        assertEquals(initialGifs, viewModel.gifs.value)
        assertFalse(viewModel.isLoading.value!!)
    }
}