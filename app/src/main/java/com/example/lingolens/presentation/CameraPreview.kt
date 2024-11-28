package com.example.lingolens.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity.MODE_PRIVATE
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.example.lingolens.R
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@Composable
fun CameraPreview(
    showSettings: Boolean,
    onSettingsShrink: () -> Unit
){
    val context = LocalContext.current

    var confidenceThreshold by remember { mutableFloatStateOf(0.4f) }
    var iouThreshold by remember { mutableFloatStateOf(0.5f) }

    var isImageBeingCaptured by remember { mutableStateOf(false) }

    val alphaAnimation = remember { Animatable(1f) }
    val flashAnimation = remember { Animatable(0f) }
    val animatedVisibilityState = remember { MutableTransitionState(false).apply { targetState = true } }


    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
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

    val imageCapture = remember { ImageCapture.Builder().build() }

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
            factory = { _ ->
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
                    .background(MaterialTheme.colorScheme.background.copy(alpha = flashAnimation.value))
            )
        }
        // Show circular progress indicator while loading
        if (isImageBeingCaptured) {
            CenterProgressIndicator()
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
        ){
            SlidingSettings(showSettings = showSettings, onClick = { onSettingsShrink() }) {
                SettingSlider(text = stringResource(id = R.string.confidence_threshold), value = confidenceThreshold) { value ->
                    confidenceThreshold = value
                }
                SettingSlider(text = stringResource(id = R.string.iou_threshold), value = iouThreshold) { value ->
                    iouThreshold = value
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp)
            ) {
                if (!isImageBeingCaptured){
                    AnimatedVisibility(
                        visibleState = animatedVisibilityState,
                        enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(1000, easing = EaseOutBounce)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.BottomCenter,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CameraInterfaceButton(
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
                                Spacer(modifier = Modifier.width(15.dp))
                                CameraInterfaceButton(
                                    onClick = {
                                        onSettingsShrink()
                                        isImageBeingCaptured = true
                                        takePhoto(context, imageCapture, cameraExecutor, previewView, confidenceThreshold, iouThreshold, cameraSelector) {
                                            isImageBeingCaptured = false
                                        }
                                    },
                                    iconID = R.drawable.photo_camera_24,
                                    description = "Take Photo"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CenterProgressIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .padding(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = MaterialTheme.colorScheme.secondary,
            strokeWidth = 5.dp
        )
    }
}


@Composable
fun SlidingSettings(
    showSettings: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
){
    AnimatedVisibility(
        visible = showSettings,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400, easing = LinearOutSlowInEasing)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400, easing = FastOutLinearInEasing))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha=0.8f),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            IconButton(
                onClick = { onClick() },
                colors = IconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    disabledContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.24f),
                    disabledContentColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.24f)
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    tint = MaterialTheme.colorScheme.onTertiary,
                    contentDescription = "Close Settings"
                )
            }
            content()
        }
    }
}

@Composable
fun SettingSlider(
    text: String,
    value: Float,
    onValueChange: (Float) -> Unit
){
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = text + ": "+ String.format("%.2f", value),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Slider(
            value = value,
            onValueChange = { onValueChange(it) },
            valueRange = 0f..1f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.tertiary,
                activeTrackColor = MaterialTheme.colorScheme.tertiary,
                inactiveTrackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.24f),
                activeTickColor = MaterialTheme.colorScheme.onTertiary,
                inactiveTickColor = MaterialTheme.colorScheme.onTertiary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        )
    }
}

@Composable
fun CameraInterfaceButton(
    onClick: () -> Unit,
    iconID: Int,
    description: String = ""
) {
    var isAnimating by remember { mutableStateOf(false) }

    // Animate scaling and opacity when clicked
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.2f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )

    val alpha by animateFloatAsState(
        targetValue = if (isAnimating) 0.8f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )

    IconButton(
        onClick = {
            isAnimating = true
            onClick()
        },
        modifier = Modifier
            .padding(15.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(MaterialTheme.colorScheme.tertiary)
            .padding(12.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = alpha
            )
    ) {
        Icon(
            painter = painterResource(id = iconID),
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.size(48.dp)
        )
    }

    // Reset animation state after the animation completes
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            delay(300)
            isAnimating = false
        }
    }
}

fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    cameraExecutor: ExecutorService,
    view: View,
    confidenceThreshold: Float,
    iouThreshold: Float,
    cameraSelector: CameraSelector,
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

                val finalBitmap = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                    mirrorBitmap(rotatedBitmap)
                } else {
                    rotatedBitmap
                }

                val scaledBitmap = scaleCapturedImage(finalBitmap, view)
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
                        putExtra("confidence_threshold", confidenceThreshold)
                        putExtra("iou_threshold", iouThreshold)
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

fun mirrorBitmap(bitmap: Bitmap): Bitmap {
    val matrix = Matrix().apply { postScale(-1f, 1f) } // Flip horizontally
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun scaleCapturedImage(bitmap: Bitmap, viewFinder: View): Bitmap {
    val viewWidth = viewFinder.width
    val viewHeight = viewFinder.height

    val imageWidth = bitmap.width
    val imageHeight = bitmap.height

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
