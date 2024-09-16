package com.example.lingolenstest

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

@Composable
fun CameraPreview(
    modifier: Modifier
){
    var isImageBeingCaptured by remember { mutableStateOf(false) }

    val alphaAnimation = remember { Animatable(1f) }
    val flashAnimation = remember { Animatable(0f) }

    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    var cameraSelector by remember {
        mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA)
    }
    val cameraProvider = remember { cameraProviderFuture.get() }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    val preview = remember {
        Preview.Builder()
            .setTargetRotation(previewView.display?.rotation ?: 0)
            .build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
    }

    val imageCapture = remember {
        ImageCapture.Builder().build()
    }



    // TODO: fix the problem with camera bounding after coming back from DisplayImageActivity

    // Update animation when `isImageBeingCaptured` changes
    LaunchedEffect(isImageBeingCaptured) {
        if (isImageBeingCaptured) {
            // Trigger flash animation
            flashAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing) // Duration of flash
            )
            flashAnimation.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing) // Duration of fade-out
            )
            // Trigger alpha animation
            alphaAnimation.animateTo(
                targetValue = 0.4f,
                animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
            )
        } else {
            alphaAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = alphaAnimation.value },
            factory = { ctx ->
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                previewView
            }
        )
        // Flash overlay
        if (flashAnimation.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = flashAnimation.value))
            )
        }
        // Show circular progress indicator while loading
        if (isImageBeingCaptured) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                CircularProgressIndicator()
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CameraButton(
                onClick = {
                    cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                },
                iconID = R.drawable.flip_camera_24,
                description = "Flip Camera"
            )
            CameraButton(
                onClick = {
                    isImageBeingCaptured = true
                    takePhoto(context, imageCapture, cameraExecutor, previewView) {
                        isImageBeingCaptured = false
                    }
                },
                iconID = R.drawable.photo_camera_24,
                description = "Take Photo"
            )
        }
    }
}

fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    cameraExecutor: ExecutorService,
    view: View,
    onFinish: () -> Unit
){
    imageCapture.takePicture(
        cameraExecutor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = image.toBitmap()
                val rotationDegrees = image.imageInfo.rotationDegrees
                val rotatedBitmap = rotateBitmapIfRequired(bitmap, rotationDegrees)
                image.close()

                val scaledBitmap = scaleCapturedImage(rotatedBitmap, view)
                // Save bitmap to temporary file
                val tempFile = createTempImageFile(context, scaledBitmap)
                tempFile?.let {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        it
                    )

                    // Start new activity and pass file's URI
                    val intent = Intent(context, DisplayImageActivity::class.java).apply {
                        putExtra("image_uri", uri.toString())
                    }
                    context.startActivity(intent)
                }
                onFinish()
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("IMAGE CAPTURE FAIL", "Failed to capture image")
                exception.printStackTrace()
                onFinish()
            }
        }
    )
}

fun scaleCapturedImage(bitmap: Bitmap, viewFinder: View): Bitmap {
    // Get the dimensions of the View (Preview)
    val viewWidth = viewFinder.width
    val viewHeight = viewFinder.height

    // Get the dimensions of the captured image
    val imageWidth = bitmap.width
    val imageHeight = bitmap.height

    // Calculate the scaling factor to maintain the aspect ratio
    val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()

    // Determine the target width and height while maintaining aspect ratio
    val targetWidth: Int
    val targetHeight: Int

    if (viewWidth / aspectRatio <= viewHeight) {
        targetWidth = viewWidth
        targetHeight = (viewWidth / aspectRatio).toInt()
    } else {
        targetHeight = viewHeight
        targetWidth = (viewHeight * aspectRatio).toInt()
    }

    // Scale the bitmap to the target size
    return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
}


fun rotateBitmapIfRequired(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
    return if (rotationDegrees != 0) {
        val matrix = android.graphics.Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}

fun createTempImageFile(context: Context, bitmap: Bitmap): File? {
    return try {
        val tempFile = File.createTempFile("captured_image", ".jpg", context.cacheDir)
        val fos = FileOutputStream(tempFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun CameraButton(
    onClick: () -> Unit,
    iconID: Int,
    description: String = ""
) {
    IconButton(
        modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primary),
        onClick = { onClick() })
    {
        Icon(
            painter = painterResource(id = iconID),
            contentDescription = description
        )
    }
}