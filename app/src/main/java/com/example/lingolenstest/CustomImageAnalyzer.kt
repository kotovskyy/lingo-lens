package com.example.lingolenstest

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Color.alpha
import android.graphics.Color.blue
import android.graphics.Color.green
import android.graphics.Color.red
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.scale
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

//class CustomImageAnalyzer(
//    private val context: Context,
//    private val viewModel: BboxesViewModel
//): ImageAnalysis.Analyzer {
//    private val imageProcessor = ImageProcessor.Builder()
//        .add(ResizeOp(320, 320, ResizeOp.ResizeMethod.BILINEAR))
//        .add(NormalizeOp(0f, 255f))
//        .build()
//
//    private lateinit var interpreter: Interpreter
//    private var width: Int = 0
//    private var height: Int = 0
//
//    init {
//        val modelPath = "yolov5.tflite"
//        interpreter = Interpreter(loadModelFile(context, modelPath))
//    }


//    override fun analyze(image: ImageProxy) {
//        val bitmap = image.toBitmap()
//        width = bitmap.width
//        height = bitmap.height
//        val tensorImage = TensorImage.fromBitmap(bitmap)
//        val processedImage = imageProcessor.process(tensorImage)
//        Log.d("Bitmap", "Width: ${bitmap.width} Height: ${bitmap.height} Colorspace: ${bitmap.colorSpace}")
//        Log.d("Processed tensor Image", "Width: ${processedImage.width} Height: ${processedImage.height} Colorspace: ${processedImage.colorSpaceType} Datatype: ${processedImage.dataType}")
//
//        // Convert processed image to FloatBuffer
//        val inputBuffer = processedImage.buffer.asFloatBuffer()
//
//        // Prepare output buffer
//        val outputBuffer = Array(1) { Array(6300) { FloatArray(85) } }
//
//        // Run inference
//        interpreter.run(inputBuffer, outputBuffer)
//
//        // Process output
//        processModelOutput(outputBuffer)
//
//        image.close()
//    }

//    private fun processModelOutput(outputBuffer: Array<Array<FloatArray>>) {
//        var boundingBoxes = ArrayList<BoundingBox>()
//        for (i in outputBuffer[0].indices) {
//            val confidence = outputBuffer[0][i][4] // Confidence score
//            if (confidence > 0.6) { // Threshold for confidence
//                val xCenter = outputBuffer[0][i][0] // Bounding box coordinates
//                val yCenter = outputBuffer[0][i][1]
//                val width = outputBuffer[0][i][2]
//                val height = outputBuffer[0][i][3]
//                val classConfidences = outputBuffer[0][i].sliceArray(5 until 85)
//                val classId = classConfidences.indexOfFirst { it == (classConfidences.maxOrNull() ?: 0f) }
//
//                boundingBoxes.add(BoundingBox(xCenter, yCenter, width, height, confidence, classId))
//                boundingBoxes.add(convertBbox(xCenter, yCenter, width, height, this.width, this.height))
//                Log.d("Detection", "Class: $classId, Confidence: $confidence, Box: ${convertBbox(xCenter, yCenter, width, height)}")
//            }
//        }

//        boundingBoxes = applyNMS(boundingBoxes.toList())
//        viewModel.updateBoundingBoxes(boundingBoxes.toList())
//    }
//
//
//    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
//        val assetFileDescriptor = context.assets.openFd(modelPath)
//        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
//        val fileChannel = fileInputStream.channel
//        val startOffset = assetFileDescriptor.startOffset
//        val declaredLength = assetFileDescriptor.declaredLength
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
//    }
//}

//    fun convertBbox(centerX: Float, centerY: Float, width: Float, height: Float, imgWidth: Int = 320, imgHeight: Int = 320): BoundingBox {
//        val centerXScaled = (centerX * imgWidth).toInt()
//        val centerYScaled = (centerY * imgHeight).toInt()
//        val widthScaled = (width * imgWidth).toInt()
//        val heightScaled = (height * imgHeight).toInt()
//
//        val minX = centerXScaled - widthScaled / 2
//        val minY = centerYScaled - heightScaled / 2
//
//        return BoundingBox(minX, minY, widthScaled, heightScaled)
//    }

//fun applyNMS(
//    boxes: List<BoundingBox>,
//    confidenceThreshold: Float = 0.6f,
//    iouThreshold: Float = 0.5f
//) : ArrayList<BoundingBox> {
//    val filteredBoxes = boxes.filter { it.confidence > confidenceThreshold }.toMutableList()
//    filteredBoxes.sortByDescending { it.confidence }
//
//    val resultBoxes = ArrayList<BoundingBox>()
//
//    while (filteredBoxes.isNotEmpty()) {
//        val bestBox = filteredBoxes.removeAt(0)
//        resultBoxes.add(bestBox)
//
//        val boxesToKeep = ArrayList<BoundingBox>()
//        for (box in filteredBoxes){
//            val iou = calculateIoU(bestBox, box)
//            if (iou < iouThreshold){
//                boxesToKeep.add(box)
//            }
//        }
//        filteredBoxes.clear()
//        filteredBoxes.addAll(boxesToKeep)
//    }
//
//    return resultBoxes
//}
//
//fun calculateIoU(boxA: BoundingBox, boxB: BoundingBox): Float {
//    val xA = max(boxA.centerX, boxB.centerX)
//    val yA = max(boxA.centerY, boxB.centerY)
//    val xB = min(boxA.centerX + boxA.width, boxB.centerX + boxB.width)
//    val yB = min(boxA.centerY + boxA.height, boxB.centerY + boxB.height)
//
//    val interArea = max(0f, xB - xA) * max(0f, yB - yA)
//    val boxAArea = boxA.width * boxA.height
//    val boxBArea = boxB.width * boxB.height
//
//    val iou = interArea / (boxAArea + boxBArea - interArea)
//
//    return iou
//}