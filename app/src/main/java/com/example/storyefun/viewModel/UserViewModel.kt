package com.example.storyefun.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.storyefun.data.models.User
import com.example.storyefun.data.repository.BookRepository
import com.example.storyefun.data.repository.CloudnaryRepository
import com.example.storyefun.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel() : ViewModel() {
    private val cloudnaryRepository: CloudnaryRepository = CloudnaryRepository()
    private val auth = FirebaseAuth.getInstance()
    private val bookRepository: BookRepository = BookRepository()
    // Update user's avatar (photoUrl) using Cloudinary
    fun updateAvatar(
        imageUri: Uri,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                cloudnaryRepository.uploadToCloudinary(
                    uri = imageUri,
                    folder = "user_avatars",
                    onSuccess = { secureUrl ->
                        // Launch coroutine here to call suspend function
                        viewModelScope.launch {
                            try {
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setPhotoUri(Uri.parse(secureUrl))
                                    .build()
                                auth.currentUser?.updateProfile(profileUpdates)?.await()
                                onSuccess()
                            } catch (e: Exception) {
                                onFailure(e)
                            }
                        }
                    },
                    onFailure = { e ->
                        onFailure(e)
                    }
                )
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    // Update user's display name
    fun updateUsername(newName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()
                auth.currentUser?.updateProfile(profileUpdates)?.await()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    // Update user's password
    fun updatePassword(newPassword: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                auth.currentUser?.updatePassword(newPassword)?.await()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    suspend fun isLikedBook(bookId: String): Boolean {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
            val snapshot = userRef.get().await()
            val likedBooks = snapshot.get("likedBooks") as? List<*>
            likedBooks?.contains(bookId) == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isFollowedBook(bookId: String): Boolean {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            val snapshot = userRef.get().await()
            val followedBooks = snapshot.get("followedBooks") as? List<*>
            followedBooks?.contains(bookId) == true

        } catch (e: Exception) {
            false
        }
    }

    suspend fun likingBook(bookId: String) {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
            userRef.update("likedBooks", FieldValue.arrayUnion(bookId)).await()
            bookRepository.valueIncrease("like", bookId)

        } catch (e: Exception) {
            Log.e("likingBook", "Error liking book: ${e.message}")
        }
    }

    suspend fun followingBook(bookId: String) {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
            userRef.update("followedBooks", FieldValue.arrayUnion(bookId)).await()
            bookRepository.valueIncrease("follow", bookId)
        } catch (e: Exception) {
            Log.e("followingBook", "Error following book: ${e.message}")
        }
    }

    suspend fun unlikingBook(bookId: String) {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            userRef.update("likedBooks", FieldValue.arrayRemove(bookId)).await()
            bookRepository.valueDecrease("like", bookId)
        } catch (e: Exception) {
            Log.e("unlikingBook", "Error removing like: ${e.message}")
        }
    }

    suspend fun unfollowingBook(bookId: String) {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            userRef.update("followedBooks", FieldValue.arrayRemove(bookId)).await()
            bookRepository.valueDecrease("follow", bookId)

          //  Log.d("unfollowingBook", "Successfully removed follow from $bookId")
        } catch (e: Exception) {
            Log.e("unfollowingBook", "Error removing follow: ${e.message}")
        }
    }

    suspend fun buyChapter(bookId: String, volumeOrder: String, chapterId: String)
    {

    }


    fun getBalance(): Long {
        val userId = auth.uid ?: return 0L

        return try {
            val snapshot = Firebase.firestore.collection("users").document(userId).get().result
            snapshot.getLong("coin") ?: 0L
        } catch (e: Exception) {
            0L // hoặc xử lý lỗi theo cách bạn muốn
        }
    }

    private val _usersList = MutableStateFlow<List<User>>(emptyList())
    val usersList: StateFlow<List<User>> = _usersList

    // StateFlow để thông báo trạng thái xóa
    private val _deleteStatus = MutableStateFlow<DeleteStatus>(DeleteStatus.Idle)
    val deleteStatus: StateFlow<DeleteStatus> = _deleteStatus

    // Sealed class để biểu thị trạng thái xóa
    sealed class DeleteStatus {
        object Idle : DeleteStatus()
        data class Success(val message: String) : DeleteStatus()
        data class Error(val message: String) : DeleteStatus()
    }

    fun getAllUsers() {
        Firebase.firestore.collection("users").get()
            .addOnSuccessListener { result ->
                val users = result.map { it.toObject(User::class.java).copy(uid = it.id) }
                _usersList.value = users
            }
            .addOnFailureListener { exception ->
                // Có thể thêm thông báo lỗi khi lấy danh sách
                println("Error fetching users: $exception")
            }
    }

    fun deleteUser(uid: String) {
        _deleteStatus.value = DeleteStatus.Idle // Reset trạng thái
        Firebase.firestore.collection("users").document(uid).delete()
            .addOnSuccessListener {
                // Xóa thành công
                _usersList.value = _usersList.value.filter { it.uid != uid }
                _deleteStatus.value = DeleteStatus.Success("Xóa người dùng thành công")
            }
            .addOnFailureListener { exception ->
                // Xóa thất bại
                _deleteStatus.value = DeleteStatus.Error("Lỗi khi xóa người dùng: ${exception.message}")
            }
    }

    // Reset trạng thái thông báo sau khi UI đã xử lý
    fun resetDeleteStatus() {
        _deleteStatus.value = DeleteStatus.Idle
    }


}