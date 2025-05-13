package com.example.storyefun.data.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.storyefun.data.models.Book
import com.example.storyefun.data.models.Category
import com.example.storyefun.data.models.Chapter
import com.example.storyefun.data.models.Comment
import com.example.storyefun.data.models.Volume
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resumeWithException

class BookRepository {
    private val db = Firebase.firestore
    val isLoading = MutableLiveData(false)
// BOOK
    suspend fun getBooks() : List<Book>{
        return try {
            val snapshot = db.collection("books").get().await()
            snapshot.documents.mapNotNull { it.toObject(Book::class.java) }
        } catch (e : Exception)
        {
            Log.e("error when getting books","Error when getting book:   ${e.message}")
            emptyList()
        }

    }
    suspend fun getBook(bookId : String) : Book? {
        try {
            val bookRef = db.collection("books").document(bookId)
            val book = bookRef.get().await()
            val result = book.toObject(Book::class.java)
            if (result != null)
            {
                val volumesSnapshot = bookRef.collection("volumes").get().await()
                val volumeList = mutableListOf<Volume>()

                for (volumeDoc in volumesSnapshot.documents) {
                    var volume = volumeDoc.toObject(Volume::class.java)
                    if (volume != null) {
                        // Fetch chapters for this volume
                        val chaptersSnapshot = volumeDoc.reference.collection("chapters").get().await()
                        val chapterList = chaptersSnapshot.mapNotNull { doc ->
                            val chapter = doc.toObject(Chapter::class.java)
                            chapter?.apply {
                                id = doc.id // <- manually assign document ID
                            }
                        }
                        volume.chapters = chapterList
                        volumeList.add(volume)
                    }
                }
                result.volume = volumeList

                // 2. Get full category objects from IDs
                val categoryIds = result.categoryIDs ?: emptyList()
                val categoryList = mutableListOf<Category>()
                for (categoryId in categoryIds) {
                    val catSnapshot = db.collection("categories").document(categoryId).get().await()
                    catSnapshot.toObject(Category::class.java)?.let { categoryList.add(it) }
                }
                result.category = categoryList // <- Gắn vào một field mới

                // 3. Get comments for the book
                val commentsSnapshot = bookRef.collection("comments").get().await()
                val commentList = commentsSnapshot.documents.mapNotNull {
                    it.toObject(Comment::class.java)
                }

                //result.comments = commentList.toMutableList() // Ensure this is a mutable list

            }
            if (result != null) {
                Log.d("comment list  ", result.comments.toString())
            }

            return result

        }
        catch (e : Exception)
        {
            Log.e("Error", e.message.toString())
            return null
        }
    }

    fun getBookByUser(): LiveData<List<Book>> {
        val liveData = MutableLiveData<List<Book>>()

        db.collection("books")
            .get()
            .addOnSuccessListener { result ->
                val books = result.documents.mapNotNull { document ->
                    val name = document.getString("name")
                    val posterUrl = document.getString("posterUrl") // hoặc "imageUrl"

                    if (name != null && posterUrl != null) {
                        Book(name, posterUrl)
                    } else null // bỏ qua nếu thiếu dữ liệu
                }
                liveData.postValue(books)
            }
            .addOnFailureListener { exception ->
                Log.e("BookRepository", "Error fetching books", exception)
                liveData.postValue(emptyList())
            }

        return liveData
    }




    suspend fun addBook(book: Book): Boolean {
        return try {
            db.collection("books").add(book).await()  // Thêm sách vào Firestore
            true
        } catch (e: Exception) {
            println("Error when adding book: ${e.message}")
            false
        }
    }
    suspend fun deleteBook (bookId : String) : Boolean
    {
        try {
            db.collection("books").document(bookId).delete().await()
            return true
        }
        catch (e : Exception)
        {
            println("Error when deleting book ${e.message}")

        }
        return false

    }
    suspend fun updateBook(book: Book): Boolean {
        return try {
            db.collection("books")
                .document(book.id)  // Use book's ID to identify the document
                .set(book)  // Replace the entire document with the new data
                .await()
            true
        } catch (e: Exception) {
            println("Error when updating book: ${e.message}")
            false
        }
    }

// VOLUME
    suspend fun getNextVolumeOrder(bookId: String): Long {
        return try {
            val snapshot = db.collection("books")
                .document(bookId)
                .collection("volumes")
                .orderBy("order")
                .get()
                .await()

            val currentMaxOrder = snapshot.documents.mapNotNull {
                it.getLong("order")
            }.maxOrNull() ?: 0L

            currentMaxOrder + 1
        } catch (e: Exception) {
            Log.e("VolumeRepo", "getNextVolumeOrder error: ${e.message}")
            1L
        }
    }

