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
    onAcceptClick: (Recruit) -> Unit,
    viewModel: NearbyViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }
    val recruits by viewModel.recruits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        ttsHelper.speak("附近招募列表")
        while (true) {
            viewModel.loadNearbyRecruits()
            delay(3000)
        }
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
                        RecruitCard(recruit = recruit, onAccept = {
                            viewModel.acceptRecruit(recruit) { success, session ->
                                if (success) {
                                    ttsHelper.speak("您已接单成功，即将进入跑步页面")
                                    onAcceptClick(recruit)
                                } else {
                                    ttsHelper.speak("接单失败，请重试")
                                }
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun RecruitCard(recruit: Recruit, onAccept: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("盲人: ${recruit.userName}", style = MaterialTheme.typography.titleMedium)
            Text("时间: ${Date(recruit.startTime).toLocaleString()}")
            Text("起点: ${recruit.startLocation}")
            Text("终点: ${recruit.endLocation}")
            Text("距离: ${recruit.distance}米")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onAccept) { Text("接单") }
        }
    }
}