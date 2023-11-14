package com.goliath.emojihub.repositories.remote

import android.util.Log
import com.goliath.emojihub.data_sources.api.PostApi
import com.goliath.emojihub.models.PostDto
import com.goliath.emojihub.models.UploadPostDto
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

interface PostRepository {
    suspend fun fetchPostList(numLimit: Int): List<PostDto>
    suspend fun uploadPost(dto: UploadPostDto): Response<Unit>
    suspend fun getPostWithId(id: String): PostDto?
    suspend fun editPost(id: String, content: String)
    suspend fun deletePost(id: String)
}

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postApi: PostApi
): PostRepository {
    override suspend fun fetchPostList(numLimit: Int): List<PostDto> {
        try {
            val response = postApi.fetchPostList(numLimit)

            if (response.isSuccessful && response.body() != null) {
                // Log success and the size of the fetched list
                Log.d("PostRepository", "Successfully fetched ${response.body()!!.size} posts")
                return response.body()!!
            } else {
                // Log failure with the response error body or a default message
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.d("PostRepository", "Failed to fetch posts: $errorBody")
            }
        } catch (e: Exception) {
            // Log exception
            Log.e("PostRepository", "Error fetching posts", e)
        }
        return listOf()
    }

    override suspend fun uploadPost(dto: UploadPostDto): Response<Unit> {
        return postApi.uploadPost(dto)
    }

    override suspend fun getPostWithId(id: String): PostDto? {
        val result = postApi.getPostWithId(id)
        if (result.isSuccessful) {
            Log.d("Search Post Success", result.body().toString())
            return result.body()
        } else {
            Log.d("Search Post Failure", result.raw().toString())
        }
        return null
    }

    override suspend fun editPost(id: String, content: String) {
        val dto = UploadPostDto(content)
        postApi.editPost(id, dto)
    }

    override suspend fun deletePost(id: String) {
        postApi.deletePost(id)
    }
}