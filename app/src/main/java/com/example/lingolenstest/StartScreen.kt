package com.example.lingolenstest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun CenteredButton(text: String, onClick: () -> Unit){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = { onClick() },
        ) {
            Text(
                text = text,
                fontSize = 24.sp,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 7.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreenTopBar(title: String){
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 30.sp
            )
        },
        modifier = Modifier.background(color = MaterialTheme.colorScheme.primary)
    )
}


@Composable
fun StartScreen(navController: NavController?){
    Scaffold(
        topBar = { StartScreenTopBar(title = "Lingo Lens") },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ){
            CenteredButton(
                text = "Start",
                onClick = { navController?.navigate(Video.route) }
            )
        }
    }
}
