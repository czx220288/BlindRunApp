package com.blindrun.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blindrun.app.tts.TtsHelper
import com.blindrun.app.viewmodel.SosViewModel

@Composable
fun SosScreen(
    userId: String = "current_user_id", // 实际应从登录状态获取
    onBack: () -> Unit,
    viewModel: SosViewModel = viewModel()
) {
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }
    val isSending by viewModel.isSending.collectAsState()
    val sendResult by viewModel.sendResult.collectAsState()

    // 页面进入时重置状态并播放语音
    LaunchedEffect(Unit) {
        viewModel.resetResult()
        ttsHelper.speak("SOS紧急求助页面，即将发送您的位置")
        // 自动触发 SOS
        viewModel.triggerSos(
            context = context,
            userId = userId,
            onSuccess = {
                ttsHelper.speak("求助已发送，请保持冷静")
            },
            onError = { error ->
                ttsHelper.speak("发送失败，请检查网络或手动重试")
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("SOS 紧急求助", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        when {
            isSending -> {
                CircularProgressIndicator()
                Text("正在发送您的位置...")
            }
            sendResult == true -> {
                Text("您的实时位置已发送给紧急联系人及平台", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("返回首页")
                }
            }
            sendResult == false -> {
                Text("发送失败，请点击重试", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    viewModel.triggerSos(context, userId)
                }) {
                    Text("重试")
                }
            }
            else -> {
                Text("正在准备...")
            }
        }
    }
}