package com.goliath.emojihub.repositories.remote

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import com.goliath.emojihub.data_sources.api.EmojiApi
import com.goliath.emojihub.models.EmojiDto
import com.goliath.emojihub.models.FetchEmojiListDto
import com.goliath.emojihub.models.UploadEmojiDto
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface EmojiRepository {
    suspend fun fetchEmojiList(numLimit: Int): List<EmojiDto>
    suspend fun getEmojiWithId(id: String): EmojiDto?
    suspend fun uploadEmoji(videoFile: File, emojiDto: UploadEmojiDto): Boolean
    suspend fun saveEmoji(id: String): Response<Unit>
    suspend fun unSaveEmoji(id: String): Response<Unit>
    suspend fun deleteEmoji(id: String): Response<Unit>
}

@Singleton
class EmojiRepositoryImpl @Inject constructor(
    private val emojiApi: EmojiApi,
    @ApplicationContext private val context: Context
): EmojiRepository {
    override suspend fun fetchEmojiList(numLimit: Int): List<EmojiDto> {
        try {
            val response = emojiApi.fetchEmojiList(1, 1, 6)

            if(response.isSuccessful && response.body() != null) {
                Log.d("Fetch_E_L", "Successfully fetched ${response.body()!!.size} emojis")
                return response.body()!!
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.d("Fetch_E_L", "Failed to fetch emojis: $errorBody")
            }
        } catch(e: Exception) {
            Log.e("Fetch_E_L", "Error fetching emojis", e)
        }
        return listOf()
    }

    override suspend fun getEmojiWithId(id: String): EmojiDto? {
        TODO("Not yet implemented")
    }

    override suspend fun uploadEmoji(videoFile: File, emojiDto: UploadEmojiDto): Boolean {
        val emojiDtoJson = Gson().toJson(emojiDto)
        val emojiDtoRequestBody = RequestBody.create("application/json".toMediaTypeOrNull(), emojiDtoJson)

        val videoFileRequestBody = RequestBody.create("video/mp4".toMediaTypeOrNull(), videoFile)
        val videoFileMultipartBody = MultipartBody.Part.createFormData("file", videoFile.name, videoFileRequestBody)

        val thumbnailFile = createVideoThumbnail(context, videoFile)

        val thumbnailRequestBody = RequestBody.create("image/jpg".toMediaTypeOrNull(),
            thumbnailFile!!
        )
        val thumbnailMultipartBody = MultipartBody.Part.createFormData("thumbnail", thumbnailFile?.name, thumbnailRequestBody)

        return try {
            emojiApi.uploadEmoji(videoFileMultipartBody, thumbnailMultipartBody, emojiDtoRequestBody)
            true
        }
        catch (e: IOException) {
            Log.d("EmojiRepository", "IOException")
            e.printStackTrace()
            false
        }
        catch (e: HttpException) {
            Log.d("EmojiRepository", "HttpException")
            e.printStackTrace()
            false
        }
    }

    override suspend fun saveEmoji(id: String): Response<Unit> {
        return emojiApi.saveEmoji(id)
    }

    override suspend fun unSaveEmoji(id: String): Response<Unit> {
        return emojiApi.unSaveEmoji(id)
    }

    override suspend fun deleteEmoji(id: String): Response<Unit> {
        TODO("Not yet implemented")
    }

    private fun createVideoThumbnail(context: Context, videoFile: File): File? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(videoFile.absolutePath)
            val bitmap = retriever.frameAtTime

            bitmap?.let {
                // Create a temporary file to store the thumbnail
                val thumbnailFile = File(context.cacheDir, "thumbnail_${videoFile.name}.jpg")
                FileOutputStream(thumbnailFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out) // Compress and write to file
                }
                Log.d("create_TN", "Thumbnail created: ${thumbnailFile.absolutePath}")
                return thumbnailFile
            }
        } catch (e: Exception) {
            Log.d("create_TN", "ERROR...")
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return null
    }
}