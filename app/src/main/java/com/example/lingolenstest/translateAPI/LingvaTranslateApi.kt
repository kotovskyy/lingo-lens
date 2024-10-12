package com.example.lingolenstest.translateAPI

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
}
