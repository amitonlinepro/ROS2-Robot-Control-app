package com.akm.robotcontrolapp

data class Pose2D(val x: Float, val y: Float, val theta: Float)

data class MapData(
    val width: Int,
    val height: Int,
    val resolution: Float, // Optional, e.g., 0.05
    val originX: Float,    // Map origin from metadata
    val originY: Float,
    val data: List<Int>,
    val pose: Pose2D? = null
)