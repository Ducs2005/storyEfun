package com.example.storyefun.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyefun.data.models.Book
import com.example.storyefun.data.models.Chapter
import com.example.storyefun.data.repository.BookRepository
import com.example.storyefun.utils.downloadAndExtractDocx
import com.example.storyefun.utils.downloadTextFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BookViewModel : ViewModel() {
    private val bookRepository: BookRepository = BookRepository()
    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> get() = _books
    private val _book = MutableLiveData<Book?>()
    val book: LiveData<Book?> get() = _book

    private val _favoriteBooks = MutableStateFlow<List<Book>>(emptyList())
    val favoriteBooks: StateFlow<List<Book>> = _favoriteBooks

    val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _fontSize = mutableStateOf(16f)
    val fontSize: State<Float> = _fontSize

    private val _lineSpacing = mutableStateOf(1f)
    val lineSpacing: State<Float> = _lineSpacing

    private val _unlockedChapterIds = MutableStateFlow<List<String>>(emptyList())
    val unlockedChapterIds: StateFlow<List<String>> = _unlockedChapterIds

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentParagraphIndex = MutableLiveData<Int>(0)
    val currentParagraphIndex: LiveData<Int> = _currentParagraphIndex

    init {
        viewModelScope.launch {
            setState(true)
            loadUnlockedChapterIds()
            _books.value = bookRepository.getBooks()
            // Load favorites for favorite screen
            loadFavorites()
            // Load unlocked chapter IDs
            setState(false)
        }
    }

    private suspend fun loadUnlockedChapterIds() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        try {
            val document = Firebase.firestore.collection("users")
                .document(userId)
                .get()
                .await()
            if (document.exists()) {
                val unlockedChapterIds = document.get("unlockedChapterIds") as? List<String> ?: emptyList()
                _unlockedChapterIds.value = unlockedChapterIds
            } else {
                Log.w("BookViewModel", "User document not found for userId: $userId")
                _unlockedChapterIds.value = emptyList()
            }
        } catch (e: Exception) {
            Log.e("BookViewModel", "Failed to load unlocked chapter IDs: ${e.message}")
            _unlockedChapterIds.value = emptyList()
        }
    }

    fun fetchBook(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = bookRepository.getBook(bookId)

            // Debugging log to check if result has comments
            if (result != null) {
                Log.d("info of loaded book comment : ", result.comments.toString())
            } else {
                Log.d("info", "No book found for ID: $bookId")
            }

            _book.value = result // Set the book after fetching comments
            _isLoading.value = false
        }
    }

    fun addBook(bookId: String) {
        // TODO: Implement addBook logic
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            _isLoading.value = true
            bookRepository.updateBook(book)
            _books.value = bookRepository.getBooks()
            _isLoading.value = false
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            bookRepository.deleteBook(bookId)
            _books.value = bookRepository.getBooks() // Reload books after deletion
            _isLoading.value = false
        }
    }

    fun setState(state: Boolean) {
        _isLoading.value = state
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size
    }

    fun setLineSpacing(size: Float) {
        _lineSpacing.value = size
    }

    // Helper function to get chapter content
    fun getChapterContent(volumeOrder: Long, chapterOrder: Long): Chapter? {
        val currentBook = _book.value ?: return null
        val volume = currentBook.volume.find { it.order == volumeOrder }
        return volume?.chapters?.find { it.order == chapterOrder }
    }

    // Helper function to get previous chapter
    fun getPreviousChapter(volumeOrder: Long, chapterOrder: Long): Triple<Boolean, Long?, Long?> {
        val currentBook = _book.value ?: return Triple(false, null, null)

        // Sort volumes by order to ensure consistent order
        val sortedVolumes = currentBook.volume.sortedBy { it.order }
        val volumeIndex = sortedVolumes.indexOfFirst { it.order == volumeOrder }
        if (volumeIndex == -1) return Triple(false, null, null)

        val volume = sortedVolumes[volumeIndex]
        // Sort chapters by order to ensure consistent order
        val sortedChapters = volume.chapters.sortedBy { it.order }
        val chapterIndex = sortedChapters.indexOfFirst { it.order == chapterOrder }
        if (chapterIndex == -1) return Triple(false, null, null)

        // If there's a previous chapter in the same volume
        if (chapterIndex > 0) {
            return Triple(true, volumeOrder, sortedChapters[chapterIndex - 1].order)
        }

        // If we're at the first chapter of the volume, go to the last chapter of the previous volume
        if (volumeIndex > 0) {
            val prevVolume = sortedVolumes[volumeIndex - 1]
            val sortedPrevChapters = prevVolume.chapters.sortedBy { it.order }
            val lastChapter = sortedPrevChapters.lastOrNull()
            return if (lastChapter != null) {
                Triple(true, prevVolume.order, lastChapter.order)
            } else {
                Triple(false, null, null)
            }
        }

        return Triple(false, null, null)
    }

    // Helper function to get next chapter
    fun getNextChapter(volumeOrder: Long, chapterOrder: Long): Triple<Boolean, Long?, Long?> {
        val currentBook = _book.value ?: return Triple(false, null, null)

        // Sort volumes by order to ensure consistent order
        val sortedVolumes = currentBook.volume.sortedBy { it.order }
        val volumeIndex = sortedVolumes.indexOfFirst { it.order == volumeOrder }
        if (volumeIndex == -1) return Triple(false, null, null)

        val volume = sortedVolumes[volumeIndex]
        // Sort chapters by order to ensure consistent order
        val sortedChapters = volume.chapters.sortedBy { it.order }
        val chapterIndex = sortedChapters.indexOfFirst { it.order == chapterOrder }
        if (chapterIndex == -1) return Triple(false, null, null)

        // If there's a next chapter in the same volume
        if (chapterIndex < sortedChapters.size - 1) {
            return Triple(true, volumeOrder, sortedChapters[chapterIndex + 1].order)
        }

        // If we're at the last chapter of the volume, go to the first chapter of the next volume
        if (volumeIndex < sortedVolumes.size - 1) {
            val nextVolume = sortedVolumes[volumeIndex + 1]
            val sortedNextChapters = nextVolume.chapters.sortedBy { it.order }
            val firstChapter = sortedNextChapters.firstOrNull()
            return if (firstChapter != null) {
                Triple(true, nextVolume.order, firstChapter.order)
            } else {
                Triple(false, null, null)
            }
        }

        return Triple(false, null, null)
    }

    suspend fun loadFavorites() {
        // TODO: Load from Firebase or local
        _favoriteBooks.value = bookRepository.favoriteBooks() // Replace with real fetch
    }

    /**
     * Checks if a chapter is unlocked for the current user.
     * @param chapterId The ID of the chapter to check.
     * @return True if the chapter is unlocked, false otherwise.
     */
    fun isChapterUnlocked(chapterId: String): Boolean {
        return chapterId in _unlockedChapterIds.value
    }

    /**
     * Refreshes the list of unlocked chapter IDs from Firestore.
     */
    fun refreshUnlockedChapterIds() {
        viewModelScope.launch {
            loadUnlockedChapterIds()
        }
    }

    suspend  fun getParagraphs(fileUrls: List<String>): List<String> {
        // Process only the first file for simplicity
        val url = fileUrls.firstOrNull() ?: return emptyList()
        return try {
            if (url.endsWith(".txt")) {
                downloadTextFile(url).split("\n\n").filter { it.isNotBlank() }
            } else if (url.endsWith(".docx")) {
                downloadAndExtractDocx(url).split("\n\n").filter { it.isNotBlank() }
            } else {
                listOf(url)
            }
        } catch (e: Exception) {
            Log.e("BookViewModel", "Failed to load paragraphs: ${e.message}")
            listOf("Lỗi tải file: ${e.message}")
        }
    }


    fun setIsPlaying(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    fun updateParagraphIndex(index: Int) {
        _currentParagraphIndex.value = index
    }

}