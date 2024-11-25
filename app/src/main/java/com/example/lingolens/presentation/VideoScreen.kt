package com.example.lingolens.presentation

import android.app.Activity
import androidx.activity.ComponentActivity.MODE_PRIVATE
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lingolens.Home
import com.example.lingolens.R


@Composable
fun VideoScreen(navController: NavController?){
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            VideoScreenTopBar(
                text = stringResource(id = R.string.take_picture),
                onBackClick =  { navController?.navigate(Home.route) },
                onSettingsClick = { showSettings = !showSettings }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CameraPreview(
                showSettings = showSettings,
                onSettingsShrink = { showSettings = false }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreenTopBar(
    text: String,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
){
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ){
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = "Back",
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        actions = {
            IconButton(onClick = { onSettingsClick() }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = "Back",
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    )
}
