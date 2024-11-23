package com.example.lingolens.detection

data class BoundingBox(
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float,
    val confidence: Float,
    val classID: Int
)
