package com.example.lingolens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lingolens.ui.theme.LingoLensTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var hasCameraPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                )
            }

            val cameraPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { granted ->
                    hasCameraPermission = granted
                }
            )

            LaunchedEffect(Unit) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

            LingoLensTheme {
                Box(modifier = Modifier.fillMaxSize()){
                    if (!hasCameraPermission) {
                        PermissionsScreen(
                            context = context,
                            onRetry = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                        )
                    } else {
                        AppNavigation()
                    }
                }
            }
        }
    }
}


@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Home.route){
        composable(route = Home.route){
            Home.screen(navController)
        }
        composable(route = Video.route){
            Video.screen(navController)
        }
    }
}

@Composable
fun PermissionsScreen(context: Context, onRetry: () -> Unit){
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 20.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(15.dp))
        showPermissionDeniedMessage(
            context = context,
            onRetry = onRetry
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Button(
                onClick = onRetry,
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    Color.Transparent, Color.Transparent
                ),
                modifier = Modifier
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.try_again),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W600),
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            Text(
                text = stringResource(id = R.string.or),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            TextButton(onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }) {
                Text(
                    text = stringResource(id = R.string.open_settings),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.W500),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun showPermissionDeniedMessage(
    context: Context,
    onRetry: () -> Unit
) {
    Snackbar(
        action = {
        },
        containerColor = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.padding(10.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.camera_permission_required),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
