package com.blindrun.app.model

data class RouteResponse(
    val status: String,
    val info: String,
    val route: Route?
)

data class Route(
    val paths: List<Path>?
)

data class Path(
    val distance: Int,
    val duration: Int,
    val steps: List<Step>?
)

data class Step(
    val polyline: String
)