package com.example.lingolenstest

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lingolenstest.translateAPI.DefinitionDetail
import com.example.lingolenstest.translateAPI.TranslationResponse
import com.example.lingolenstest.translateAPI.TranslatorInstance
import kotlinx.coroutines.launch

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

                IconButton({ onClose() }){
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Back"
                    )
                }
            }
            if (translationLanguage.code != "nan"){
                Text(text = "Word: $word", style = MaterialTheme.typography.titleSmall)
                if (translation.isNotEmpty()){
                    Text(text = "Translation: $translation", style = MaterialTheme.typography.bodyLarge)
                }
                if (transcription.isNotEmpty()) {
                    Text(text = "Transcription: $transcription", style = MaterialTheme.typography.bodyMedium)
                }
                if (wordDescription.isNotEmpty()){
                    Text(text = "Description: $wordDescription", style = MaterialTheme.typography.bodyMedium)
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