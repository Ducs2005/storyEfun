package com.example.storyefun.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyefun.data.repository.QuestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.example.storyefun.data.models.Quest

class QuestViewModel(
    private val repository: QuestRepository,
    private val userId: String
) : ViewModel() {

    private val _quests = MutableStateFlow<List<Quest>>(emptyList())
    val quests: StateFlow<List<Quest>> = _quests

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    init {
        viewModelScope.launch {
            repository.initializeQuests(userId)
            loadQuests()
        }
    }

    fun loadQuests() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.resetDailyQuests(userId)
                val quests = repository.getQuests(userId)
                _quests.value = quests
            } catch (e: Exception) {
                Log.e("QuestViewModel", "loadQuests error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun completeQuest(questId: String, reward: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.completeQuest(userId, questId, reward)
            _isLoading.value = false
            if (success) {
                _toastMessage.value = "Nhận ${reward} coin thành công!"
                loadQuests()  // Tải lại danh sách nhiệm vụ
            } else {
                _toastMessage.value = "Lỗi khi nhận thưởng"
            }
        }
    }


    fun updateQuestProgress(questId: String, progress: Long) {
        viewModelScope.launch {
            try {
                repository.updateQuestProgress(userId, questId, progress)
                // Không gọi loadQuests() nữa để tránh tốn tài nguyên
            } catch (e: Exception) {
                Log.e("QuestViewModel", "updateQuestProgress error: ${e.message}", e)
            }
        }
    }

    fun setToastMessage(message: String) {
        _toastMessage.value = message
    }
}
