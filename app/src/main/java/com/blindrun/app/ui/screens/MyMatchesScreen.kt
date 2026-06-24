package com.blindrun.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blindrun.app.model.MatchSession
import com.blindrun.app.tts.TtsHelper
import com.blindrun.app.viewmodel.MatchViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyMatchesScreen(
    userId: String,
    userRole: String,
    onStartMatch: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: MatchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }
    var lastClickTime by remember { mutableStateOf(0L) }
    
    val sessions by viewModel.sessions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // 分离待开始和进行中的订单（移到前面）
    val pendingSessions = sessions.filter { it.status == "pending" }
    val activeSessions = sessions.filter { it.status == "active" }
    val historySessions = sessions.filter { it.status == "completed" || it.status == "cancelled" }
    
    LaunchedEffect(userId, userRole) {
        android.util.Log.d("MyMatches", "加载匹配列表: userId=$userId, role=$userRole")
        viewModel.loadMatches(userId, userRole)
        
        // 页面进入时播放语音提示
        ttsHelper.speak("我的匹配页面，待开始${pendingSessions.size}个，进行中${activeSessions.size}个")
    }
    
    // 监听订单状态更新通知
    LaunchedEffect(Unit) {
        viewModel.webSocketManager.notificationFlow.collect { notification ->
            val type = notification["type"]
            val sessionId = notification["sessionId"]
            if (type == "match_status_updated" && !sessionId.isNullOrEmpty()) {
                android.util.Log.d("MyMatches", "收到状态更新通知，刷新列表")
                viewModel.loadMatches(userId, userRole)
            }
        }
    }
    
    // 处理点击事件：单击TTS，双击执行（仅盲人端）
    fun handleClick(singleTapText: String, doubleTapAction: () -> Unit) {
        if (userRole != "blind") {
            // 陪跑者直接执行
            doubleTapAction()
            return
        }
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < 300) {
            // 双击（300ms内再次点击）
            doubleTapAction()
        } else {
            // 单击，播放TTS
            ttsHelper.speak(singleTapText)
        }
        lastClickTime = currentTime
    }
    
    LaunchedEffect(sessions) {
        android.util.Log.d("MyMatches", "收到 ${sessions.size} 个订单")
        sessions.forEach {
            android.util.Log.d("MyMatches", "  - SessionId: ${it.sessionId}, Status: ${it.status}, Active: ${it.active}")
        }
        android.util.Log.d("MyMatches", "待开始: ${pendingSessions.size}, 进行中: ${activeSessions.size}, 历史: ${historySessions.size}")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部标题
        Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 4.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("我的匹配", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("待开始: ${pendingSessions.size} | 进行中: ${activeSessions.size}", 
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 待开始的订单
            if (pendingSessions.isNotEmpty()) {
                item {
                    Text("待开始订单", 
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp))
                }
                
                items(pendingSessions) { session ->
                    PendingMatchCard(
                        session = session,
                        userRole = userRole,
                        ttsHelper = ttsHelper,
                        onStart = { sessionId ->
                            viewModel.startMatch(sessionId, userId, userRole) { success ->
                                if (success) {
                                    ttsHelper.speak("已开始陪跑")
                                    viewModel.loadMatches(userId, userRole)
                                }
                            }
                        },
                        onCancel = { sessionId ->
                            viewModel.cancelMatch(sessionId, userId) { success ->
                                if (success) {
                                    ttsHelper.speak("已取消订单")
                                    viewModel.loadMatches(userId, userRole)
                                }
                            }
                        }
                    )
                }
            }
            
            // 进行中的订单
            if (activeSessions.isNotEmpty()) {
                item {
                    Text("进行中订单", 
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp))
                }
                
                items(activeSessions) { session ->
                    ActiveMatchCard(
                        session = session,
                        userRole = userRole,
                        ttsHelper = ttsHelper,
                        onStartRun = { recruitId ->
                            onStartMatch(recruitId)
                        }
                    )
                }
            }
            
            // 历史订单
            if (historySessions.isNotEmpty()) {
                item {
                    Text("历史订单", 
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp))
                }
                
                items(historySessions) { session ->
                    HistoryMatchCard(session = session)
                }
            }
            
            // 空状态
            if (sessions.isEmpty() && !isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无匹配记录", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun PendingMatchCard(
    session: MatchSession,
    userRole: String,
    ttsHelper: TtsHelper,
    onStart: (String) -> Unit,
    onCancel: (String) -> Unit
) {
    var lastClickTime by remember { mutableStateOf(0L) }
    
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
    val startTimeStr = if (session.startTime > 0) {
        dateFormat.format(Date(session.startTime))
    } else {
        "未知时间"
    }
    
    val isConfirmed = if (userRole == "blind") session.blindConfirmed else session.companionConfirmed
    val partnerConfirmed = if (userRole == "blind") session.companionConfirmed else session.blindConfirmed
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("预约时间: $startTimeStr", style = MaterialTheme.typography.titleMedium)
                if (isConfirmed) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("已确认", 
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                if (partnerConfirmed) "对方已确认，可以开始了" else "等待对方确认...",
                style = MaterialTheme.typography.bodyMedium,
                color = if (partnerConfirmed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (userRole == "blind") {
                    // 盲人端：双击逻辑
                    OutlinedButton(
                        onClick = {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastClickTime < 300) {
                                onCancel(session.sessionId)
                            } else {
                                ttsHelper.speak("取消订单，双击确认取消")
                            }
                            lastClickTime = currentTime
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消订单")
                    }
                    
                    var startLastClickTime by remember { mutableStateOf(0L) }
                    Button(
                        onClick = {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - startLastClickTime < 300) {
                                onStart(session.sessionId)
                            } else {
                                ttsHelper.speak("开始陪跑，双击确认开始")
                            }
                            startLastClickTime = currentTime
                        },
                        enabled = partnerConfirmed,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isConfirmed) "开始陪跑" else "确认并开始")
                    }
                } else {
                    // 陪跑者端：直接执行
                    OutlinedButton(
                        onClick = { onCancel(session.sessionId) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消订单")
                    }
                    
                    Button(
                        onClick = { onStart(session.sessionId) },
                        enabled = partnerConfirmed,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isConfirmed) "开始陪跑" else "确认并开始")
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveMatchCard(
    session: MatchSession,
    userRole: String,
    ttsHelper: TtsHelper,
    onStartRun: (String) -> Unit
) {
    var lastClickTime by remember { mutableStateOf(0L) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("进行中", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (userRole == "blind") {
                // 盲人端：双击逻辑
                Button(
                    onClick = {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastClickTime < 300) {
                            onStartRun(session.recruitId)
                        } else {
                            ttsHelper.speak("进入跑步页面，双击确认")
                        }
                        lastClickTime = currentTime
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("进入跑步页面")
                }
            } else {
                // 陪跑者端：直接执行
                Button(
                    onClick = { onStartRun(session.recruitId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("进入跑步页面")
                }
            }
        }
    }
}

@Composable
fun HistoryMatchCard(
    session: MatchSession,
    viewModel: MatchViewModel = hiltViewModel()
) {
    val activeRecruits by viewModel.activeRecruits.collectAsState()
    val recruit = activeRecruits.find { it.id == session.recruitId }
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    val startTimeStr = if (session.startTime > 0) {
        dateFormat.format(Date(session.startTime))
    } else {
        "未知时间"
    }
    
    val statusText = when (session.status) {
        "completed" -> "已完成"
        "cancelled" -> "已取消"
        else -> session.status
    }
    
    val statusColor = when (session.status) {
        "completed" -> MaterialTheme.colorScheme.primary
        "cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "订单状态: $statusText",
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor
                )
                
                Text(
                    startTimeStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            recruit?.let { r ->
                DetailRow("发布者", r.userName ?: "未知")
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("起点", r.startLocation ?: "未知")
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("终点", r.endLocation ?: "未知")
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("距离", "${r.distance ?: 0}米")
            }
            
            if (session.blindConfirmed || session.companionConfirmed) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (session.blindConfirmed) {
                        StatusChip("盲人已确认")
                    }
                    if (session.companionConfirmed) {
                        StatusChip("陪跑者已确认")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StatusChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
