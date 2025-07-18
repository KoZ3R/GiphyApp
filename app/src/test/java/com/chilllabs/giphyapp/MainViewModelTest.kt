package com.chilllabs.giphyapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    private lateinit var giphyApiService: GiphyApiService

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = MainViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `searchGifs with valid query returns gifs`() = runBlockingTest {
        val mockResponse = GiphyApiService.GiphyResponse(
            listOf(
                GiphyApiService.GifData(
                    id = "1",
                    title = "Test GIF",
                    images = GiphyApiService.Images(
                        fixedHeight = GiphyApiService.FixedHeight(url = "https://example.com/gif1.gif")
                    )
                )
            )
        )
        `when`(giphyApiService.searchGifs(query = "test", offset = 0)).thenReturn(mockResponse)

        viewModel.searchGifs("test")

        assertEquals(1, viewModel.gifs.value?.size)
        assertEquals("Test GIF", viewModel.gifs.value?.first()?.title)
        assertFalse(viewModel.isLoading.value!!)
    }

    @Test
    fun `searchGifs with empty query does nothing`() = runBlockingTest {
        viewModel.searchGifs("")
        assertTrue(viewModel.gifs.value.isNullOrEmpty())
        assertFalse(viewModel.isLoading.value!!)
    }
}