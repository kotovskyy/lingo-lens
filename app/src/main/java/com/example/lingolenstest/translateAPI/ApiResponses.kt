package com.example.lingolenstest.translateAPI

data class TranslationResponse(
    val translation: String,
    val info: Info
)

data class Info(
    val pronunciation: Pronunciation,
    val definitions: List<Definition>
)

data class Pronunciation(
    val query: String?,
    val translation: String?
)

data class Definition(
    val list: List<DefinitionDetail>?
)

data class DefinitionDetail(
    val definition: String?,
    val example: String?,
    val synonyms: List<String>?
)
