package com.example.lingolenstest

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lingolenstest.translateAPI.DefinitionDetail
import com.example.lingolenstest.translateAPI.TranslationResponse
import com.example.lingolenstest.translateAPI.TranslatorInstance
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun TranslationCard(
    word: String,
    sourceLangCode: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val savedTranslationCode = getTranslationLanguage(context)
    var defaultTranslationLanguage = languages.find { it.code == savedTranslationCode } ?: Language("nan", "Choose language")

    val translationLanguage by remember { mutableStateOf(defaultTranslationLanguage) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    LaunchedEffect(translationLanguage) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
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

    var translation by remember { mutableStateOf("") }
    var transcription by remember { mutableStateOf("") }
    var wordDescription by remember { mutableStateOf("") }


    val coroutineScope = rememberCoroutineScope()


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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ){
                LanguagePicker("Translate to:", defaultLanguage = defaultTranslationLanguage) { language ->
                    saveTranslationLanguage(context, language.code)

                    tts?.language = Locale(language.code)

                    coroutineScope.launch {
                        val wordTranslationResponse: TranslationResponse? = translateWord(word, sourceLangCode, language.code)
                        wordTranslationResponse?.let { response ->
                            translation = response.translation
                            transcription = response.info.pronunciation.translation ?: ""
                            wordDescription = if (!response.info.definitions.isNullOrEmpty()){
                                val firstDefinition: DefinitionDetail? = response.info.definitions[0].list?.firstOrNull()
                                firstDefinition?.definition ?: ""
                            } else {
                                ""
                            }
                        }
                    }


                }

                IconButton(onClick = { onClose() }){
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Back"
                    )
                }
            }
            if (translationLanguage.code != "nan"){
                Text(text = "Word: $word", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                if (translation.isNotEmpty()){
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Translation: $translation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            modifier = Modifier.weight(1f) // This makes the text take up available space
                        )
                        Spacer(modifier = Modifier.width(10.dp)) // Add space between the text and button
                        IconButton(
                            onClick = {
                                tts?.speak(translation, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.play_sound),
                                contentDescription = "play",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                                    }
                if (transcription.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp).fillMaxWidth())
                    Text(text = "Transcription: $transcription", fontWeight = FontWeight.Normal, fontSize = 20.sp)
                }
                if (wordDescription.isNotEmpty()){
                    Text(text = "Description: $wordDescription", fontWeight = FontWeight.Light, fontSize = 18.sp)
                }
            }
        }
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
            val rawResponse = response.body()?.toString()
            Log.d("RawAPIResponse", rawResponse ?: "No response body")
            response.body()
        } else {
            Log.e("Label Translate", "Failed to translate label: ${response.errorBody()?.string()}")
            null
        }
    } catch (e: Exception) {
        Log.e("TranslateError", "Error translating label: $word", e)
        null
    }
}
