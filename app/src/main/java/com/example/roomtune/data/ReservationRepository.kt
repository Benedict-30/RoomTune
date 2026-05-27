package com.example.roomtune.data

import android.util.Log
import com.example.roomtune.model.Reservation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.*

class ReservationRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun getReservations(): Flow<List<Reservation>> = callbackFlow {
        val registration = db.collection("schedules")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("ReservationRepository", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.toObjects(Reservation::class.java)
                    trySend(list)
                }
            }
        awaitClose { registration.remove() }
    }

    fun saveReservation(reservation: Map<String, Any>, id: String? = null, onComplete: (Boolean, Exception?) -> Unit) {
        val task = if (id != null) {
            db.collection("schedules").document(id).set(reservation)
        } else {
            db.collection("schedules").add(reservation)
        }

        task.addOnSuccessListener {
            onComplete(true, null)
        }.addOnFailureListener { e ->
            onComplete(false, e)
        }
    }

    fun deleteReservation(id: String, onComplete: (Boolean, Exception?) -> Unit) {
        db.collection("schedules").document(id).delete()
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e) }
    }

    fun cleanupExpiredSchedules(list: List<Reservation>) {
        val now = Calendar.getInstance().time
        val dateTimeFormat = SimpleDateFormat("M/d/yyyy HH:mm", Locale.getDefault())

        list.forEach { reservation ->
            if (reservation.date.isNotEmpty() && reservation.timeOut.isNotEmpty()) {
                try {
                    val endDateTimeStr = "${reservation.date} ${reservation.timeOut}"
                    val endDate = dateTimeFormat.parse(endDateTimeStr)

                    if (endDate != null && endDate.before(now)) {
                        db.collection("schedules").document(reservation.id).delete()
                            .addOnSuccessListener {
                                Log.d("Cleanup", "Automatically deleted expired schedule: ${reservation.id}")
                            }
                    }
                } catch (e: Exception) {
                    Log.e("Cleanup", "Error parsing date/time for reservation ${reservation.id}", e)
                }
            }
        }
    }
}
