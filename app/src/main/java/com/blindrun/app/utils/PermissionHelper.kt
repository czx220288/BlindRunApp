package com.blindrun.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 检查并请求位置权限（传统方式，用于 Activity）
 */
fun checkAndRequestPermissions(activity: Activity, requestCode: Int): Boolean {
    val permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val notGranted = permissions.filter {
        ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
    }
    if (notGranted.isNotEmpty()) {
        ActivityCompat.requestPermissions(activity, notGranted.toTypedArray(), requestCode)
        return false
    }
    return true
}

/**
 * Compose 权限请求组合函数（使用 rememberLauncherForActivityResult）
 * @param onGranted 权限授予后的回调
 * @param onDenied 权限拒绝后的回调
 */
@Composable
fun RequestLocationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit = {}
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            onGranted()
        } else {
            onDenied()
        }
    }

    // 检查是否已有权限
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (hasPermission) {
        onGranted()
    } else {
        // 提示并请求权限
        AlertDialog(
            onDismissRequest = { onDenied() },
            title = { Text("需要位置权限") },
            text = { Text("助盲跑需要获取您的位置信息，用于匹配附近跑友和共享实时位置。") },
            confirmButton = {
                Button(
                    onClick = {
                        launcher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text("授予权限")
                }
            },
            dismissButton = {
                Button(onClick = { onDenied() }) {
                    Text("取消")
                }
            }
        )
    }
}