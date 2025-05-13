package com.example.storyefun.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyefun.data.models.Volume
import com.example.storyefun.data.repository.BookRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VolumeViewModel(
    private val repository: BookRepository = BookRepository()  // inject hoặc truyền qua constructor sau
) : ViewModel() {

    private val _volumes = MutableStateFlow<List<Volume>>(emptyList())
    val volumes: StateFlow<List<Volume>> = _volumes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    fun fetchNextVolumeOrder(bookId: String, callback: (Long) -> Unit) {
        viewModelScope.launch {
            val order = repository.getNextVolumeOrder(bookId)
            callback(order)
        }
    }

    fun addVolume(bookId: String, volume: Volume, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val resultId = repository.addVolume(bookId, volume)
            _isLoading.value = false

            if (resultId != null) {
                _toastMessage.value = "Đã thêm volume thành công"
                onSuccess(resultId)
            } else {
                _toastMessage.value = "Lỗi khi thêm volume"
            }
        }
    }

    fun loadVolumes(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _volumes.value = repository.loadVolumes(bookId)
            _isLoading.value = false
        }
    }

    fun setToastMessage(message: String) {
        _toastMessage.value = message
    }
    fun deleteVolume(bookId: String, volumeId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.deleteVolume(bookId, volumeId)
            _isLoading.value = false

            if (success) {
                _toastMessage.value = "Đã xóa volume thành công"
                // Cập nhật lại danh sách volumes sau khi xóa
                _volumes.value = repository.loadVolumes(bookId)
                onSuccess()
            } else {
                _toastMessage.value = "Lỗi khi xóa volume"
            }
        }
    }
}

