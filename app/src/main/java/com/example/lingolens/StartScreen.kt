package com.example.lingolens

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import com.example.lingolens.translateAPI.Language
import com.example.lingolens.translateAPI.appLanguages
import java.util.Locale

@Composable
fun StartButton(text: String, onClick: () -> Unit){
    Button(
        onClick = { onClick() },
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        shape = RoundedCornerShape(15.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSecondary,
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
    val defaultLanguage = appLanguages.find { it.code == savedLanguageCode }
        ?: appLanguages.find { it.code == Locale.getDefault().language } // If possible, default to system lang
        ?: appLanguages[0] // Default to English
    val selectedLanguage by remember { mutableStateOf(defaultLanguage) }

    Scaffold(
        topBar = { StartScreenTopBar(title = stringResource(id = R.string.app_name)) },
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.tertiary
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    MaterialTheme.colorScheme.tertiaryContainer
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
                    text = stringResource(id = R.string.choose_language),
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    style = MaterialTheme.typography.headlineSmall
                )
                LanguagePicker(
                    languages = appLanguages,
                    defaultLanguage = selectedLanguage,
                    onLanguageSelected = { language ->
                        saveSelectedLanguage(context, language.code)
                        updateAppLocale(language.code)
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(
                                language.code
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.height(25.dp))
                StartButton(
                    text = stringResource(id = R.string.start),
                    onClick = { navController?.navigate(Video.route) }
                )
            }
        }
    }
}

@Composable
fun LanguagePicker(
    languages: List<Language>,
    defaultLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    showFlags: Boolean = true,
    maxDropdownHeight: Dp = 400.dp
){
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(defaultLanguage) }

    val context = LocalContext.current

    val iconID = context.resources.getIdentifier(
        "${selectedLanguage.code}_flag", "drawable", context.packageName
    )

    Column(
        verticalArrangement = Arrangement.Center
    ){
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
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.secondaryContainer)
                .heightIn(max = maxDropdownHeight),
        ) {
            languages.forEach { language: Language ->
                val languageFlagResId = if (showFlags)
                    context.resources.getIdentifier(
                    "${language.code}_flag", "drawable", context.packageName)
                else 0

                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            if (languageFlagResId != 0 && showFlags){
                                Icon(
                                    painter = painterResource(id = languageFlagResId),
                                    contentDescription = "${language.name} flag",
                                    tint = Color.Unspecified
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
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
}

fun getSelectedLanguage(context: Context): String? {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    return sharedPreferences.getString("selected_language", "en")
}

fun saveSelectedLanguage(context: Context, languageCode: String){
    val sharedPreferences : SharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("selected_language", languageCode).apply()
}

fun updateAppLocale(languageCode: String) {
    val locale = Locale(languageCode)
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale.toLanguageTag()))
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
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
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
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}