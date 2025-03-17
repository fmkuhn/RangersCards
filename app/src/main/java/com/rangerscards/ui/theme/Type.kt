package com.rangerscards.ui.theme


import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rangerscards.R

@Immutable
data class CustomTypography(
    val headline: TextStyle,
)

val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

val Jost = FontFamily(
    Font(R.font.jost_regular, FontWeight.Normal),
    Font(R.font.jost_medium, FontWeight.Medium),
    Font(R.font.jost_bold, FontWeight.Bold),
    Font(R.font.jost_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.jost_mediumitalic, FontWeight.Medium, FontStyle.Italic)
)

val typography = CustomTypography(
    headline = TextStyle(
        fontFamily = Jost,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    )
)

val LocalCustomTypography = staticCompositionLocalOf { typography }