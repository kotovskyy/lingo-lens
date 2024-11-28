package com.example.lingolens.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.res.stringResource
import com.example.lingolens.R
import com.example.lingolens.detection.BoundingBox
import com.example.lingolens.detection.YoloAPI
import com.example.lingolens.getSelectedLanguage
import com.example.lingolens.translateAPI.LabelTranslator
import com.example.lingolens.ui.theme.LingoLensTheme
import kotlin.math.max
import kotlin.math.min

class DisplayImageActivity: AppCompatActivity() {

    private lateinit var yoloAPI: YoloAPI
    private lateinit var selectedLanguageCode: String
    private lateinit var imageUri: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageUri = intent.getStringExtra("image_uri").toString()
        val confidenceThreshold = intent.getFloatExtra("confidence_threshold", 0.5f)
        val iouThreshold = intent.getFloatExtra("iou_threshold", 0.5f)

        selectedLanguageCode = getSelectedLanguage(this).toString()

        val labelsTranslator = LabelTranslator(this)

        yoloAPI = YoloAPI(
            context = this,
            modelFilename = "yolov5.tflite",
            confidenceThreshold = confidenceThreshold,
            iouThreshold = iouThreshold
        )

        setContent {
            LingoLensTheme {
                val bitmap = loadImageFromUri(Uri.parse(imageUri))
                if (bitmap != null) {
                    ResultScreen(
                        bitmap = bitmap,
                        yoloAPI = yoloAPI,
                        labelsTranslator = labelsTranslator,
                        selectedLanguageCode = selectedLanguageCode
                    )
                } else {
                    ErrorScreen(text = stringResource(id = R.string.cant_open_image))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearCache(this)
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

    private fun clearCache(context: Context) {
        val cacheDir = context.cacheDir
        cacheDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("captured_image") && file.name.endsWith(".jpg")) {
                file.delete()
            }
        }
    }
}

fun drawBoundingBoxes(bitmap: Bitmap, boxes: List<BoundingBox>, labels: List<String>) : Bitmap {
    val maxWidth = bitmap.width-10
    val maxHeight = bitmap.height-10

    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)
    val paint = Paint().apply {
        color = android.graphics.Color.GREEN
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true // Smoother lines
        pathEffect = CornerPathEffect(15f) // Rounded corners for the bounding boxes
    }

    // Paint for filled boxes
    val fillPaint = Paint().apply {
        color = android.graphics.Color.GREEN
        alpha = 40 // transparency (0-255)
        style = Paint.Style.FILL
    }

    val textPaint = Paint().apply {
        color = android.graphics.Color.GREEN
        textSize = 25f
        style = android.graphics.Paint.Style.FILL
        setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)  // Add shadow for better readability
    }

    for (i in 0..<boxes.count()){
        val minX = max(boxes[i].centerX * bitmap.width - boxes[i].width*bitmap.width/2, 0f)
        val minY = max(boxes[i].centerY * bitmap.height - boxes[i].height*bitmap.height/2, 0f)
        val maxX = min(boxes[i].centerX * bitmap.width + boxes[i].width*bitmap.width/2, maxWidth.toFloat())
        val maxY = min(boxes[i].centerY * bitmap.height + boxes[i].height*bitmap.height/2, maxHeight.toFloat())

        val label = labels[i]
        val confidence = String.format("%.2f", boxes[i].confidence)

        val displayText = "$confidence $label"

        canvas.drawRect(minX, minY, maxX, maxY, paint)
        canvas.drawRect(minX, minY, maxX, maxY, fillPaint)
        canvas.drawText(displayText, minX, minY-10, textPaint)
    }

    return mutableBitmap
}
