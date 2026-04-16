package com.example.roomtune.model

import com.google.firebase.firestore.DocumentId

data class Reservation (
    @DocumentId val id: String = "",
    var room: String = "",
    var building: String = "",
    var date: String = "",
    var timeIn: String = "",
    var timeOut: String = "",
    var purpose: String = ""
)
