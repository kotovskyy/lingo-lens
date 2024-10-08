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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import com.example.lingolenstest.ui.theme.LingoLensTestTheme
import kotlin.math.max
import kotlin.math.min

class DisplayImageActivity: ComponentActivity() {

    private lateinit var yoloAPI: YoloAPI
    private lateinit var selectedLanguageCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageUri = intent.getStringExtra("image_uri")
        val confidenceThreshold = intent.getFloatExtra("confidence_threshold", 0.5f)
        val iouThreshold = intent.getFloatExtra("iou_threshold", 0.5f)
        val labelsTranslator = LabelTranslator(this)

        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        selectedLanguageCode = sharedPreferences.getString("selected_language", "en") ?: "en" // Default to English

        Log.d("Selected language", "CODE: $selectedLanguageCode")

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
                        var translatedLabels by remember { mutableStateOf(emptyList<String>()) }
                        var showTranslationCard by remember { mutableStateOf(false) }
                        var selectedBoundingBox by remember { mutableStateOf<BoundingBox?>(null) }


                        LaunchedEffect(Unit) {
                            yoloAPI.analyze(bitmap)

                            val boxes = yoloAPI.boundingBoxes


                            translatedLabels = boxes.map {
                                labelsTranslator.getTranslatedLabel(Labels.LABELS.get(it.classID), selectedLanguageCode)
                            }
                            detectedBitmap = drawBoundingBoxes(bitmap, yoloAPI.boundingBoxes, translatedLabels)
                        }
                        Box(
                            contentAlignment = Alignment.Center
                        ){
                            DisplayImage(bitmap = detectedBitmap, yoloAPI.boundingBoxes) { bbox ->
                                selectedBoundingBox = bbox
                                showTranslationCard = true
                            }

                            // Show the TranslationCard when a bounding box is clicked
                            if (showTranslationCard && (selectedBoundingBox != null)) {
                                Log.d("CARD CREATION", "${selectedBoundingBox!!.classID}")
                                TranslationCard(
                                    word = labelsTranslator.getTranslatedLabel(Labels.LABELS.get(selectedBoundingBox!!.classID), selectedLanguageCode),
                                    sourceLangCode = selectedLanguageCode,
                                    onClose = {
                                        showTranslationCard = false
                                        selectedBoundingBox = null
                                    }
                                )
                            }
                        }

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

    private fun drawBoundingBoxes(bitmap: Bitmap, boxes: List<BoundingBox>, labels: List<String>) : Bitmap {
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

        for (i in 0..<boxes.count()){
            val minX = max(boxes[i].centerX * bitmap.width - boxes[i].width*bitmap.width/2, 0f)
            val minY = max(boxes[i].centerY * bitmap.height - boxes[i].height*bitmap.height/2, 0f)
            val maxX = min(boxes[i].centerX * bitmap.width + boxes[i].width*bitmap.width/2, maxWidth.toFloat())
            val maxY = min(boxes[i].centerY * bitmap.height + boxes[i].height*bitmap.height/2, maxHeight.toFloat())

            val label = labels[i]
            val confidence = String.format("%.2f", boxes[i].confidence)

            val displayText = "$confidence $label"

            canvas.drawRect(minX, minY, maxX, maxY, paint)
            canvas.drawText(displayText, minX, minY-10, textPaint)
        }
        
        return mutableBitmap
    }
}

@Composable
fun DisplayImage(bitmap: Bitmap, boxes: List<BoundingBox>, onBoxClicked: (BoundingBox) -> Unit){
    var imageSize by remember { mutableStateOf(Size.Zero) }
    // Sort the boxes by area (width * height) in ascending order
    val sortedBoxes = boxes.sortedBy { box ->
        val width = box.width * bitmap.width
        val height = box.height * bitmap.height
        width * height // Area of the bounding box
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Captured Image",
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { tapOffset: Offset ->
                        val maxWidth = bitmap.width
                        val maxHeight = bitmap.height
                        val bitmapYOffset = (imageSize.height - maxHeight)/2

                        for (box in sortedBoxes) {
                            val minX = max(box.centerX * bitmap.width - box.width * bitmap.width / 2, 0f)
                            val minY = max(box.centerY * bitmap.height - box.height * bitmap.height / 2 + bitmapYOffset, 0f)

                            val maxX = min(box.centerX * bitmap.width + box.width * bitmap.width / 2, maxWidth.toFloat())
                            val maxY = min(box.centerY * bitmap.height + box.height * bitmap.height / 2 + bitmapYOffset, maxHeight.toFloat())

                            if (tapOffset.x in minX..maxX && tapOffset.y in minY..maxY){
                                onBoxClicked(box)
                                break
                            }
                        }
                    }
                )
            }
            .onGloballyPositioned { layoutCoordinates ->
            // Capture the size of the Image composable after it is rendered
            imageSize = layoutCoordinates.size.toSize()
        },
        contentScale = ContentScale.Fit
    )
}