package com.example.lingolens.presentation

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import com.example.lingolens.LanguagePicker
import com.example.lingolens.R
import com.example.lingolens.translateAPI.DefinitionDetail
import com.example.lingolens.translateAPI.Language
import com.example.lingolens.translateAPI.TranslationResponse
import com.example.lingolens.translateAPI.TranslatorInstance
import com.example.lingolens.translateAPI.translationLanguages
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun TranslationScreen(
    word: String,
    sourceLangCode: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val savedTranslationCode = getTranslationLanguage(context)
    val defaultTranslationLanguage = translationLanguages.find { it.code == savedTranslationCode } ?: Language("nan", stringResource(
        id = R.string.choose_language
    ))
    var translationLanguage by remember { mutableStateOf(defaultTranslationLanguage) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    var translation by remember { mutableStateOf("") }
    var transcription by remember { mutableStateOf("") }
    var wordDescription by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS && translationLanguage.code != "nan") {
                tts?.language = Locale(translationLanguage.code)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    LaunchedEffect(translationLanguage) {
        if (translationLanguage.code != "nan") {
            coroutineScope.launch {
                val wordTranslationResponse: TranslationResponse? = translateWord(word, sourceLangCode, translationLanguage.code)
                wordTranslationResponse?.let { response ->
                    translation = response.translation
                    transcription = response.info.pronunciation.translation ?: ""
                    wordDescription = if (response.info.definitions.isNotEmpty()){
                        val firstDefinition: DefinitionDetail? = response.info.definitions[0].list?.firstOrNull()
                        firstDefinition?.definition ?: ""
                    } else {
                        ""
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .zIndex(10F),
        contentAlignment = Alignment.Center
    ) {
        Popup(
            alignment = Alignment.Center,
            properties = PopupProperties(
                excludeFromSystemGesture = true,
            ),
            onDismissRequest = { onClose() }
        ) {
            Card(
                colors = CardColors(
                    contentColor = Color.Transparent,
                    containerColor = Color.Unspecified,
                    disabledContentColor = Color.Unspecified,
                    disabledContainerColor = Color.Unspecified
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        2.dp,
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = RoundedCornerShape(25.dp)
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(25.dp)
                    ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                if (!isOnline(context)){
                    ErrorTranslationCard(
                        title = stringResource(id = R.string.no_internet_connection),
                        description = stringResource(id = R.string.translation_not_available),
                        onClose = onClose
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        TranslationHeader(
                            defaultLanguage = defaultTranslationLanguage,
                            onClose = onClose,
                            onLanguageSelected = { language ->
                                translation = ""
                                transcription = ""
                                wordDescription = ""

                                translationLanguage = language
                                saveTranslationLanguage(context, language.code)
                                tts?.language = Locale(language.code)
                                coroutineScope.launch {
                                    val response = translateWord(word, sourceLangCode, language.code)
                                    response?.let {
                                        translation = it.translation
                                        transcription = it.info.pronunciation.translation ?: ""
                                        wordDescription = it.info.definitions?.firstOrNull()?.list?.firstOrNull()?.definition ?: ""
                                    }
                                }
                            }
                        )
                        if (translationLanguage.code != "nan"){
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                            ) {
                                Text(text = stringResource(id = R.string.word) + ": $word", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                if (translation.isNotEmpty()){
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.translation) + ": $translation",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(1f),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        IconButton(
                                            onClick = {
                                                tts?.speak(translation, TextToSpeech.QUEUE_FLUSH, null, null)
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.play_sound),
                                                contentDescription = "play",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                                if (transcription.isNotEmpty()) {
                                    Spacer(modifier = Modifier
                                        .height(10.dp)
                                        .fillMaxWidth())
                                    Text(
                                        text = stringResource(id = R.string.transcription) + ": $transcription",
                                        fontWeight = FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (wordDescription.isNotEmpty()){
                                    Text(
                                        text = stringResource(id = R.string.description) + ": $wordDescription",
                                        fontWeight = FontWeight.Light,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun ErrorTranslationCard(
    title: String,
    description: String,
    onClose: () -> Unit
){
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.Start
    ){
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            CloseCardButton(onClose)
        }
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun TranslationHeader(
    defaultLanguage: Language,
    onClose: () -> Unit,
    onLanguageSelected: (Language) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        LanguagePicker(
            languages = translationLanguages,
            defaultLanguage = defaultLanguage,
            onLanguageSelected = { language -> onLanguageSelected(language) },
            showFlags = false
        )
        CloseCardButton(onClose)
    }
}

@Composable
fun CloseCardButton(
    onClose: () -> Unit
){
    IconButton(
        onClick = onClose,
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.tertiary,
            shape = RoundedCornerShape(50.dp)
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            tint = MaterialTheme.colorScheme.onSecondary,
            contentDescription = "Close"
        )
    }
}

fun getTranslationLanguage(context: Context): String? {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    return sharedPreferences.getString("translation_language", null)  // Return null if not found
}

fun saveTranslationLanguage(context: Context, languageCode: String) {
    val sharedPreferences : SharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("translation_language", languageCode).apply()
}

suspend fun translateWord(word: String, sourceLangCode: String, targetLangCode: String): TranslationResponse? {
    return try {
        val response = TranslatorInstance.api.translateLabel(sourceLangCode, targetLangCode, word)
        if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (connectivityManager != null) {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
    }
    return false
}
