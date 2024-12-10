package com.example.storyapp.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.storyapp.DataDummy
import com.example.storyapp.MainDispatcherRule
import com.example.storyapp.data.api.remote.response.ListStoryItem
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.results.Result
import com.example.storyapp.getOrAwaitValue
import com.example.storyapp.view.adapter.StoryAdapter
import com.example.storyapp.view.ui.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var userRepository: UserRepository

    @Test
    fun `when Get Story Should Not Be Null and Return Data`() = runTest {
        val dummyStories = DataDummy.generateDummyStoryResponse()
        val data: PagingData<ListStoryItem> = StoryPagingSource.snapshot(dummyStories)
        val expectedStories = MutableLiveData<Result<PagingData<ListStoryItem>>>()
        expectedStories.value = Result.Success(data)

        Mockito.`when`(userRepository.getAllStories()).thenReturn(expectedStories)

        val mainViewModel = MainViewModel(userRepository)
        val actualResult: Result<PagingData<ListStoryItem>> = mainViewModel.getAllStories().getOrAwaitValue()

        if (actualResult is Result.Success) {
            val actualStories = actualResult.data

            val differ = AsyncPagingDataDiffer(
                diffCallback = StoryAdapter.DIFF_CALLBACK,
                updateCallback = noopListUpdateCallback,
                workerDispatcher = Dispatchers.Main,
            )
            differ.submitData(actualStories)

            assertNotNull(differ.snapshot())
            assertEquals(dummyStories.size, differ.snapshot().size)
            assertEquals(dummyStories[0], differ.snapshot()[0])
        }
    }

    @Test
    fun `when Get Story List is Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())
        val expectedStories = MutableLiveData<Result<PagingData<ListStoryItem>>>()
        expectedStories.value = Result.Success(data)

        Mockito.`when`(userRepository.getAllStories()).thenReturn(expectedStories)

        val mainViewModel = MainViewModel(userRepository)
        val actualResult: Result<PagingData<ListStoryItem>> = mainViewModel.getAllStories().getOrAwaitValue()

        if (actualResult is Result.Success) {
            val actualStories = actualResult.data

            val differ = AsyncPagingDataDiffer(
                diffCallback = StoryAdapter.DIFF_CALLBACK,
                updateCallback = noopListUpdateCallback,
                workerDispatcher = Dispatchers.Main,
            )
            differ.submitData(actualStories)

            assertEquals(0, differ.snapshot().size)
        }
    }
}

class StoryPagingSource : PagingSource<Int, ListStoryItem>() {
    companion object {
        fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
            return PagingData.from(items)
        }
    }
    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? = null
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
