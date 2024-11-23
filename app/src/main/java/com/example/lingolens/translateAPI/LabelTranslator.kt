package com.example.lingolens.translateAPI

import android.content.Context
import org.json.JSONObject
import java.io.IOException

const val DICT_PATH = "mscoco_dict.json"

class LabelTranslator(private val context: Context) {

    private val translations: Map<String, Map<String, String>>

    init {
        translations = loadTranslations()
    }

    // Function to load and parse the JSON file
    private fun loadTranslations(): Map<String, Map<String, String>> {
        val jsonStr = readJsonFromAssets(DICT_PATH)
        val jsonObject = JSONObject(jsonStr)
        val translationMap = mutableMapOf<String, Map<String, String>>()

        jsonObject.keys().forEach { label ->
            val languageMap = jsonObject.getJSONObject(label)
            val langMap = mutableMapOf<String, String>()

            languageMap.keys().forEach { lang ->
                langMap[lang] = languageMap.getString(lang)
            }

            translationMap[label] = langMap
        }

        return translationMap
    }

    private fun readJsonFromAssets(filename: String): String {
        val json: String
        try {
            json = context.assets.open(filename).bufferedReader().use { it.readText() }
        } catch (ex: IOException) {
            throw RuntimeException("Error reading JSON file", ex)
        }
        return json
    }

    fun getTranslatedLabel(label: String, langCode: String): String {
        return translations[label]?.get(langCode) ?: label
    }
}