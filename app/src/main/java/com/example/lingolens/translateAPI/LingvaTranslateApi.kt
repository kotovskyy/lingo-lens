package com.example.lingolens.translateAPI

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

//const val ROOT_URL = "https://lingva.ml/api/v1/"
const val ROOT_URL = "https://lingva-translate-eta.vercel.app/api/v1/" // mirror of the original API

interface LingvaTranslateApi {
    @GET("{sourceLang}/{targetLang}/{text}")
    suspend fun translateLabel(
        @Path("sourceLang") sourceLang: String,
        @Path("targetLang") targetLang: String,
        @Path("text") text: String
    ): Response<TranslationResponse>

    @GET("languages")
    suspend fun getSupportedLanguages(): Response<LanguagesResponse>
}

object TranslatorInstance {
    private const val BASE_URL = ROOT_URL

    val api: LingvaTranslateApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LingvaTranslateApi::class.java)
    }

    suspend fun fetchSupportedLanguages(): List<Language>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getSupportedLanguages()
                if (response.isSuccessful) {
                    response.body()?.languages?.filter { it.code != "auto" }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
