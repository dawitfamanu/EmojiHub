package com.goliath.emojihub.viewmodels

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.goliath.emojihub.models.CreatedEmoji
import com.goliath.emojihub.models.Emoji
import com.goliath.emojihub.usecases.EmojiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EmojiViewModel @Inject constructor(
    private val emojiUseCase: EmojiUseCase
): ViewModel() {
    var videoUri: Uri = Uri.EMPTY
    var currentEmoji: Emoji? = null
    var isBottomSheetShown by mutableStateOf(false)

    val emojiList = emojiUseCase.emojiList

    private val _topK = 3

    fun fetchEmojiList()
    {
        viewModelScope.launch {
            emojiUseCase.fetchEmojiList()
                .cachedIn(viewModelScope)
                .collect {
                    emojiUseCase.updateEmojiList(it)
                }
        }
    }

    suspend fun createEmoji(videoUri: Uri): List<CreatedEmoji> {
        return withContext(Dispatchers.IO) {
            val createdEmojiList = emojiUseCase.createEmoji(videoUri, _topK)
            Log.d("EmojiViewModel", "Done create emoji: $createdEmojiList")
            createdEmojiList
        }
    }

    suspend fun uploadEmoji(emojiUnicode: String, emojiLabel: String, videoFile: File): Boolean {
        return emojiUseCase.uploadEmoji(emojiUnicode, emojiLabel, videoFile)
    }

    suspend fun saveEmoji(id: String) {
        emojiUseCase.saveEmoji(id)
    }

    suspend fun unSaveEmoji(id: String) {
        emojiUseCase.saveEmoji(id)
    }
}