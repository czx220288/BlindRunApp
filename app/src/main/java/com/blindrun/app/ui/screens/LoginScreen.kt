package com.blindrun.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blindrun.app.tts.TtsHelper
import com.blindrun.app.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (userId: String, role: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()  // 改为 hiltViewModel
) {
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }
    var selectedRole by remember { mutableStateOf("blind") }
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        ttsHelper.speak("欢迎使用助盲跑，请选择您的角色并输入用户ID和密码，或注册新账号")
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("助盲跑", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it; errorMessage = null },
            label = { Text("用户ID") },
            placeholder = { Text("例如: blind001") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("密码") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            RadioButton(
                selected = selectedRole == "blind",
                onClick = { selectedRole = "blind"; ttsHelper.speak("盲人用户") }
            )
            Text("我是盲人", modifier = Modifier.padding(start = 8.dp))
            Spacer(modifier = Modifier.width(24.dp))
            RadioButton(
                selected = selectedRole == "companion",
                onClick = { selectedRole = "companion"; ttsHelper.speak("陪跑员") }
            )
            Text("我是陪跑员", modifier = Modifier.padding(start = 8.dp))
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (userId.isBlank() || password.isBlank()) {
                    ttsHelper.speak("请输入用户ID和密码")
                    errorMessage = "请输入完整信息"
                    return@Button
                }
                val success = viewModel.login(userId, password, selectedRole)
                if (success) {
                    ttsHelper.speak("登录成功，欢迎您")
                    onLoginSuccess(userId, selectedRole)
                } else {
                    ttsHelper.speak("登录失败，请检查用户ID或密码")
                    errorMessage = "用户ID或密码错误"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("登录")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text("没有账号？立即注册")
        }
    }
}