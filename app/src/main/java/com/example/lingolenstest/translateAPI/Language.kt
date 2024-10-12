package com.example.lingolenstest.translateAPI

data class Language(val code: String, val name: String)

val languages = listOf(
    Language("en", "English"),
    Language("es", "Spanish"),
    Language("fr", "French"),
    Language("de", "German"),
    Language("it", "Italian"),
    Language("pl", "Polish"),
    Language("ru", "Russian")
)
