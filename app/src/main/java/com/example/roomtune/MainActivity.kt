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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roomtune.ui.theme.RoomTuneTheme
import com.example.roomtune.viewmodel.ReservationViewModel
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
            
            val viewModel: ReservationViewModel = viewModel()

            RoomTuneTheme(darkTheme = isDarkMode) {
                MyApp(
                    isDarkMode = isDarkMode,
                    onThemeToggle = { isDarkMode = !isDarkMode },
                    reservationList = viewModel.reservationList,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun MyApp(
    isDarkMode: Boolean, 
    onThemeToggle: () -> Unit, 
    reservationList: MutableList<Reservation>,
    viewModel: ReservationViewModel
){
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
            AdminMainPager(navController, reservationList, isDarkMode, onThemeToggle, viewModel, 0)
        }
        composable("reserveRoom"){
            AdminMainPager(navController, reservationList, isDarkMode, onThemeToggle, viewModel, 1)
        }
        composable("viewStudents"){
            AdminMainPager(navController, reservationList, isDarkMode, onThemeToggle, viewModel, 2)
        }
        composable(
            "editRoom/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ){ backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: -1
            ReserveRoomScreen(
                navController, 
                reservationList, 
                index, 
                isDarkMode = isDarkMode, 
                onThemeToggle = onThemeToggle,
                viewModel = viewModel
            )
        }
        composable("publicSchedules"){
            ViewReservationScreen(
                navController, 
                reservationList, 
                isAdmin = false, 
                isDarkMode = isDarkMode, 
                onThemeToggle = onThemeToggle,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun AdminMainPager(
    navController: NavHostController, 
    reservationList: MutableList<Reservation>, 
    isDarkMode: Boolean, 
    onThemeToggle: () -> Unit,
    viewModel: ReservationViewModel,
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
                    wrapInScaffold = false,
                    viewModel = viewModel
                )
                2 -> ViewReservationScreen(
                    navController = navController, 
                    reservationList = reservationList, 
                    isAdmin = true, 
                    isDarkMode = isDarkMode, 
                    onThemeToggle = onThemeToggle,
                    wrapInScaffold = false,
                    viewModel = viewModel
                )
            }
        }
    }
}
