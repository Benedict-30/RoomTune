package com.example.roomtune

import com.google.firebase.firestore.DocumentId
import java.text.SimpleDateFormat
import java.util.Locale

data class Reservation (
    @DocumentId val id: String = "",
    var room: String = "",
    var building: String = "",
    var date: String = "",
    var timeIn: String = "",
    var timeOut: String = "",
    var purpose: String = ""
)

fun formatTo12Hour(time24: String): String {
    if (time24.isEmpty()) return ""
    return try {
        val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sdf12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = sdf24.parse(time24)
        sdf12.format(date!!)
    } catch (e: Exception) {
        time24
    }
}
