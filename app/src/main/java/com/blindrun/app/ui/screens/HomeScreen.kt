package com.blindrun.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.util.Log
import com.blindrun.app.tts.TtsHelper

@Composable
fun HomeScreen(
    role: String,
    onNavigateToNearby: () -> Unit,
    onNavigateToPublish: () -> Unit,
    onNavigateToMyMatches: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }
    
    // 页面进入时播报（延迟确保TTS就绪）
    LaunchedEffect(role) {
        kotlinx.coroutines.delay(500) // 等待TTS初始化
        ttsHelper.speak(if (role == "blind") "盲人首页，您可以发布陪跑需求或查看我的匹配" else "陪跑员首页，您可以查看附近招募或我的匹配")
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("首页", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        if (role == "blind") {
            // 盲人端：使用自定义双击逻辑
            BlindButton(
                text = "发布陪跑招募",
                ttsText = "发布招募，双击进入发布页面",
                onClick = onNavigateToPublish,
                ttsHelper = ttsHelper,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // 陪跑者端：普通单击
            Button(
                onClick = {
                    Log.d("HomeScreen", "陪跑者点击：查看附近招募")
                    ttsHelper.speak("查看附近招募")
                    onNavigateToNearby()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("查看附近招募")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (role == "blind") {
            BlindButton(
                text = "我的匹配",
                ttsText = "我的匹配，双击查看订单",
                onClick = onNavigateToMyMatches,
                ttsHelper = ttsHelper,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Button(
                onClick = {
                    Log.d("HomeScreen", "陪跑者点击：我的匹配")
                    ttsHelper.speak("我的匹配")
                    onNavigateToMyMatches()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("我的匹配")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (role == "blind") {
            BlindButton(
                text = "退出登录",
                ttsText = "退出登录，双击确认退出",
                onClick = onLogout,
                ttsHelper = ttsHelper,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Button(
                onClick = {
                    Log.d("HomeScreen", "陪跑者点击：退出登录")
                    ttsHelper.speak("退出登录")
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("退出登录")
            }
        }
    }
}

/**
 * 盲人专用按钮：单击播放TTS，双击执行操作
 */
@Composable
fun BlindButton(
    text: String,
    ttsText: String,
    onClick: () -> Unit,
    ttsHelper: TtsHelper,
    modifier: Modifier = Modifier
) {
    var lastClickTime by remember { mutableStateOf(0L) }
    
    Button(
        onClick = {
            val currentTime = System.currentTimeMillis()
            val timeDiff = currentTime - lastClickTime
            
            Log.d("BlindButton", "点击: $text, 时间间隔: ${timeDiff}ms")
            
            if (timeDiff < 300 && timeDiff > 0) {
                // 双击：执行操作
                Log.d("BlindButton", "双击检测到，执行: $onClick")
                onClick()
            } else {
                // 单击：播放TTS
                Log.d("BlindButton", "单击检测到，播报: $ttsText")
                ttsHelper.speak(ttsText)
            }
            lastClickTime = currentTime
        },
        modifier = modifier
    ) {
        Text(text)
    }
}
