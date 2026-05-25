package com.blindrun.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.amap.api.maps.model.MyLocationStyle
import com.blindrun.app.model.Recruit
import com.blindrun.app.network.MockApiInterceptor
import com.blindrun.app.tts.TtsHelper
import com.blindrun.app.viewmodel.RunViewModel

@Composable
fun RunScreen(
    recruitId: String,
    onSosTriggered: () -> Unit,
    onFinish: () -> Unit,
    viewModel: RunViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }
    var recruit by remember { mutableStateOf<Recruit?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var mapView: MapView? by remember { mutableStateOf(null) }
    var aMap: AMap? by remember { mutableStateOf(null) }
    var startMarker: Marker? by remember { mutableStateOf(null) }
    var endMarker: Marker? by remember { mutableStateOf(null) }
    var routePolyline: Polyline? by remember { mutableStateOf(null) }

    // 加载招募数据
    LaunchedEffect(recruitId) {
        recruit = MockApiInterceptor.recruitsStore[recruitId]
        isLoading = false
        if (recruit == null) {
            ttsHelper.speak("未找到招募信息")
        } else {
            ttsHelper.speak("已加载路线，准备陪跑")
        }
    }

    // 启动定位和实时位置共享（用于 WebSocket 或模拟伙伴位置）
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.startRun(recruitId, context)
        } else {
            ttsHelper.speak("缺少位置权限，无法共享位置")
        }
    }

    // 绘制路线和起终点标记
    fun drawRouteAndMarkers() {
        val recruitData = recruit ?: return
        aMap?.clear()
        // 起点标记
        startMarker = aMap?.addMarker(
            MarkerOptions()
                .position(LatLng(recruitData.startLat, recruitData.startLng))
                .title(recruitData.startLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
        // 终点标记
        endMarker = aMap?.addMarker(
            MarkerOptions()
                .position(LatLng(recruitData.endLat, recruitData.endLng))
                .title(recruitData.endLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        // 绘制路线（直线，如需精确路线可替换为解码 polyline）
        val polylineOptions = PolylineOptions()
            .add(LatLng(recruitData.startLat, recruitData.startLng))
            .add(LatLng(recruitData.endLat, recruitData.endLng))
            .color(0xFF2196F3.toInt())
            .width(12f)
        routePolyline = aMap?.addPolyline(polylineOptions)

        // 调整视野包含起终点
        val bounds = LatLngBounds.builder()
            .include(LatLng(recruitData.startLat, recruitData.startLng))
            .include(LatLng(recruitData.endLat, recruitData.endLng))
            .build()
        aMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    // 配置带方向箭头的定位样式
    fun setupMyLocationStyle() {
        val myLocationStyle = MyLocationStyle()
        // 连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转（显示方向箭头）
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
        myLocationStyle.interval(2000) // 定位间隔（毫秒）
        myLocationStyle.showMyLocation(true)
        aMap?.myLocationStyle = myLocationStyle
        aMap?.isMyLocationEnabled = true
        aMap?.uiSettings?.isMyLocationButtonEnabled = true // 显示定位按钮（默认在右下角）
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopRun()
            mapView?.onDestroy()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            recruit == null -> {
                Text("未找到招募信息", modifier = Modifier.align(Alignment.Center))
            }
            else -> {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            mapView = this
                            onCreate(Bundle())
                            aMap = map
                            aMap?.uiSettings?.isZoomControlsEnabled = true
                            setupMyLocationStyle()       // 开启带方向箭头的定位图层
                            drawRouteAndMarkers()       // 绘制起终点和路线
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        view.onResume()
                        // 重新绘制避免因生命周期导致标记丢失
                        drawRouteAndMarkers()
                        // 定位图层会持续自动更新，无需手动干预
                    }
                )
            }
        }

        // SOS 按钮（底部中央）
        Button(
            onClick = {
                ttsHelper.speak("SOS紧急求助，已发送您的位置")
                viewModel.triggerSos(context)
                onSosTriggered()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("SOS 紧急求助", color = MaterialTheme.colorScheme.onError)
        }

        // 结束陪跑按钮（左上角，避免与右下角定位按钮重叠）
        Button(
            onClick = onFinish,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("结束陪跑")
        }
    }
}