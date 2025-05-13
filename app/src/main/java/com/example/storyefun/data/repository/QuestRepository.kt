package com.example.storyefun.data.repository

import android.util.Log
import com.example.storyefun.data.models.Quest
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class QuestRepository {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun getQuests(userId: String): List<Quest> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("quests")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
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
        } catch (e: Exception) {
            Log.e("QuestRepo", "getQuests error: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun initializeQuests(userId: String) {
        try {
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
                    ),
                )

                val batch = db.batch()
                quests.forEach { quest ->
                    val questRef = db.collection("users")
                        .document(userId)
                        .collection("quests")
                        .document()
                    batch.set(questRef, quest)
                }

                batch.commit().await()
            }
        } catch (e: Exception) {
            Log.e("QuestRepo", "initializeQuests error: ${e.message}", e)
        }
    }

    suspend fun completeQuest(userId: String, questId: String, reward: Long): Boolean {
        return try {
            db.runTransaction { transaction ->
                val userRef = db.collection("users").document(userId)
                val questRef = userRef.collection("quests").document(questId)

                val userSnap = transaction.get(userRef)
                val currentCoins = userSnap.getLong("coin") ?: 0L

                // Cập nhật trạng thái nhiệm vụ
                transaction.update(questRef, mapOf(
                    "completed" to true,
                    "progress" to 0L  // Đặt lại tiến độ về 0
                ))
                // Cộng coin vào tài khoản người dùng
                transaction.update(userRef, "coin", currentCoins + reward)
            }.await()
            Log.d("QuestRepo", "Quest $questId completed successfully")
            true
        } catch (e: Exception) {
            Log.e("QuestRepo", "completeQuest error: ${e.message}", e)
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
                Log.d("QuestRepo", "Quest $questId ($type) already completed, skipping update")
                return
            }

            // Cộng dồn tiến độ
            val newProgress = (currentProgress + progressToAdd).coerceAtMost(requiredProgress)
            val updates = mutableMapOf<String, Any>(
                "progress" to newProgress
            )

            // Nếu tiến độ đủ, đánh dấu là hoàn thành
            if (newProgress >= requiredProgress) {
                updates["completed"] = true
            }

            // Cập nhật Firestore
            questRef.update(updates).await()
            Log.d("QuestRepo", "Updated quest $questId ($type): progress=$newProgress, completed=${updates["completed"]}")
        } catch (e: Exception) {
            Log.e("QuestRepo", "updateQuestProgress error: ${e.message}", e)
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

            snapshot.documents.forEach { doc ->
                val resetTimeMillis = doc.getTimestamp("resetTime")?.toDate()?.time ?: 0L
                if (now > resetTimeMillis) {
                    val nextReset = now + 24 * 60 * 60 * 1000
                    batch.update(doc.reference, mapOf(
                        "completed" to false,
                        "progress" to if (doc.getString("type") == "daily_login") 1L else 0L,
                        "resetTime" to Timestamp(nextReset / 1000, 0)
                    ))
                }
            }

            batch.commit().await()
        } catch (e: Exception) {
            Log.e("QuestRepo", "resetDailyQuests error: ${e.message}", e)
        }
    }
}