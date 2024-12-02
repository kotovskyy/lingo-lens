package com.example.lingolens.detection

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min

class YOLO(
    private val context: Context,
    private val modelFilename: String,
    private val confidenceThreshold: Float = 0.4f,
    private val iouThreshold: Float = 0.5f
){
    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(320, 320, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()

    private var interpreter: Interpreter
    var boundingBoxes = ArrayList<BoundingBox>()

    init {
        interpreter = Interpreter(loadModelFile(context, modelFilename))
    }

    fun analyze(bitmap: Bitmap) {
        val tensorImage = TensorImage.fromBitmap(bitmap)
        val processedImage = imageProcessor.process(tensorImage)
        val inputBuffer = processedImage.buffer.asFloatBuffer()
        val outputBuffer = Array(1) { Array(6300) { FloatArray(85) } }
        interpreter.allocateTensors()
        interpreter.run(inputBuffer, outputBuffer)
        processModelOutput(outputBuffer)
    }

    private fun processModelOutput(outputBuffer: Array<Array<FloatArray>>) {
        val boxes = ArrayList<BoundingBox>()
        for (i in outputBuffer[0].indices) {
            val confidence = outputBuffer[0][i][4]
            if (confidence > confidenceThreshold) {
                val xCenter = outputBuffer[0][i][0]
                val yCenter = outputBuffer[0][i][1]
                val width = outputBuffer[0][i][2]
                val height = outputBuffer[0][i][3]
                val classConfidences = outputBuffer[0][i].sliceArray(5 until 85)
                val classId = classConfidences.indexOfFirst { it == (classConfidences.maxOrNull() ?: 0f) }

                boxes.add(BoundingBox(xCenter, yCenter, width, height, confidence, classId))
            }
        }
        boundingBoxes = applyNMS(boxes.toMutableList(), iouThreshold)
    }

    private fun loadModelFile(context: Context, filename: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(filename)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun applyNMS(boxes: MutableList<BoundingBox>, iouThreshold: Float = 0.5f) : ArrayList<BoundingBox> {
        boxes.sortByDescending { it.confidence }
        val resultBoxes = ArrayList<BoundingBox>()
        while (boxes.isNotEmpty()) {
            val bestBox = boxes.removeAt(0)
            resultBoxes.add(bestBox)
            val boxesToKeep = ArrayList<BoundingBox>()
            for (box in boxes){
                val iou = calculateIoU(bestBox, box)
                if (iou < iouThreshold){
                    boxesToKeep.add(box)
                }
            }
            boxes.clear()
            boxes.addAll(boxesToKeep)
        }
        return resultBoxes
    }

    private fun calculateIoU(boxA: BoundingBox, boxB: BoundingBox): Float {
        val xA = max(boxA.centerX, boxB.centerX)
        val yA = max(boxA.centerY, boxB.centerY)
        val xB = min(boxA.centerX + boxA.width, boxB.centerX + boxB.width)
        val yB = min(boxA.centerY + boxA.height, boxB.centerY + boxB.height)

        val interArea = max(0f, xB - xA) * max(0f, yB - yA)
        val boxAArea = boxA.width * boxA.height
        val boxBArea = boxB.width * boxB.height

        val iou = interArea / (boxAArea + boxBArea - interArea)

        return iou
    }
}