    suspend fun addVolume(bookId: String, volume: Volume): String? {
        return try {
            val volumeData = mapOf(
                "title" to volume.title,
                "order" to volume.order,
                "createdAt" to FieldValue.serverTimestamp()
            )

            val docRef = db.collection("books")
                .document(bookId)
                .collection("volumes")
                .add(volumeData)
                .await()

            docRef.id
        } catch (e: Exception) {
            Log.e("VolumeRepo", "addVolume error: ${e.message}")
            null
        }
    }

    suspend fun loadVolumes(bookId: String): List<Volume> {
        return try {
            val snapshot = db.collection("books")
                .document(bookId)
                .collection("volumes")
                .orderBy("order")
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(Volume::class.java)?.copy(id = it.id)
            }
        } catch (e: Exception) {
            Log.e("VolumeRepo", "loadVolumes error: ${e.message}")
            emptyList()
        }
    }
    suspend fun deleteVolume(bookId: String, volumeId: String): Boolean {
        return try {
            db.collection("books")
                .document(bookId)
                .collection("volumes")
                .document(volumeId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e("VolumeRepo", "deleteVolume error: ${e.message}")
            false
        }
    }

// CHAPTER
suspend fun getNextChapterOrder(bookId: String, volumeID: String): Long {
    return try {
        val snapshot = db.collection("books")
            .document(bookId)
            .collection("volumes")
            .document(volumeID)
            .collection("chapters")
            .orderBy("order")
            .get()
            .await()

        val currentMaxOrder = snapshot.documents.mapNotNull {
            it.getLong("order")
        }.maxOrNull() ?: 0L

        currentMaxOrder + 1
    } catch (e: Exception) {
        Log.e("ChapterRepo", "getNextChapterOrder error: ${e.message}")
        1L
    }
}

    fun loadChapters(
        bookId: String,
        volumeId: String,
        onChaptersLoaded: (List<Chapter>) -> Unit
    ) {
        db.collection("books")
            .document(bookId)
            .collection("volumes")
            .document(volumeId)
            .collection("chapters")
            .orderBy("order")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val chapters = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        Chapter(
                            id = doc.id,
                            title = data["title"] as? String ?: "",
                            content = (data["content"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            order = data["order"] as? Long ?: 0L,
                            price = data["price"] as? Int ?: 0
                        )
                    }
                    onChaptersLoaded(chapters)
                }
            }
    }
    suspend fun getChapters(bookId: String, volumeId: String): List<Chapter> {
        return try {
            val chaptersSnapshot = db.collection("books")
                .document(bookId)
                .collection("volumes")
                .document(volumeId)
                .collection("chapters")
                .get()
                .await()

            // Chuyển dữ liệu từ Firestore thành danh sách các Chapter
            chaptersSnapshot.documents.map { doc ->
                doc.toObject(Chapter::class.java) ?: Chapter()
            }
        } catch (e: Exception) {
            emptyList() // Nếu có lỗi, trả về danh sách trống
        }
    }
    fun addChapter(
        bookId: String,
        volumeId: String,
        title: String,
        price: Int,
        imageUrls: List<String>,
        onComplete: () -> Unit
    ) {
        val chaptersRef = db.collection("books")
            .document(bookId)
            .collection("volumes").document(volumeId)
            .collection("chapters")

        chaptersRef.orderBy("order", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                // Lấy order lớn nhất hiện có, nếu không có thì dùng 1
                val maxOrder = snapshot.documents.firstOrNull()?.getLong("order") ?: 0L
                val newOrder = maxOrder + 1

                val chapterData = hashMapOf(
                    "title" to title,
                    "content" to imageUrls,
                    "order" to newOrder,
                    "price" to price,
                    "createdAt" to System.currentTimeMillis(),
                    "locked" to false
                )

                chaptersRef.add(chapterData)
                    .addOnSuccessListener {
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        println("Error adding chapter: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                println("Error getting max order: ${e.message}")
            }
    }

    fun uploadChapterImage(uri: Uri, folder: String, onSuccess: (String) -> Unit) {
        MediaManager.get().upload(uri)
            .option("folder", folder)
            .callback(object : com.cloudinary.android.callback.UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        onSuccess(secureUrl)
                    } else {
                        println("Upload error: Secure URL is null")
                    }
                }

                override fun onError(requestId: String, error: com.cloudinary.android.callback.ErrorInfo) {
                    println("Upload error: ${error.description}")
                }

                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String, error: com.cloudinary.android.callback.ErrorInfo) {}
            })
            .dispatch()
    }
    suspend fun deleteChapter(bookId: String, volumeId: String, chapterId: String): Boolean {
        return try {
            db.collection("books")
                .document(bookId)
                .collection("volumes")
                .document(volumeId)
                .collection("chapters")
                .document(chapterId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e("ChapterRepo", "deleteChapter error: ${e.message}")
            false
        }
    }

    suspend fun valueIncrease(type: String, bookId: String) {
        try {
            val bookRef = db.collection("books").document(bookId)

            val field = when (type.lowercase()) {
                "like" -> "likes"
                "follow" -> "follows"
                else -> return // invalid type
            }

            bookRef.update(field, FieldValue.increment(1)).await()
            Log.d("valueIncrease", "Increased $field for book $bookId")
        } catch (e: Exception) {
            Log.e("valueIncrease", "Error increasing value: ${e.message}")
        }
    }

    suspend fun valueDecrease(type: String, bookId: String) {
        try {
            val bookRef = db.collection("books").document(bookId)

            val field = when (type.lowercase()) {
                "like" -> "likes"
                "follow" -> "follows"
                else -> return // invalid type
            }

            bookRef.update(field, FieldValue.increment(-1)).await()
            Log.d("valueDecrease", "Decreased $field for book $bookId")
        } catch (e: Exception) {
            Log.e("valueDecrease", "Error decreasing value: ${e.message}")
        }
    }

    suspend fun favoriteBooks(): List<Book> {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid ?: return emptyList()
        val userDoc = firestore.collection("users").document(userId).get().await()
        val likedBookIds = userDoc.get("likedBooks") as? List<String> ?: emptyList()

        if (likedBookIds.isEmpty()) return emptyList()

        val books = mutableListOf<Book>()

        for (bookId in likedBookIds) {
            val bookSnapshot = firestore.collection("books").document(bookId).get().await()
            val book = bookSnapshot.toObject(Book::class.java)?.copy(id = bookSnapshot.id)

            if (book != null) {
                val volumeSnapshots = firestore.collection("books")
                    .document(bookId)
                    .collection("volumes")
                    .get()
                    .await()

                val volumes = volumeSnapshots.mapNotNull { volumeDoc ->
                    val volume = volumeDoc.toObject(Volume::class.java)
                    val chapterSnapshots = volumeDoc.reference
                        .collection("chapters")
                        .get()
                        .await()

                    val chapters = chapterSnapshots.mapNotNull { it.toObject(Chapter::class.java) }
                    volume?.apply { this.chapters = chapters }
                }

                book.volume = volumes
                books.add(book)
            }
        }

        return books
    }
    //Upload


    fun uploadImageToCloudinary(uri: Uri, folder: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        MediaManager.get().upload(uri)
            .option("folder", folder)
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) onSuccess(url)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    onFailure(error.description)
                }
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    fun uploadBook(book: Book, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("books")
            .document(book.id)
            .set(book)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Unknown error") }
    }
    private suspend fun <T> List<Task<T>>.awaitAll(): List<T> {
        return map { it.await() }
    }


}