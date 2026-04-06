package com.example.roomtune

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    title: String,
    navController: NavHostController,
    currentRoute: String,
    isAdmin: Boolean = true,
    isDarkMode: Boolean = true,
    onThemeToggle: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.NightlightRound,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    TextButton(onClick = {
                        val destination = if (isAdmin) "login" else "get"
                        navController.navigate(destination) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Text(
                            if (isAdmin) "Logout" else "Back", 
                            color = if (isAdmin) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (isAdmin) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "reserveRoom",
                        onClick = { if (currentRoute != "reserveRoom") navController.navigate("reserveRoom") },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Reserve") },
                        label = { Text("Reserve") },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = Color.Gray
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "viewStudents",
                        onClick = { if (currentRoute != "viewStudents") navController.navigate("viewStudents") },
                        icon = { Icon(Icons.Default.List, contentDescription = "Schedules") },
                        label = { Text("Schedules") },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        },
        content = content,
        containerColor = MaterialTheme.colorScheme.background
    )
}
