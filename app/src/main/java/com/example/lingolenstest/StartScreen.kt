package com.example.lingolenstest

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lingolenstest.translateAPI.Language
import com.example.lingolenstest.translateAPI.languages

@Composable
fun StartButton(text: String, onClick: () -> Unit){
    Button(
        onClick = { onClick() },
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        shape = RoundedCornerShape(15.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 25.dp, vertical = 8.dp)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreenTopBar(title: String){
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge
            )
        },
        navigationIcon = {
            Icon(
                painter = painterResource(id = R.drawable.language),
                contentDescription = "language icon",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(50.dp)
                    .padding(horizontal = 12.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
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
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.tertiary
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                )
            ,
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Language:",
                    color = MaterialTheme.colorScheme.onTertiary,
                    style = MaterialTheme.typography.headlineSmall
                )
                LanguagePicker(selectedLanguage) { language ->
                    saveSelectedLanguage(context, language.code)
                }
                Spacer(modifier = Modifier.height(25.dp))
                StartButton(
                    text = "Start",
                    onClick = { navController?.navigate(Video.route) }
                )
            }
        }
    }
}

@Composable
fun LanguagePicker(defaultLanguage: Language, onLanguageSelected: (Language) -> Unit){
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(defaultLanguage) }

    val context = LocalContext.current

    val iconID = context.resources.getIdentifier(
        "${selectedLanguage.code}_flag", "drawable", context.packageName
    )

    LanguageButton(
        text=selectedLanguage.name,
        flagIcon = iconID,
        onClick = {
            expanded = true
        }
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(color = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        languages.forEach { language: Language ->
            val languageFlagResId = context.resources.getIdentifier(
                "${language.code}_flag", "drawable", context.packageName
            )

            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        if (languageFlagResId != 0){
                            Icon(
                                painter = painterResource(id = languageFlagResId),
                                contentDescription = "${language.name} flag",
                                tint = Color.Unspecified
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = language.name,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
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

fun getSelectedLanguage(context: Context): String? {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    return sharedPreferences.getString("selected_language", null)  // Return null if not found
}

fun saveSelectedLanguage(context: Context, languageCode: String){
    val sharedPreferences : SharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("selected_language", languageCode).apply()
}


@Preview
@Composable
fun StartScreenPreview(){
    StartScreen(navController = null)
}

@Composable
fun LanguageButton(
    text: String,
    flagIcon: Int,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (flagIcon != 0){
                Icon(
                    painter = painterResource(id = flagIcon),
                    contentDescription = "Flag",
                    modifier = Modifier.padding(end = 8.dp, start = 8.dp),
                    tint = Color.Unspecified
                )
            }
            Text(
                text = text,
                fontSize = 20.sp,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(end = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}