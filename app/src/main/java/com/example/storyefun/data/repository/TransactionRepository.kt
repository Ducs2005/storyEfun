package com.example.storyefun.data.repository

import android.util.Log
import com.example.storyefun.data.models.Transactions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class TransactionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val transactionsCollection = db.collection("transactions")

    suspend fun addTransaction(
        userID: String,
        coin: Int,
        money: Double
    ): Result<Unit> {
        return try {
            val transactionData = hashMapOf(
                "uid" to userID,
                "coin" to coin,
                "money" to money,
                "time" to System.currentTimeMillis()
            )
            transactionsCollection
                .add(transactionData)
                .await()
            Log.d("TransactionRepo", "Transaction added for userId=$userID, coin=$coin, money=$money")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error adding transaction: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getTransactions(): Result<List<Transactions>> {
        return try {
            val snapshot = transactionsCollection
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .await()
            val transactions = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    try {
                        Transactions(
                            uid = data["uid"] as? String ?: "",
                            coin = when (val coinValue = data["coin"]) {
                                is Long -> coinValue.toInt()
                                is Int -> coinValue
                                else -> 0
                            },
                            money = when (val moneyValue = data["money"]) {
                                is Long -> moneyValue.toDouble()
                                is Double -> moneyValue
                                else -> 0.0
                            },
                            time = data["time"] as? Long ?: 0L
                        )
                    } catch (e: Exception) {
                        Log.w("TransactionRepo", "Error parsing transaction ${doc.id}: ${e.message}")
                        null
                    }
                }
            }
            Log.d("TransactionRepo", "Fetched ${transactions.size} transaction")
            Result.success(transactions)
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error fetching transactions: ${e.message}", e)
            Result.failure(e)
        }
    }
}