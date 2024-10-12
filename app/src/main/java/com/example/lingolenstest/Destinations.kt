package com.example.lingolenstest

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.lingolenstest.presentation.VideoScreen


interface Destination{
    val route: String
    val screen: @Composable (NavController) -> Unit
}


object Home: Destination {
    override val route = "home"
    override val screen: @Composable (NavController) -> Unit = {
        StartScreen(navController = it)
    }
}

object Video: Destination {
    override val route = "video"
    override val screen: @Composable (NavController) -> Unit = {
        VideoScreen(navController = it)
    }
}
