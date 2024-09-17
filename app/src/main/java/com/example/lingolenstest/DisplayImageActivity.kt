package com.example.lingolenstest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
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

class DisplayImageActivity: ComponentActivity() {

    private lateinit var yoloAPI: YoloAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageUri = intent.getStringExtra("image_uri")

        yoloAPI = YoloAPI(
            context = this,
            modelFilename = "yolov5.tflite",
            confidenceThreshold = 0.5f,
            iouThreshold = 0.5f
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
            val minX = box.centerX * bitmap.width - box.width*bitmap.width/2
            val minY = box.centerY * bitmap.height - box.height*bitmap.height/2
            val maxX = box.centerX * bitmap.width + box.width*bitmap.width/2
            val maxY = box.centerY * bitmap.height + box.height*bitmap.height/2


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