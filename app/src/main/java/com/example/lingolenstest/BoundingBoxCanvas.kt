package com.example.lingolenstest

import android.graphics.Rect
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
//
//@Composable
//fun BoundingBoxCanvas(
//    modifier: Modifier = Modifier,
//    boxes: List<BoundingBox>, // List of bounding boxes to draw
//    imageSize: Size // Size of the image to scale the boxes accordingly
//) {
//    Box(modifier = modifier) {
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            drawBoundingBoxes(boxes, imageSize)
//        }
//    }
//}
//
//fun DrawScope.drawBoundingBoxes(boxes: List<BoundingBox>, imageSize: Size) {
//    Log.d("Number of boxes", "Number of boxes on canvas: ${boxes.count()}")
//    for (box in boxes) {
//        val topX = box.centerX * imageSize.width - box.centerX*imageSize.width/2
//        val topY = box.centerY * imageSize.height - box.centerY*imageSize.height/2
//        val boxWidth = box.width * imageSize.width
//        val boxHeight = box.height * imageSize.height
//
//        Log.d("Box data", "X: ${topX} Y: ${topY} Width: ${boxWidth} Height: ${boxHeight} Confidence: ${box.confidence} Class: ${box.classId}")
//
//        drawRect(
//            color = Color.Red,
//            topLeft = Offset(topX, topY),
//            size = Size(boxWidth, boxHeight),
//            style = Stroke(width = 2.dp.toPx())
//        )
//    }
//}