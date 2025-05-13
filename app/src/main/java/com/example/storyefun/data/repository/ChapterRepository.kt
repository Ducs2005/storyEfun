package com.example.storyefun.data.repository

import android.net.Uri
import com.example.storyefun.data.models.Chapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChapterRepository {

    private val db = FirebaseFirestore.getInstance()

    // Lấy danh sách chương theo volumeId
    fun getChapters(bookId: String, volumeId: String): Flow<List<Chapter>> = callbackFlow {
        val chaptersRef = db.collection("books")
            .document(bookId)
            .collection("volumes")
            .document(volumeId)
            .collection("chapters")

        val listener = chaptersRef.orderBy("order")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chapters = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data
                        data?.let {
                            Chapter(
                                id = doc.id,
                                title = it["title"] as? String ?: "",
                                order = it["order"] as? Long ?: 0,
                                content = (it["content"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                price = it["price"] as? Int ?: 0
                            )
                        }
                    }
                    trySend(chapters).isSuccess
                }
            }

        awaitClose { listener.remove() }
    }

    // Upload chương mới
    suspend fun uploadChapter(
        bookId: String,
        volumeId: String,
        title: String,
        imageUrls: List<String>,
        onComplete: () -> Unit
    ) {
        val chaptersRef = db.collection("books")
            .document(bookId)
            .collection("volumes")
            .document(volumeId)
            .collection("chapters")

        val chapterData = hashMapOf(
            "title" to title,
            "content" to imageUrls,
            "order" to System.currentTimeMillis(),
            "createdAt" to System.currentTimeMillis(),
            "locked" to false
        )

        chaptersRef.add(chapterData).await()

        // Sau khi thêm chương, kiểm tra và khóa các chương từ chương 3 trở đi
        val snapshot = chaptersRef.orderBy("order").get().await()
        val chapterDocs = snapshot.documents
        if (chapterDocs.size > 2) {
            chapterDocs.drop(2).forEach { doc ->
                chaptersRef.document(doc.id).update("locked", true).await()
            }
        }
        onComplete()
    }

    // Upload ảnh lên Cloudinary
    fun uploadImageToCloudinary(uri: Uri, folder: String): Flow<String> = callbackFlow {
        MediaManager.get().upload(uri)
            .option("folder", folder)
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        trySend(secureUrl).isSuccess
                        close()
                    } else {
                        close(Exception("Secure URL is null"))
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    close(Exception("Upload error: ${error.description}"))
                }

                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()

        awaitClose {}
    }
}