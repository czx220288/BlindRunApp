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
import com.blindrun.app.model.Recruit
import com.blindrun.app.tts.TtsHelper
import com.blindrun.app.viewmodel.NearbyViewModel
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun NearbyScreen(
    userId: String,
    onAcceptClick: (Recruit) -> Unit,
    viewModel: NearbyViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }
    val recruits by viewModel.recruits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var lastClickTime by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        ttsHelper.speak("附近招募列表")
        while (true) {
            viewModel.loadNearbyRecruits()
            delay(3000)
        }
    }
    
    // 处理点击事件：单击TTS，双击执行（仅盲人端）
    fun handleClick(singleTapText: String, doubleTapAction: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < 300) {
            // 双击
            doubleTapAction()
        } else {
            // 单击，播放TTS
            ttsHelper.speak(singleTapText)
        }
        lastClickTime = currentTime
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading && recruits.isEmpty() -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            error != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("加载失败: $error")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadNearbyRecruits() }) { Text("重试") }
                }
            }
            recruits.isEmpty() -> Text("暂无附近招募", modifier = Modifier.align(Alignment.Center))
            else -> {
                LazyColumn {
                    items(recruits) { recruit ->
                        RecruitCard(
                            recruit = recruit,
                            onAccept = {
                                handleClick(
                                    "选择${recruit.userName}的招募，从${recruit.startLocation}到${recruit.endLocation}，距离${recruit.distance}米，双击确认接单",
                                    {
                                        viewModel.acceptRecruit(recruit, userId) { success, session ->
                                            if (success) {
                                                ttsHelper.speak("您已接单成功")
                                            } else {
                                                ttsHelper.speak("接单失败，请重试")
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecruitCard(recruit: Recruit, onAccept: () -> Unit) {
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }
    val dateFormat = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.CHINA)
    val startTimeStr = if (recruit.startTime > 0) {
        dateFormat.format(java.util.Date(recruit.startTime))
    } else {
        "未知时间"
    }
    
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("盲人: ${recruit.userName}", style = MaterialTheme.typography.titleMedium)
            Text("时间: $startTimeStr")
            Text("起点: ${recruit.startLocation}")
            Text("终点: ${recruit.endLocation}")
            Text("距离: ${recruit.distance}米")
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    ttsHelper.speak("已选择${recruit.userName}的招募，从${recruit.startLocation}到${recruit.endLocation}，距离${recruit.distance}米")
                    onAccept()
                }
            ) { 
                Text("接单") 
            }
        }
    }
}