package com.example.roomtune

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    navController: NavHostController, 
    reservationList: List<Reservation>,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
){
    val calendar = Calendar.getInstance()
    val currentDate = SimpleDateFormat("M/d/yyyy", Locale.getDefault()).format(calendar.time)
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)

    val activeRoomsCount = reservationList.count { 
        it.date == currentDate && currentTime >= it.timeIn && currentTime <= it.timeOut 
    }
    val totalBookings = reservationList.size

    MainScaffold(
        title = "Dashboard", 
        navController = navController, 
        currentRoute = "home",
        isDarkMode = isDarkMode,
        onThemeToggle = onThemeToggle
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "Welcome Admin",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Here's what's happening today",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Quick Guide", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Use the navigation bar below to start reserving rooms or view existing schedules. You can delete outdated reservations in the Schedules tab.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Overview",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                DashboardStatCard(
                    title = "Active",
                    value = "$activeRoomsCount Rooms",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary,
                    isDarkMode = isDarkMode
                )
                Spacer(modifier = Modifier.width(16.dp))
                DashboardStatCard(
                    title = "Total",
                    value = "$totalBookings Bookings",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary,
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

@Composable
fun DashboardStatCard(title: String, value: String, modifier: Modifier = Modifier, color: Color, isDarkMode: Boolean) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
