package com.example.lingolens.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.example.lingolens.R
import com.example.lingolens.detection.Labels
import com.example.lingolens.detection.BoundingBox
import com.example.lingolens.detection.YoloAPI
import com.example.lingolens.getSelectedLanguage
import com.example.lingolens.translateAPI.LabelTranslator
import com.example.lingolens.translateAPI.Language
import com.example.lingolens.translateAPI.TranslatorInstance
import com.example.lingolens.translateAPI.translationLanguages
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
                if (imageUri != null) {
                    val bitmap = loadImageFromUri(Uri.parse(imageUri))
                    if (bitmap != null) {
                        val context = LocalContext.current
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
                            detectedBitmap = drawBoundingBoxes(
                                bitmap = bitmap,
                                boxes = yoloAPI.boundingBoxes,
                                labels = translatedLabels
                            )

                            translationLanguages = emptyList<Language>()
                            if (isOnline(context)) {
                                translationLanguages = TranslatorInstance.fetchSupportedLanguages()!!
                            }
                        }

                        Box(
                            contentAlignment = Alignment.Center
                        ){
                            DisplayImage(
                                originalBitmap = bitmap,
                                bboxBitmap = detectedBitmap,
                                boxes = yoloAPI.boundingBoxes
                            ) { bbox ->
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
                        Column(
                            modifier = Modifier.fillMaxSize().padding(15.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.cant_open_image),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.cant_open_image),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
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

    private fun drawBoundingBoxes(bitmap: Bitmap, boxes: List<BoundingBox>, labels: List<String>) : Bitmap {
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

    private fun clearCache(context: Context) {
        val cacheDir = context.cacheDir
        cacheDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("captured_image") && file.name.endsWith(".jpg")) {
                file.delete()
            }
        }
    }
}

@Composable
fun DisplayImage(originalBitmap: Bitmap, bboxBitmap: Bitmap, boxes: List<BoundingBox>, onBoxClicked: (BoundingBox) -> Unit){
    val sdkInt = android.os.Build.VERSION.SDK_INT
    var imageSize by remember { mutableStateOf(Size.Zero) }
    // Sort the boxes by area (width * height) in ascending order
    val sortedBoxes = boxes.sortedBy { box ->
        val width = box.width * bboxBitmap.width
        val height = box.height * bboxBitmap.height
        width * height // Area of the bounding box
    }

    // Blurred background
    Image(
        bitmap = originalBitmap.asImageBitmap(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .blur(10.dp),
        contentScale = ContentScale.Crop
    )

    if (sdkInt < android.os.Build.VERSION_CODES.S){
        // simulation of the Modifier.blur effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 0.8f
                }
                .background(Color.Black.copy(alpha = 0.9f))
        )
    }

    Image(
        bitmap = bboxBitmap.asImageBitmap(),
        contentDescription = "Captured Image",
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { tapOffset: Offset ->
                        val maxWidth = bboxBitmap.width
                        val maxHeight = bboxBitmap.height
                        val bitmapYOffset = (imageSize.height - maxHeight) / 2

                        for (box in sortedBoxes) {
                            val minX = max(
                                box.centerX * bboxBitmap.width - box.width * bboxBitmap.width / 2,
                                0f
                            )
                            val minY = max(
                                box.centerY * bboxBitmap.height - box.height * bboxBitmap.height / 2 + bitmapYOffset,
                                0f
                            )

                            val maxX = min(
                                box.centerX * bboxBitmap.width + box.width * bboxBitmap.width / 2,
                                maxWidth.toFloat()
                            )
                            val maxY = min(
                                box.centerY * bboxBitmap.height + box.height * bboxBitmap.height / 2 + bitmapYOffset,
                                maxHeight.toFloat()
                            )

                            if (tapOffset.x in minX..maxX && tapOffset.y in minY..maxY) {
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