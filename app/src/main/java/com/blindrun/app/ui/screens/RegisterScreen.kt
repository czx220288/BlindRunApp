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
fun RegisterScreen(
    onRegisterSuccess: (userId: String, role: String) -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }
    var userName by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("blind") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }

    val registerResult by viewModel.registerResult.collectAsState()

    LaunchedEffect(Unit) {
        ttsHelper.speak("注册页面，请输入用户名、用户ID、密码并确认，选择角色")
    }

    LaunchedEffect(registerResult) {
        if (registerResult != null) {
            isRegistering = false
            if (registerResult == true) {
                ttsHelper.speak("注册成功，请登录")
                onRegisterSuccess(userId, selectedRole)
                viewModel.resetRegisterResult()
            } else {
                errorMessage = "用户ID已存在"
                ttsHelper.speak("用户ID已存在")
                viewModel.resetRegisterResult()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("注册新账号", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it; errorMessage = null },
            label = { Text("姓名") },
            placeholder = { Text("请输入您的用户名") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it; errorMessage = null },
            label = { Text("用户ID") },
            placeholder = { Text("请输入您的用户ID") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("密码") },
            placeholder = { Text("请输入您的密码") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; errorMessage = null },
            label = { Text("确认密码") },
            placeholder = { Text("请再次输入密码") },
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
                when {
                    userName.isBlank() -> {
                        errorMessage = "请填写用户名"
                        ttsHelper.speak("请填写用户名")
                    }
                    userId.isBlank() -> {
                        errorMessage = "请填写用户ID"
                        ttsHelper.speak("请填写用户ID")
                    }
                    password.isBlank() -> {
                        errorMessage = "请填写密码"
                        ttsHelper.speak("请填写密码")
                    }
                    password != confirmPassword -> {
                        errorMessage = "两次输入的密码不一致"
                        ttsHelper.speak("两次输入的密码不一致")
                    }
                    else -> {
                        isRegistering = true
                        ttsHelper.speak("正在提交注册信息")
                        viewModel.register(userId, password, selectedRole, userName)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegistering
        ) {
            if (isRegistering) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("注册")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBackToLogin) {
            Text("已有账号？返回登录")
        }
    }
}