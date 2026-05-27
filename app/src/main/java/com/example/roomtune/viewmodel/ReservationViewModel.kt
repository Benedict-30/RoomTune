package com.example.roomtune.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtune.data.ReservationRepository
import com.example.roomtune.model.Reservation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReservationViewModel(private val repository: ReservationRepository = ReservationRepository()) : ViewModel() {
    
    val reservationList = mutableStateListOf<Reservation>()

    init {
        observeReservations()
    }

    private fun observeReservations() {
        viewModelScope.launch {
            repository.getReservations().collectLatest { list ->
                reservationList.clear()
                reservationList.addAll(list)
                repository.cleanupExpiredSchedules(list)
            }
        }
    }

    fun saveReservation(
        room: String,
        building: String,
        date: String,
        timeIn: String,
        timeOut: String,
        purpose: String,
        id: String? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        // Business Logic: Conflict check
        val isConflict = reservationList.any {
            it.id != id && 
            it.room == room && 
            it.building == building && 
            it.date == date &&
            (timeIn < it.timeOut && it.timeIn < timeOut)
        }

        if (isConflict) {
            onComplete(false, "Already reserved for this time range")
            return
        }

        val reservationData = hashMapOf(
            "room" to room,
            "building" to building,
            "date" to date,
            "timeIn" to timeIn,
            "timeOut" to timeOut,
            "purpose" to purpose
        )

        repository.saveReservation(reservationData, id) { success, exception ->
            if (success) {
                onComplete(true, null)
            } else {
                onComplete(false, exception?.message ?: "Unknown error")
            }
        }
    }

    fun deleteReservation(id: String, onComplete: (Boolean, String?) -> Unit) {
        repository.deleteReservation(id) { success, exception ->
            if (success) {
                onComplete(true, null)
            } else {
                onComplete(false, exception?.message ?: "Unknown error")
            }
        }
    }
}
