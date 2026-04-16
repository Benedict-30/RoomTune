package com.example.roomtune.util

import java.text.SimpleDateFormat
import java.util.Locale

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
