package com.example.roomtune

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.roomtune.ui.theme.RoomTuneTheme
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemTheme = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemTheme) }
            
            val db = FirebaseFirestore.getInstance()
            val reservationList = remember { mutableStateListOf<Reservation>() }

            // Sync with Firestore and auto-cleanup expired schedules
            DisposableEffect(Unit) {
                val registration = db.collection("schedules")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w("MainActivity", "Listen failed.", e)
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val list = snapshot.toObjects(Reservation::class.java)
                            reservationList.clear()
                            reservationList.addAll(list)
                            
                            // Auto-cleanup logic
                            cleanupExpiredSchedules(db, list)
                        }
                    }
                onDispose {
                    registration.remove()
                }
            }

            RoomTuneTheme(darkTheme = isDarkMode) {
                MyApp(isDarkMode = isDarkMode, onThemeToggle = { isDarkMode = !isDarkMode }, reservationList = reservationList)
            }
        }
    }

    private fun cleanupExpiredSchedules(db: FirebaseFirestore, list: List<Reservation>) {
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
                            .addOnFailureListener { e -> 
                                Log.e("Cleanup", "Failed to delete expired schedule", e) 
                            }
                    }
                } catch (e: Exception) {
                    Log.e("Cleanup", "Error parsing date/time for reservation ${reservation.id}", e)
                }
            }
        }
    }
}

@Composable
fun MyApp(isDarkMode: Boolean, onThemeToggle: () -> Unit, reservationList: MutableList<Reservation>){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "get",
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(400))
        }
    ){
        composable("get"){
            GetStarted(navController)
        }
        composable("login"){
            LoginScreen(navController)
        }
        composable("home"){
            HomeScreen(navController, reservationList, isDarkMode, onThemeToggle)
        }
        composable("reserveRoom"){
            ReserveRoomScreen(navController, reservationList, isDarkMode = isDarkMode, onThemeToggle = onThemeToggle)
        }
        composable(
            "editRoom/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ){ backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: -1
            ReserveRoomScreen(navController, reservationList, index, isDarkMode = isDarkMode, onThemeToggle = onThemeToggle)
        }
        composable("viewStudents"){
            ViewReservationScreen(navController, reservationList, isAdmin = true, isDarkMode = isDarkMode, onThemeToggle = onThemeToggle)
        }
        composable("publicSchedules"){
            ViewReservationScreen(navController, reservationList, isAdmin = false, isDarkMode = isDarkMode, onThemeToggle = onThemeToggle)
        }
    }
}
