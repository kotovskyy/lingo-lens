package com.example.lingolens.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.lingolens.R


var Montserrat = FontFamily(
    Font(R.font.montserrat_black, weight = FontWeight.Black),
    Font(R.font.montserrat_black_italic, weight = FontWeight.Black, style = FontStyle.Italic),
    Font(R.font.montserrat_bold, weight = FontWeight.Bold),
    Font(R.font.montserrat_bold_italic, weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(R.font.montserrat_extra_bold, weight = FontWeight.ExtraBold),
    Font(R.font.montserrat_extra_bold_italic, weight = FontWeight.ExtraBold, style = FontStyle.Italic),
    Font(R.font.montserrat_extra_light, weight = FontWeight.ExtraLight),
    Font(R.font.montserrat_extra_light_italic, weight = FontWeight.ExtraLight, style = FontStyle.Italic),
    Font(R.font.montserrat_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(R.font.montserrat_light, weight = FontWeight.Light),
    Font(R.font.montserrat_light_italic, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(R.font.montserrat_medium, weight = FontWeight.Medium),
    Font(R.font.montserrat_medium_italic, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(R.font.montserrat_regular, weight = FontWeight.Normal),
    Font(R.font.montserrat_semi_bold, weight = FontWeight.SemiBold),
    Font(R.font.montserrat_semi_bold_italic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
    Font(R.font.montserrat_thin, weight = FontWeight.Thin),
    Font(R.font.montserrat_thin_italic, weight = FontWeight.Thin, style = FontStyle.Italic)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 25.sp,
        letterSpacing = 1.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Montserrat,
        fontSize = 40.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 42.sp,
        letterSpacing = 2.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Montserrat,
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Montserrat,
        fontSize = 28.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Montserrat,
        fontSize = 26.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Montserrat,
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp
    )
)