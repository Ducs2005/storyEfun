package com.example.storyefun.data.repository

import android.util.Log
import com.example.storyefun.data.models.Quest
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class QuestRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private val TAG = "QuestRepo"

    suspend fun getQuests(userId: String): List<Quest> {
        return try {
            // Reset nhiệm vụ trước khi lấy danh sách để đảm bảo dữ liệu mới
            resetDailyQuests(userId)

            val snapshot = db.collection("users")
                .document(userId)
                .collection("quests")
                .get()
                .await()

            val quests = snapshot.documents.mapNotNull { doc ->
                Quest(
                    id = doc.id,
                    type = doc.getString("type") ?: "",
                    completed = doc.getBoolean("completed") ?: false,
                    progress = doc.getLong("progress") ?: 0L,
                    requiredProgress = doc.getLong("requiredProgress") ?: 1L,
                    reward = doc.getLong("reward") ?: 0L,
                    resetTime = doc.getTimestamp("resetTime")
                )
            }
            Log.d(TAG, "getQuests: Fetched ${quests.size} quests for user $userId")
            quests
        } catch (e: Exception) {
            Log.e(TAG, "getQuests error: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun initializeQuests(userId: String) {
        try {
            val userRef = db.collection("users").document(userId)
            val userSnap = userRef.get().await()
            val questsInitialized = userSnap.getBoolean("questsInitialized") ?: false

            if (!questsInitialized) {
                val existingQuests = getQuests(userId)
                if (existingQuests.isEmpty()) {
                    val now = System.currentTimeMillis()
                    val nextReset = now + 24 * 60 * 60 * 1000 // +24h

                    val quests = listOf(
                        mapOf(
                            "type" to "daily_login",
                            "completed" to false,
                            "progress" to 1L,
                            "requiredProgress" to 1L,
                            "reward" to 10L,
                            "resetTime" to Timestamp(nextReset / 1000, 0)
                        ),
                        mapOf(
                            "type" to "online_one_minute",
                            "completed" to false,
                            "progress" to 0L,
                            "requiredProgress" to 1L,
                            "reward" to 5L,
                            "resetTime" to Timestamp(nextReset / 1000, 0)
                        ),
                        mapOf(
                            "type" to "online_two_minute",
                            "completed" to false,
                            "progress" to 0L,
                            "requiredProgress" to 1L,
                            "reward" to 5L,
                            "resetTime" to Timestamp(nextReset / 1000, 0)
                        ),
                        mapOf(
                            "type" to "online_twenty_minute",
                            "completed" to false,
                            "progress" to 0L,
                            "requiredProgress" to 1L,
                            "reward" to 5L,
                            "resetTime" to Timestamp(nextReset / 1000, 0)
                        )
                    )

                    val batch = db.batch()
                    quests.forEach { quest ->
                        val questRef = userRef.collection("quests").document()
                        batch.set(questRef, quest)
                    }
                    // Đánh dấu rằng nhiệm vụ đã được khởi tạo
                    batch.update(userRef, mapOf("questsInitialized" to true))
                    batch.commit().await()
                    Log.d(TAG, "initializeQuests: Initialized ${quests.size} quests for user $userId")
                } else {
                    Log.d(TAG, "initializeQuests: Quests already exist, skipping initialization")
                }
            } else {

            }
        } catch (e: Exception) {
            Log.e(TAG, "initializeQuests error: ${e.message}", e)
        }
    }

    suspend fun completeQuest(userId: String, questId: String, reward: Long): Boolean {
        return try {
            db.runTransaction { transaction ->
                val userRef = db.collection("users").document(userId)
                val questRef = userRef.collection("quests").document(questId)

                val userSnap = transaction.get(userRef)
                val currentCoins = userSnap.getLong("coin") ?: 0L

                transaction.update(questRef, mapOf(
                    "completed" to true,
                    "progress" to 0L
                ))
                transaction.update(userRef, "coin", currentCoins + reward)
            }.await()
            Log.d(TAG, "completeQuest: Quest $questId completed successfully for user $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "completeQuest error: ${e.message}", e)
            false
        }
    }

    suspend fun updateQuestProgress(userId: String, questId: String, progressToAdd: Long) {
        try {
            val questRef = db.collection("users")
                .document(userId)
                .collection("quests")
                .document(questId)

            val snapshot = questRef.get().await()
            val type = snapshot.getString("type") ?: ""
            val currentProgress = snapshot.getLong("progress") ?: 0L
            val requiredProgress = snapshot.getLong("requiredProgress") ?: 1L
            val isCompleted = snapshot.getBoolean("completed") ?: false

            if (isCompleted) {
                Log.d(TAG, "updateQuestProgress: Quest $questId ($type) already completed, skipping")
                return
            }

            val newProgress = (currentProgress + progressToAdd).coerceAtMost(requiredProgress)
            val updates = mutableMapOf<String, Any>("progress" to newProgress)

            if (newProgress >= requiredProgress) {
                updates["completed"] = true
            }

            questRef.update(updates).await()
            Log.d(TAG, "updateQuestProgress: Updated quest $questId ($type): progress=$newProgress, completed=${updates["completed"]}")
        } catch (e: Exception) {
            Log.e(TAG, "updateQuestProgress error: ${e.message}", e)
        }
    }

    suspend fun resetDailyQuests(userId: String) {
        try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("quests")
                .get()
                .await()

            val batch = db.batch()
            val now = System.currentTimeMillis()
            val questsByType = mutableMapOf<String, MutableList<DocumentSnapshot>>()

            // Nhóm các nhiệm vụ theo type để kiểm tra trùng lặp
            snapshot.documents.forEach { doc ->
                val type = doc.getString("type") ?: ""
                if (type.isNotEmpty()) {
                    questsByType.getOrPut(type) { mutableListOf() }.add(doc)
                }
            }

            // Xóa các nhiệm vụ trùng lặp, giữ nhiệm vụ mới nhất
            questsByType.forEach { (type, docs) ->
                if (docs.size > 1) {
                    val latestDoc = docs.maxByOrNull { it.getTimestamp("resetTime")?.toDate()?.time ?: 0L }
                    docs.filter { it != latestDoc }.forEach { doc ->
                        batch.delete(doc.reference)
                        Log.d(TAG, "resetDailyQuests: Deleted duplicate quest $type with ID ${doc.id}")
                    }
                }
            }

            // Reset các nhiệm vụ còn lại
            snapshot.documents.forEach { doc ->
                val resetTimeMillis = doc.getTimestamp("resetTime")?.toDate()?.time ?: 0L
                val type = doc.getString("type") ?: ""
                if (now > resetTimeMillis) {
                    val nextReset = now + 24 * 60 * 60 * 1000
                    batch.update(doc.reference, mapOf(
                        "completed" to false,
                        "progress" to if (type == "daily_login") 1L else 0L,
                        "resetTime" to Timestamp(nextReset / 1000, 0)
                    ))
                    Log.d(TAG, "resetDailyQuests: Reset quest $type with ID ${doc.id}")
                }
            }

            batch.commit().await()
            Log.d(TAG, "resetDailyQuests: Completed reset for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "resetDailyQuests error: ${e.message}", e)
        }
    }
}