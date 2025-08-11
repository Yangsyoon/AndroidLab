package com.example.androidlab.ActivityD

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MyFaceScoreViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    val faceScores: Flow<List<MyFaceScore>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection("faceScores")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val dateStr = doc.getString("date") ?: return@mapNotNull null
                        MyFaceScore(
                            id = doc.id,
                            date = dateStr, // 문자열로 바로 저장
                            emotion1Correct = (doc.getLong("emotion1Correct") ?: 0L).toInt(),
                            emotion1Wrong = (doc.getLong("emotion1Wrong") ?: 0L).toInt(),
                            emotion2Correct = (doc.getLong("emotion2Correct") ?: 0L).toInt(),
                            emotion2Wrong = (doc.getLong("emotion2Wrong") ?: 0L).toInt(),
                            emotion3Correct = (doc.getLong("emotion3Correct") ?: 0L).toInt(),
                            emotion3Wrong = (doc.getLong("emotion3Wrong") ?: 0L).toInt(),
                            emotion4Correct = (doc.getLong("emotion4Correct") ?: 0L).toInt(),
                            emotion4Wrong = (doc.getLong("emotion4Wrong") ?: 0L).toInt(),
                            emotion5Correct = (doc.getLong("emotion5Correct") ?: 0L).toInt(),
                            emotion5Wrong = (doc.getLong("emotion5Wrong") ?: 0L).toInt(),
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(list)
            }

        awaitClose { listenerRegistration.remove() }
    }
}
