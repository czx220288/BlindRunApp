package com.blindrun.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.blindrun.app.ui.screens.*
import com.blindrun.app.ui.theme.BlindRunTheme
import com.blindrun.app.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BlindRunTheme {
                AppNavigation()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    var isLoggedIn by remember { mutableStateOf(authViewModel.isLoggedIn()) }
    var userRole by remember { mutableStateOf(authViewModel.currentUserRole ?: "") }
    var currentUserId by remember { mutableStateOf(authViewModel.currentUserId ?: "") }

    // 监听登录状态变化
    LaunchedEffect(authViewModel.currentUserId) {
        isLoggedIn = authViewModel.isLoggedIn()
        userRole = authViewModel.currentUserRole ?: ""
        currentUserId = authViewModel.currentUserId ?: ""
    }

    val canNavigateBack = navController.previousBackStackEntry != null

    Scaffold(
        topBar = {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != "login" && currentRoute != "register") {
                TopAppBar(
                    title = { Text("助盲跑") },
                    navigationIcon = {
                        if (canNavigateBack) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors()
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) "home" else "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = { userId, role ->
                        isLoggedIn = true
                        userRole = role
                        currentUserId = userId
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = { userId, role ->
                        isLoggedIn = true
                        userRole = role
                        currentUserId = userId
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onBackToLogin = { navController.popBackStack() }
                )
            }
            composable("home") {
                HomeScreen(
                    role = userRole,
                    onNavigateToNearby = { navController.navigate("nearby") },
                    onNavigateToPublish = { navController.navigate("publish") },
                    onNavigateToMyMatches = { navController.navigate("myMatches") },
                    onLogout = {
                        authViewModel.logout()
                        isLoggedIn = false
                        userRole = ""
                        currentUserId = ""
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
            composable("nearby") {
                NearbyScreen(
                    onAcceptClick = { recruit ->
                        navController.navigate("run/${recruit.id}")
                    }
                )
            }
            composable("publish") {
                PublishRecruitScreen(
                    navController = navController,
                    onPublishSuccess = { navController.popBackStack() }
                )
            }
            composable("run/{recruitId}") { backStackEntry ->
                val recruitId = backStackEntry.arguments?.getString("recruitId") ?: ""
                RunScreen(
                    recruitId = recruitId,
                    onSosTriggered = { navController.navigate("sos") },
                    onFinish = { navController.popBackStack() }
                )
            }
            composable("sos") {
                SosScreen(onBack = { navController.popBackStack() })
            }
            composable("myMatches") {
                MyMatchesScreen(
                    userId = currentUserId,
                    role = userRole,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}