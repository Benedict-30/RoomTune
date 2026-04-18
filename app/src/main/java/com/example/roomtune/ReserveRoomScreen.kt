package com.example.roomtune

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.roomtune.model.Reservation
import com.example.roomtune.util.formatTo12Hour
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun ReserveRoomScreen(
    navController: NavHostController, 
    reservationList: MutableList<Reservation>,
    editingIndex: Int = -1,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onNavigate: ((String) -> Unit)? = null,
    wrapInScaffold: Boolean = true
){
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val isEditing = editingIndex != -1
    val existingReservation = if (isEditing) reservationList.getOrNull(editingIndex) else null

    var building by remember { mutableStateOf(existingReservation?.building ?: "") }
    var roomNum by remember { mutableStateOf(existingReservation?.room ?: "") }
    var date by remember { mutableStateOf(existingReservation?.date ?: "") }
    var timeIn by remember { mutableStateOf(existingReservation?.timeIn ?: "") }
    var timeOut by remember { mutableStateOf(existingReservation?.timeOut ?: "") }
    var purpose by remember { mutableStateOf(existingReservation?.purpose ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            date = "${selectedMonth + 1}/$selectedDay/$selectedYear"
        }, year, month, day
    )

    val timeInPickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            timeIn = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
        }, hour, minute, false
    )

    val timeOutPickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            timeOut = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
        }, hour, minute, false
    )

    val content = @Composable { paddingValues: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
        ){
            Text(if (isEditing) "Update Details" else "Booking Details", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            ReservationField(
                value = roomNum,
                onValueChange = {roomNum = it},
                label = "Room Number",
                placeholder = "e.g. 101"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ReservationField(
                value = building,
                onValueChange = {building = it},
                label = "Building Name",
                placeholder = "e.g. Science Wing"
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = date,
                onValueChange = { },
                label = {Text("Date")},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                enabled = false,
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { datePickerDialog.show() }
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = Color.Gray,
                    disabledLabelColor = Color.Gray,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formatTo12Hour(timeIn),
                    onValueChange = { },
                    label = {Text("Time In")},
                    modifier = Modifier
                        .weight(1f)
                        .clickable { timeInPickerDialog.show() },
                    enabled = false,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = Color.Gray,
                        disabledLabelColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = formatTo12Hour(timeOut),
                    onValueChange = { },
                    label = {Text("Time Out")},
                    modifier = Modifier
                        .weight(1f)
                        .clickable { timeOutPickerDialog.show() },
                    enabled = false,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = Color.Gray,
                        disabledLabelColor = Color.Gray
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ReservationField(
                value = purpose,
                onValueChange = {purpose = it},
                label = "Purpose",
                placeholder = "e.g. Group Study"
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (isSaving) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Button(
                    onClick = {
                        if (roomNum.isNotBlank() && building.isNotBlank() && date.isNotBlank() && timeIn.isNotBlank() && timeOut.isNotBlank() && purpose.isNotBlank()) {
                            if (timeIn >= timeOut) {
                                Toast.makeText(context, "Time Out must be after Time In", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val isConflict = reservationList.filterIndexed { index, _ -> index != editingIndex }.any {
                                it.room == roomNum && it.building == building && it.date == date &&
                                (timeIn < it.timeOut && it.timeIn < timeOut)
                            }

                            if (isConflict) {
                                Toast.makeText(context, "Already reserved for this time range", Toast.LENGTH_SHORT).show()
                            } else {
                                isSaving = true
                                val reservationData = hashMapOf(
                                    "room" to roomNum,
                                    "building" to building,
                                    "date" to date,
                                    "timeIn" to timeIn,
                                    "timeOut" to timeOut,
                                    "purpose" to purpose
                                )

                                val task = if (isEditing && existingReservation != null) {
                                    db.collection("schedules").document(existingReservation.id).set(reservationData)
                                } else {
                                    db.collection("schedules").add(reservationData)
                                }

                                task.addOnSuccessListener {
                                    isSaving = false
                                    if (onNavigate != null) onNavigate("viewStudents")
                                    else navController.navigate("viewStudents") {
                                        popUpTo("home") { inclusive = false }
                                    }
                                }.addOnFailureListener { e ->
                                    isSaving = false
                                    Log.e("ReserveRoomScreen", "Error saving schedule", e)
                                    Toast.makeText(context, "Failed to save schedule: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ){
                    Text(if (isEditing) "Update Reservation" else "Confirm Reservation", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    if (wrapInScaffold) {
        MainScaffold(
            title = if (isEditing) "Edit Reservation" else "New Reservation", 
            navController = navController, 
            currentRoute = "reserveRoom",
            isDarkMode = isDarkMode,
            onThemeToggle = onThemeToggle,
            onNavigate = onNavigate,
            content = content
        )
    } else {
        content(PaddingValues(0.dp))
    }
}

@Composable
fun ReservationField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = Color.Gray
        )
    )
}
