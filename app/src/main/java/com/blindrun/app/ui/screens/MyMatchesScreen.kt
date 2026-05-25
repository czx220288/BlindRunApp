package com.blindrun.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blindrun.app.model.MatchSession
import com.blindrun.app.tts.TtsHelper
import com.blindrun.app.viewmodel.MatchViewModel

@Composable
fun MyMatchesScreen(
    userId: String,
    role: String,
    onBack: () -> Unit,
    viewModel: MatchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }
    val sessions by viewModel.sessions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        ttsHelper.speak("我的匹配列表")
        if (userId.isNotBlank()) {
            viewModel.loadMatches(userId, role)
        } else {
            ttsHelper.speak("用户ID无效，请重新登录")
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.fillMaxSize())
            error != null -> {
                Column {
                    Text("加载失败: $error")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadMatches(userId, role) }) {
                        Text("重试")
                    }
                }
            }
            sessions.isEmpty() -> Text("暂无匹配记录")
            else -> LazyColumn {
                items(sessions) { session ->
                    MatchCard(session, role)
                }
            }
        }
    }
}

@Composable
fun MatchCard(session: MatchSession, role: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (role == "blind") {
                Text("陪跑员ID: ${session.companionUserId}")
            } else {
                Text("盲人ID: ${session.blindUserId}")
            }
            Text("会话ID: ${session.sessionId}")
            Text("状态: ${if (session.active) "进行中" else "已结束"}")
        }
    }
}