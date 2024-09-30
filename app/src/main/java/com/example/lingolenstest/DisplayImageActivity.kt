package com.example.lingolenstest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.capitalize
import androidx.core.app.ActivityCompat
import com.example.lingolenstest.ui.theme.LingoLensTestTheme
import kotlin.math.max
import kotlin.math.min

class DisplayImageActivity: ComponentActivity() {

    private lateinit var yoloAPI: YoloAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageUri = intent.getStringExtra("image_uri")
        val confidenceThreshold = intent.getFloatExtra("confidence_threshold", 0.5f)
        val iouThreshold = intent.getFloatExtra("iou_threshold", 0.5f)


        yoloAPI = YoloAPI(
            context = this,
            modelFilename = "yolov5.tflite",
            confidenceThreshold = confidenceThreshold,
            iouThreshold = iouThreshold
        )

        setContent {
            LingoLensTestTheme {
                if (imageUri != null) {
                    val bitmap = loadImageFromUri(Uri.parse(imageUri))
                    if (bitmap != null) {
                        var detectedBitmap by remember { mutableStateOf(bitmap) }

                        LaunchedEffect(Unit) {
                            yoloAPI.analyze(bitmap)
                            detectedBitmap = drawBoundingBoxes(bitmap, yoloAPI.boundingBoxes)
                        }
                        DisplayImage(bitmap = detectedBitmap)
                    } else {
                        Text(text = "Couldn't open image")
                    }
                } else {
                    Text(text = "Couldn't open image")
                }
            }
        }
    }

    private fun loadImageFromUri(uri: Uri): Bitmap? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawBoundingBoxes(bitmap: Bitmap, boxes: List<BoundingBox>) : Bitmap {
        val maxWidth = bitmap.width-10
        val maxHeight = bitmap.height-10

        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.GREEN
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }

        val textPaint = Paint().apply {
            color = Color.GREEN
            textSize = 25f
            style = android.graphics.Paint.Style.FILL
            setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)  // Add shadow for better readability
        }

        for (box in boxes) {
            val minX = max(box.centerX * bitmap.width - box.width*bitmap.width/2, 0f)
            val minY = max(box.centerY * bitmap.height - box.height*bitmap.height/2, 0f)
            val maxX = min(box.centerX * bitmap.width + box.width*bitmap.width/2, maxWidth.toFloat())
            val maxY = min(box.centerY * bitmap.height + box.height*bitmap.height/2, maxHeight.toFloat())

            Log.d("Width", "Max X: ${box.centerX * bitmap.width + box.width*bitmap.width/2}, bmWidth: ${maxWidth.toFloat()}")
            Log.d("Result", "maxX = $maxX")

            val label = Labels.LABELS.get(box.classID) ?: "Unknown"
            val confidence = String.format("%.2f", box.confidence)

            val displayText = "$confidence $label"

            canvas.drawRect(minX, minY, maxX, maxY, paint)
            canvas.drawText(displayText, minX, minY-10, textPaint)
        }

        return mutableBitmap
    }
}

@Composable
fun DisplayImage(bitmap: Bitmap){
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Captured Image",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}