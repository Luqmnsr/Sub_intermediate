package com.example.storyapp.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.storyapp.data.api.remote.response.ListStoryItem
import com.example.storyapp.data.api.remote.response.StoryResponse
import com.example.storyapp.data.api.remote.retrofit.ApiService

class StoryPagingSource(
    private val apiService: ApiService,
) : PagingSource<Int, ListStoryItem>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val page = params.key ?: INITIAL_PAGE_INDEX
            val response: StoryResponse = apiService.getAllStories(page, params.loadSize)

            val stories = response.listStory

            LoadResult.Page(
                data = stories,
                prevKey = if (page == INITIAL_PAGE_INDEX) null else page - 1,
                nextKey = if (stories.isEmpty()) null else page + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}