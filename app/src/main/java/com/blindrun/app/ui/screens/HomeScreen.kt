package com.blindrun.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    LaunchedEffect(Unit) {
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
            Button(
                onClick = {
                    ttsHelper.speak("发布招募")
                    onNavigateToPublish()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("发布陪跑招募")
            }
        } else {
            Button(
                onClick = {
                    ttsHelper.speak("查看附近招募")
                    onNavigateToNearby()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("查看附近招募")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                ttsHelper.speak("我的匹配")
                onNavigateToMyMatches()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("我的匹配")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                ttsHelper.speak("退出登录")
                onLogout()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("退出登录")
        }
    }
}