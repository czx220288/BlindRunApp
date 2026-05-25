package com.blindrun.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.blindrun.app.BuildConfig
import com.blindrun.app.model.Recruit
import com.blindrun.app.service.RouteService
import com.blindrun.app.tts.TtsHelper
import com.blindrun.app.viewmodel.AuthViewModel
import com.blindrun.app.viewmodel.PublishViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*

@Composable
fun PublishRecruitScreen(
    navController: NavController,
    onPublishSuccess: () -> Unit,
    viewModel: PublishViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }
    val scope = rememberCoroutineScope()
    val routeService = remember {
        Retrofit.Builder()
            .baseUrl("https://restapi.amap.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RouteService::class.java)
    }

    // 状态
    var startLocation by remember { mutableStateOf<Pair<String, LatLng>?>(null) }
    var endLocation by remember { mutableStateOf<Pair<String, LatLng>?>(null) }
    var pickingState by remember { mutableStateOf("start") }
    var routeDistance by remember { mutableStateOf(0) }
    var isCalculating by remember { mutableStateOf(false) }
    var timeInMinutes by remember { mutableStateOf(60L) }
    var isPublishing by remember { mutableStateOf(false) }
    var mapView: MapView? by remember { mutableStateOf(null) }
    var aMap: AMap? by remember { mutableStateOf(null) }
    var startMarker: Marker? by remember { mutableStateOf(null) }
    var endMarker: Marker? by remember { mutableStateOf(null) }
    var routePolyline: Polyline? by remember { mutableStateOf(null) }
    var myLocationMarker: Marker? by remember { mutableStateOf(null) }
    var searchText by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Address>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // 逆地理编码
    suspend fun getAddressFromLatLng(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.CHINA)
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (addresses.isNullOrEmpty()) "所选位置" else addresses[0].getAddressLine(0) ?: "所选位置"
        } catch (e: Exception) {
            "经度:$lng,纬度:$lat"
        }
    }

    // 解码 polyline
    fun decodePolyline(polylineStr: String): List<LatLng> {
        val points = mutableListOf<LatLng>()
        val pairs = polylineStr.split(";")
        for (pair in pairs) {
            val coords = pair.split(",")
            if (coords.size == 2) {
                val lng = coords[0].toDoubleOrNull()
                val lat = coords[1].toDoubleOrNull()
                if (lng != null && lat != null) points.add(LatLng(lat, lng))
            }
        }
        return points
    }

    // 规划路线
    suspend fun calculateRoute() {
        if (startLocation == null || endLocation == null) return
        isCalculating = true
        try {
            val origin = "${startLocation!!.second.longitude},${startLocation!!.second.latitude}"
            val dest = "${endLocation!!.second.longitude},${endLocation!!.second.latitude}"
            val response = routeService.getWalkingRoute(origin, dest, BuildConfig.AMAP_WEB_KEY)
            if (response.isSuccessful && response.body()?.status == "1") {
                val path = response.body()?.route?.paths?.firstOrNull()
                if (path != null && path.distance > 0) {
                    routeDistance = path.distance
                    path.steps?.firstOrNull()?.polyline?.let { polylineStr ->
                        val points = decodePolyline(polylineStr)
                        routePolyline?.remove()
                        routePolyline = aMap?.addPolyline(
                            PolylineOptions().addAll(points).color(0xFF2196F3.toInt()).width(12f)
                        )
                    }
                    ttsHelper.speak("路线规划成功，距离 ${routeDistance}米")
                    val bounds = LatLngBounds.builder()
                        .include(startLocation!!.second)
                        .include(endLocation!!.second)
                        .build()
                    aMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                } else {
                    ttsHelper.speak("未能规划路线")
                }
            } else {
                ttsHelper.speak("路线规划失败")
            }
        } catch (e: Exception) {
            ttsHelper.speak("网络错误，无法规划路线")
        } finally {
            isCalculating = false
        }
    }

    // 自动定位并显示蓝点，同时设为起点（如果未设置）
    fun startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return
        val locationClient = AMapLocationClient(context)
        locationClient.setLocationListener { location ->
            if (location != null && location.errorCode == 0) {
                val latLng = LatLng(location.latitude, location.longitude)
                if (myLocationMarker == null) {
                    myLocationMarker = aMap?.addMarker(
                        MarkerOptions().position(latLng).title("我的位置")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )
                } else {
                    myLocationMarker?.position = latLng
                }
                aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                if (startLocation == null) {
                    scope.launch {
                        val address = getAddressFromLatLng(latLng.latitude, latLng.longitude)
                        startMarker?.remove()
                        startMarker = aMap?.addMarker(
                            MarkerOptions().position(latLng).title(address)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        )
                        startLocation = Pair(address, latLng)
                        ttsHelper.speak("起点已设为当前定位：$address")
                    }
                }
                locationClient.stopLocation()
                locationClient.onDestroy()
            }
        }
        locationClient.startLocation()
    }

    // 搜索地址
    fun searchAddress(query: String) {
        if (query.isBlank()) return
        scope.launch {
            isSearching = true
            val results = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.CHINA)
                    geocoder.getFromLocationName(query, 10) ?: emptyList()
                } catch (e: IOException) {
                    emptyList()
                }
            }
            isSearching = false
            searchResults = results
            if (results.isNotEmpty()) {
                val first = results.first()
                val latLng = LatLng(first.latitude, first.longitude)
                aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                val addr = first.getAddressLine(0) ?: query
                if (pickingState == "start") {
                    startMarker?.remove()
                    startMarker = aMap?.addMarker(
                        MarkerOptions().position(latLng).title(addr)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                    startLocation = Pair(addr, latLng)
                    ttsHelper.speak("起点已设为 $addr")
                } else {
                    endMarker?.remove()
                    endMarker = aMap?.addMarker(
                        MarkerOptions().position(latLng).title(addr)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )
                    endLocation = Pair(addr, latLng)
                    ttsHelper.speak("终点已设为 $addr")
                }
                if (startLocation != null && endLocation != null) calculateRoute()
            } else {
                ttsHelper.speak("未找到相关地点")
            }
        }
    }

    // 地图长按监听
    fun setupMapListeners() {
        aMap?.setOnMapLongClickListener { latLng ->
            scope.launch {
                val address = getAddressFromLatLng(latLng.latitude, latLng.longitude)
                if (pickingState == "start") {
                    startMarker?.remove()
                    startMarker = aMap?.addMarker(
                        MarkerOptions().position(latLng).title(address)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                    startLocation = Pair(address, latLng)
                    ttsHelper.speak("起点已设置")
                } else {
                    endMarker?.remove()
                    endMarker = aMap?.addMarker(
                        MarkerOptions().position(latLng).title(address)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )
                    endLocation = Pair(address, latLng)
                    ttsHelper.speak("终点已设置")
                }
                if (startLocation != null && endLocation != null) calculateRoute()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            startLocationUpdate()
        } else {
            ttsHelper.speak("位置权限被拒绝，无法自动定位起点")
        }
    }

    val currentUserId = authViewModel.currentUserId ?: ""
    val currentUserName = authViewModel.currentUserName ?: "当前用户"

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdate()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
        ttsHelper.speak("发布招募页面，请选择起点和终点")
    }

    LaunchedEffect(startLocation, endLocation) {
        if (startLocation != null && endLocation != null) calculateRoute()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部工具栏
        Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 4.dp) {
            Column {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = { pickingState = "start" },
                        colors = if (pickingState == "start") ButtonDefaults.buttonColors()
                        else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(if (pickingState == "start") "● 正在选起点" else "○ 选起点")
                    }
                    Button(
                        onClick = { pickingState = "end" },
                        colors = if (pickingState == "end") ButtonDefaults.buttonColors()
                        else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(if (pickingState == "end") "● 正在选终点" else "○ 选终点")
                    }
                }
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("搜索地点") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(onClick = { searchAddress(searchText) }, enabled = !isSearching) {
                        if (isSearching) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        else Text("搜索")
                    }
                }
                if (searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.height(150.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(searchResults) { address ->
                            val addr = address.getAddressLine(0) ?: "未知地点"
                            TextButton(onClick = { searchAddress(addr) }, modifier = Modifier.fillMaxWidth()) {
                                Text(addr)
                            }
                        }
                    }
                }
            }
        }

        // 地图视图
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapView = this
                    onCreate(Bundle())
                    aMap = map
                    aMap?.uiSettings?.isZoomControlsEnabled = true
                    setupMapListeners()
                }
            },
            modifier = Modifier.weight(1f),
            update = { view -> view.onResume() }
        )

        // 底部信息栏
        Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 8.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (startLocation != null) Text("起点: ${startLocation!!.first}")
                if (endLocation != null) Text("终点: ${endLocation!!.first}")
                if (routeDistance > 0) Text("路线距离: ${routeDistance}米", color = MaterialTheme.colorScheme.primary)
                else if (isCalculating) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = timeInMinutes.toString(),
                    onValueChange = { timeInMinutes = it.toLongOrNull() ?: 60 },
                    label = { Text("开始时间（分钟后）") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (startLocation == null || endLocation == null) {
                            ttsHelper.speak("请先选择起点和终点")
                            return@Button
                        }
                        if (routeDistance == 0 && !isCalculating) {
                            ttsHelper.speak("请等待路线规划完成")
                            return@Button
                        }
                        scope.launch {
                            isPublishing = true
                            val recruit = Recruit(
                                id = "",
                                userId = currentUserId,
                                userName = currentUserName,
                                startTime = System.currentTimeMillis() + timeInMinutes * 60 * 1000,
                                startLocation = startLocation!!.first,
                                startLat = startLocation!!.second.latitude,
                                startLng = startLocation!!.second.longitude,
                                endLocation = endLocation!!.first,
                                endLat = endLocation!!.second.latitude,
                                endLng = endLocation!!.second.longitude,
                                distance = routeDistance,
                                status = "active"
                            )
                            val result = viewModel.publishRecruit(recruit)
                            isPublishing = false
                            if (result.isSuccess) {
                                ttsHelper.speak("发布成功")
                                Toast.makeText(context, "发布成功", Toast.LENGTH_SHORT).show()
                                onPublishSuccess()
                            } else {
                                val errorMsg = result.exceptionOrNull()?.message ?: "发布失败"
                                ttsHelper.speak("发布失败")
                                Toast.makeText(context, "发布失败: $errorMsg", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = startLocation != null && endLocation != null && routeDistance > 0 && !isPublishing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isPublishing) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("发布")
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { mapView?.onDestroy() }
    }
}