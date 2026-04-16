package com.example.roomtune

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.roomtune.model.Reservation
import com.example.roomtune.ui.theme.RoomTuneTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
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
                MyApp(
                    isDarkMode = isDarkMode,
                    onThemeToggle = { isDarkMode = !isDarkMode },
                    reservationList = reservationList
                )
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
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(400)
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
            AdminMainPager(navController, reservationList, isDarkMode, onThemeToggle, 0)
        }
        composable("reserveRoom"){
            AdminMainPager(navController, reservationList, isDarkMode, onThemeToggle, 1)
        }
        composable("viewStudents"){
            AdminMainPager(navController, reservationList, isDarkMode, onThemeToggle, 2)
        }
        composable(
            "editRoom/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ){ backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: -1
            ReserveRoomScreen(navController, reservationList, index, isDarkMode = isDarkMode, onThemeToggle = onThemeToggle)
        }
        composable("publicSchedules"){
            ViewReservationScreen(navController, reservationList, isAdmin = false, isDarkMode = isDarkMode, onThemeToggle = onThemeToggle)
        }
    }
}

@Composable
fun AdminMainPager(
    navController: NavHostController, 
    reservationList: MutableList<Reservation>, 
    isDarkMode: Boolean, 
    onThemeToggle: () -> Unit,
    initialPage: Int = 0
) {
    val pagerState = rememberPagerState(initialPage = initialPage) { 3 }
    val scope = rememberCoroutineScope()

    val currentRoute = when (pagerState.currentPage) {
        0 -> "home"
        1 -> "reserveRoom"
        2 -> "viewStudents"
        else -> "home"
    }

    val currentTitle = when (pagerState.currentPage) {
        0 -> "Dashboard"
        1 -> "New Reservation"
        2 -> "Manage Schedules"
        else -> "Dashboard"
    }

    MainScaffold(
        title = currentTitle,
        navController = navController,
        currentRoute = currentRoute,
        isDarkMode = isDarkMode,
        onThemeToggle = onThemeToggle,
        onNavigate = { route ->
            val target = when(route) {
                "home" -> 0
                "reserveRoom" -> 1
                "viewStudents" -> 2
                else -> -1
            }
            if (target != -1) {
                scope.launch { pagerState.animateScrollToPage(target) }
            } else {
                navController.navigate(route)
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            userScrollEnabled = true,
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    navController = navController, 
                    reservationList = reservationList, 
                    isDarkMode = isDarkMode, 
                    onThemeToggle = onThemeToggle,
                    wrapInScaffold = false
                )
                1 -> ReserveRoomScreen(
                    navController = navController, 
                    reservationList = reservationList, 
                    isDarkMode = isDarkMode, 
                    onThemeToggle = onThemeToggle,
                    wrapInScaffold = false
                )
                2 -> ViewReservationScreen(
                    navController = navController, 
                    reservationList = reservationList, 
                    isAdmin = true, 
                    isDarkMode = isDarkMode, 
                    onThemeToggle = onThemeToggle,
                    wrapInScaffold = false
                )
            }
        }
    }
}
