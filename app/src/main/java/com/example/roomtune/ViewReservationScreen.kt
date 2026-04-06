package com.example.roomtune

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ViewReservationScreen(
    navController: NavHostController, 
    reservationList: MutableList<Reservation>, 
    isAdmin: Boolean = true,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
){
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredReservations = remember(searchQuery, reservationList.toList()) {
        if (searchQuery.isBlank()) {
            reservationList
        } else {
            reservationList.filter { 
                it.room.contains(searchQuery, ignoreCase = true) ||
                it.building.contains(searchQuery, ignoreCase = true) ||
                it.purpose.contains(searchQuery, ignoreCase = true) ||
                it.date.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    MainScaffold(
        title = if (isAdmin) "Manage Schedules" else "Room Schedules",
        navController = navController,
        currentRoute = if (isAdmin) "viewStudents" else "publicSchedules",
        isAdmin = isAdmin,
        isDarkMode = isDarkMode,
        onThemeToggle = onThemeToggle
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 24.dp),
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search room, building, or date...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                )
            )

            if (filteredReservations.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (searchQuery.isEmpty()) "No reservations found" else "No matches found for \"$searchQuery\"", 
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), 
                            fontSize = 18.sp
                        )
                        if (isAdmin && searchQuery.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.navigate("reserveRoom") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Text("Make one now", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredReservations) { reservation ->
                        val originalIndex = reservationList.indexOf(reservation)
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Room ${reservation.room}",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = reservation.purpose,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    if (isAdmin) {
                                        Row {
                                            IconButton(
                                                onClick = { navController.navigate("editRoom/$originalIndex") },
                                                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = {
                                                    db.collection("schedules").document(reservation.id).delete()
                                                        .addOnSuccessListener {
                                                            Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                                },
                                                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

                                InfoRow(Icons.Default.LocationOn, reservation.building)
                                Spacer(modifier = Modifier.height(8.dp))
                                InfoRow(Icons.Default.DateRange, "${reservation.date}  •  ${formatTo12Hour(reservation.timeIn)} - ${formatTo12Hour(reservation.timeOut)}")
                            }
                        }
                    }
                }
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { navController.navigate("home") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                    )
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dashboard", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), 
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 14.sp)
    }
}
