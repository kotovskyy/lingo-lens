package com.example.lingolens.presentation

import android.graphics.Bitmap
import android.util.Log
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.example.lingolens.detection.BoundingBox
import com.example.lingolens.detection.Labels
import com.example.lingolens.detection.YOLO
import com.example.lingolens.translateAPI.LabelTranslator
import com.example.lingolens.translateAPI.Language
import com.example.lingolens.translateAPI.TranslatorInstance
import com.example.lingolens.translateAPI.translationLanguages
import kotlin.math.max
import kotlin.math.min

@Composable
fun ResultScreen(
    bitmap: Bitmap,
    yolo: YOLO,
    labelsTranslator: LabelTranslator,
    selectedLanguageCode: String
) {
    val context = LocalContext.current
    var detectedBitmap by remember { mutableStateOf(bitmap) }
    var translatedLabels by remember { mutableStateOf(emptyList<String>()) }
    var showTranslationCard by remember { mutableStateOf(false) }
    var selectedBoundingBox by remember { mutableStateOf<BoundingBox?>(null) }


    LaunchedEffect(Unit) {
        yolo.analyze(bitmap)
        val boxes = yolo.boundingBoxes

        translatedLabels = boxes.map {
            labelsTranslator.getTranslatedLabel(Labels.LABELS.get(it.classID), selectedLanguageCode)
        }
        detectedBitmap = drawBoundingBoxes(
            bitmap = bitmap,
            boxes = yolo.boundingBoxes,
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
            boxes = yolo.boundingBoxes
        ) { bbox ->
            selectedBoundingBox = bbox
            showTranslationCard = true
        }

        // Show the TranslationCard when a bounding box is clicked
        if (showTranslationCard && (selectedBoundingBox != null)) {
            Log.d("CARD CREATION", "${selectedBoundingBox!!.classID}")
            TranslationScreen(
                word = labelsTranslator.getTranslatedLabel(Labels.LABELS.get(selectedBoundingBox!!.classID), selectedLanguageCode),
                sourceLangCode = selectedLanguageCode,
                onClose = {
                    showTranslationCard = false
                    selectedBoundingBox = null
                }
            )
        }
    }
}

@Composable
fun ErrorScreen(
    text: String
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
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