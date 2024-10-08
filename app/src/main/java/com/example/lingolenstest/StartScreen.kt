package com.example.lingolenstest

import android.content.Context
import android.content.SharedPreferences
import android.widget.Spinner
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val savedLanguageCode = getSelectedLanguage(context)
    val defaultLanguage = languages.find { it.code == savedLanguageCode } ?: languages[0] // Default to English

    val selectedLanguage by remember { mutableStateOf(defaultLanguage) }

    Scaffold(
        topBar = { StartScreenTopBar(title = "Lingo Lens") },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LanguagePicker("Selected language:", selectedLanguage) { language ->
                    saveSelectedLanguage(context, language.code)
                }
                CenteredButton(
                    text = "Start",
                    onClick = { navController?.navigate(Video.route) }
                )
            }
        }
    }
}

@Composable
fun LanguagePicker(text: String, defaultLanguage: Language, onLanguageSelected: (Language) -> Unit){
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(defaultLanguage) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text)
        OutlinedButton(
            onClick = {expanded = true},
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Text(selectedLanguage.name)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            languages.forEach { language: Language ->
                DropdownMenuItem(
                    text = {
                        Text(language.name)
                    },
                    onClick = {
                        selectedLanguage = language
                        expanded = false
                        onLanguageSelected(language)
                    }
                )
            }
        }
    }
}

fun getSelectedLanguage(context: Context): String? {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    return sharedPreferences.getString("selected_language", null)  // Return null if not found
}

fun saveSelectedLanguage(context: Context, languageCode: String){
    val sharedPreferences : SharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("selected_language", languageCode).apply()
}
