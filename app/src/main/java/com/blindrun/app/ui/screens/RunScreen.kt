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
import com.blindrun.app.model.MatchFinishRequest
import com.blindrun.app.model.SosRequest
import com.blindrun.app.network.ApiService
import com.blindrun.app.tts.TtsHelper
import com.blindrun.app.viewmodel.RunViewModel
import kotlinx.coroutines.launch

@Composable
fun RunScreen(
    recruitId: String,
    onSosTriggered: () -> Unit,
    onFinish: () -> Unit,
    viewModel: RunViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ttsHelper = remember { TtsHelper(context) }
    val recruit by viewModel.recruit.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val myLocation by viewModel.myLocation.collectAsState()
    val partnerLocation by viewModel.partnerLocation.collectAsState()
    var mapView: MapView? by remember { mutableStateOf(null) }
    var aMap: AMap? by remember { mutableStateOf(null) }
    var startMarker: Marker? by remember { mutableStateOf(null) }
    var endMarker: Marker? by remember { mutableStateOf(null) }
    var routePolyline: Polyline? by remember { mutableStateOf(null) }
    var partnerMarker: Marker? by remember { mutableStateOf(null) }

    LaunchedEffect(recruitId) {
        viewModel.loadRecruit(recruitId)
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            viewModel.startRun(recruitId, context)
        } else {
            ttsHelper.speak("缺少位置权限，无法共享位置")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.notificationFlow.collect { notification ->
            val type = notification["type"]
            when (type) {
                "match_cancelled" -> {
                    ttsHelper.speak("订单已被取消")
                    onFinish()
                }
                "match_finished" -> {
                    ttsHelper.speak("订单已完成")
                    onFinish()
                }
            }
        }
    }

    fun drawRouteAndMarkers() {
        val recruitData = recruit ?: return
        aMap?.clear()
        startMarker = aMap?.addMarker(
            MarkerOptions()
                .position(LatLng(recruitData.startLat, recruitData.startLng))
                .title(recruitData.startLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
        endMarker = aMap?.addMarker(
            MarkerOptions()
                .position(LatLng(recruitData.endLat, recruitData.endLng))
                .title(recruitData.endLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        val polylineOptions = PolylineOptions()
            .add(LatLng(recruitData.startLat, recruitData.startLng))
            .add(LatLng(recruitData.endLat, recruitData.endLng))
            .color(0xFF2196F3.toInt())
            .width(12f)
        routePolyline = aMap?.addPolyline(polylineOptions)
        val bounds = LatLngBounds.builder()
            .include(LatLng(recruitData.startLat, recruitData.startLng))
            .include(LatLng(recruitData.endLat, recruitData.endLng))
            .build()
        aMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    fun setupMyLocationStyle() {
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
        myLocationStyle.interval(2000)
        myLocationStyle.showMyLocation(true)
        aMap?.myLocationStyle = myLocationStyle
        aMap?.isMyLocationEnabled = true
        aMap?.uiSettings?.isMyLocationButtonEnabled = true
    }

    fun updatePartnerMarker() {
        partnerLocation?.let { loc ->
            if (partnerMarker == null) {
                partnerMarker = aMap?.addMarker(
                    MarkerOptions()
                        .position(LatLng(loc.latitude, loc.longitude))
                        .title("陪跑伙伴")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
            } else {
                partnerMarker?.position = LatLng(loc.latitude, loc.longitude)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopRun()
            mapView?.onDestroy()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            recruit == null -> Text("未找到招募信息", modifier = Modifier.align(Alignment.Center))
            else -> AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapView = this
                        onCreate(Bundle())
                        aMap = map
                        aMap?.uiSettings?.isZoomControlsEnabled = true
                        setupMyLocationStyle()
                        drawRouteAndMarkers()
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    view.onResume()
                    drawRouteAndMarkers()
                    updatePartnerMarker()
                }
            )
        }

        Button(
            onClick = {
                scope.launch {
                    try {
                        ttsHelper.speak("SOS紧急求助")
                        
                        val sessionResponse = viewModel.apiService.getMatchSessionByRecruitId(recruitId)
                        
                        if (sessionResponse.isSuccessful && sessionResponse.body() != null) {
                            val session = sessionResponse.body()!!
                            
                            val currentLocation = myLocation
                            if (currentLocation != null) {
                                val sosRequest = SosRequest(
                                    userId = "user_${System.currentTimeMillis()}",
                                    latitude = currentLocation.latitude,
                                    longitude = currentLocation.longitude,
                                    sessionId = session.sessionId,
                                    recruitId = session.recruitId,
                                    companionUserId = session.companionUserId,
                                    blindUserId = session.blindUserId,
                                    sessionStatus = session.status
                                )
                                
                                val sosResponse = viewModel.apiService.triggerSos(sosRequest)
                                
                                if (sosResponse.isSuccessful) {
                                    ttsHelper.speak("SOS已发送")
                                    onSosTriggered()
                                } else {
                                    ttsHelper.speak("SOS发送失败")
                                }
                            } else {
                                ttsHelper.speak("无法获取位置")
                            }
                        } else {
                            ttsHelper.speak("未找到订单")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ttsHelper.speak("SOS发送失败")
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("SOS 紧急求助", color = MaterialTheme.colorScheme.onError)
        }

        Button(
            onClick = {
                scope.launch {
                    try {
                        println("结束陪跑 - recruitId: $recruitId")
                        val finishRequest = MatchFinishRequest()
                        finishRequest.sessionId = recruitId
                        val response = viewModel.apiService.finishMatch(finishRequest)
                        println("结束陪跑 - 响应码: ${response.code()}")
                        
                        when (response.code()) {
                            200 -> {
                                ttsHelper.speak("陪跑已结束")
                                onFinish()
                            }
                            400 -> {
                                ttsHelper.speak("订单已完成或已取消")
                                onFinish()
                            }
                            404 -> {
                                ttsHelper.speak("未找到订单")
                            }
                            else -> {
                                ttsHelper.speak("结束失败，请重试")
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("结束陪跑 - 异常: ${e.message}")
                        ttsHelper.speak("网络错误: ${e.message}")
                    }
                }
            },
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Text("结束陪跑")
        }
    }

    LaunchedEffect(partnerLocation) {
        updatePartnerMarker()
    }
}