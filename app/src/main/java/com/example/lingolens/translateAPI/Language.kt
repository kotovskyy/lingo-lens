package com.example.lingolens.translateAPI

data class Language(
    val code: String,
    val name: String
)

val appLanguages = listOf(
    Language("en", "English"),
    Language("es", "Spanish"),
    Language("fr", "French"),
    Language("de", "German"),
    Language("it", "Italian"),
    Language("pl", "Polish"),
    Language("ru", "Russian")
)

var translationLanguages = listOf(
    Language("en", "English"),
    Language("es", "Spanish"),
    Language("fr", "French"),
    Language("de", "German"),
    Language("it", "Italian"),
    Language("pl", "Polish"),
    Language("ru", "Russian")
)
