package com.example.storyefun.viewModel

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyefun.data.models.Book
import com.example.storyefun.data.models.Chapter
import com.example.storyefun.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChapterViewModel(
    private val repository: BookRepository,
    private val bookId: String,
    private val volumeId: String
) : ViewModel() {

    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book

    private val _title = mutableStateOf("")
    val title: String get() = _title.value

    private val _price = mutableStateOf(0)
    val price: Int get() = _price.value

    private val _chapterNumber = mutableStateOf(1)
    val chapterNumber: Int get() = _chapterNumber.value

    private val _imageUris = mutableStateOf<List<Uri>>(emptyList())
    val imageUris: List<Uri> get() = _imageUris.value

    private val _isUploading = mutableStateOf(false)
    val isUploading: Boolean get() = _isUploading.value

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    init {
        loadBook()
        loadChapters()
    }

    private fun loadBook() {
        viewModelScope.launch {
            _isLoading.value = true
            val book = repository.getBook(bookId)
            _book.value = book
            _isLoading.value = false
        }
    }

    fun loadChapters() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.loadChapters(bookId, volumeId) { chapters ->
                _chapters.value = chapters
                _chapterNumber.value = chapters.size + 1
                _title.value = "Chapter ${_chapterNumber.value}"
                _isLoading.value = false
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun updatePrice(newPrice: Int) {
        _price.value = newPrice
    }

    fun updateImageUris(uris: List<Uri>) {
        _imageUris.value = uris
    }

    fun uploadChapter(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isUploading.value = true
            val imageUrls = mutableListOf<String>()
            _imageUris.value.forEach { uri ->
                repository.uploadChapterImage(uri, "chapters") { url ->
                    imageUrls.add(url)
                    if (imageUrls.size == _imageUris.value.size) {
                        repository.addChapter(bookId, volumeId, _title.value, _price.value, imageUrls) {
                            _title.value = "Chapter ${_chapterNumber.value}"
                            _imageUris.value = emptyList()
                            _toastMessage.value = "Đã thêm chapter thành công"
                            onComplete()
                        }
                    }
                }
            }
            _isUploading.value = false
        }
    }

    fun uploadNovelChapter(content: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isUploading.value = true
            repository.addNovelChapter(bookId, volumeId, _title.value, _price.value, content) {
                _title.value = "Chapter ${_chapterNumber.value}"
                _toastMessage.value = "Đã thêm chapter tiểu thuyết thành công"
                loadChapters()
                onComplete()
            }
            _isUploading.value = false
        }
    }

    fun deleteChapter(chapterId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.deleteChapter(bookId, volumeId, chapterId)
            _isLoading.value = false

            if (success) {
                _toastMessage.value = "Đã xóa chapter thành công"
                loadChapters()
                onSuccess()
            } else {
                _toastMessage.value = "Lỗi khi xóa chapter"
            }
        }
    }

    fun setToastMessage(message: String) {
        _toastMessage.value = message
    }
}