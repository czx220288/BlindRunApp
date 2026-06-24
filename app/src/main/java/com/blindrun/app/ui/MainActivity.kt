package com.blindrun.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.blindrun.app.network.WebSocketManager
import com.blindrun.app.ui.screens.*
import com.blindrun.app.ui.theme.BlindRunTheme
import com.blindrun.app.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var webSocketManager: WebSocketManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            android.widget.Toast.makeText(this, "需要位置权限才能使用完整功能", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> { }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        setContent {
            BlindRunTheme {
                AppNavigation(webSocketManager = webSocketManager)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(webSocketManager: WebSocketManager) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    var isLoggedIn by remember { mutableStateOf(authViewModel.isLoggedIn()) }
    var userRole by remember { mutableStateOf(authViewModel.currentUserRole ?: "") }
    var currentUserId by remember { mutableStateOf(authViewModel.currentUserId ?: "") }

    LaunchedEffect(authViewModel.currentUserId) {
        isLoggedIn = authViewModel.isLoggedIn()
        userRole = authViewModel.currentUserRole ?: ""
        currentUserId = authViewModel.currentUserId ?: ""
    }

    // 监听 WebSocket 通知
    LaunchedEffect(Unit) {
        webSocketManager.notificationFlow.collect { notification ->
            android.util.Log.d("MainActivity", "Received notification: $notification")
            if (notification["type"] == "start_run") {
                val recruitId = notification["recruitId"]
                if (recruitId != null && recruitId.isNotEmpty()) {
                    val currentRoute = navController.currentDestination?.route
                    if (currentRoute?.startsWith("run/") != true) {
                        android.util.Log.d("MainActivity", "Navigating to run/$recruitId")
                        navController.navigate("run/$recruitId")
                    } else {
                        android.util.Log.d("MainActivity", "Already in run page")
                    }
                }
            }
        }
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
                        authViewModel.saveLogin(userId, role, "用户$userId")
                        // 登录后连接 WebSocket，使用 userId 作为 sessionId
                        webSocketManager.connect(userId, userId)
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
                        authViewModel.saveLogin(userId, role, "用户$userId")
                        webSocketManager.connect(userId, userId)
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
                        webSocketManager.disconnect()
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
                    userId = currentUserId,
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
                    onSosTriggered = { },
                    onFinish = { navController.popBackStack() }
                )
            }
            composable("myMatches") {
                MyMatchesScreen(
                    userId = currentUserId,
                    userRole = userRole,
                    onBack = { navController.popBackStack() },
                    onStartMatch = { recruitId ->
                        navController.navigate("run/$recruitId")
                    }
                )
            }

        }
    }
}