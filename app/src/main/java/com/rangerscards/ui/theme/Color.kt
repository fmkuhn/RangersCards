package com.rangerscards.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Grayscale
val Light30 = Color(0xFFFFFBF2)
val Light20 = Color(0xFFF5F0E1)
val Light15 = Color(0xFFE6E1D3)
val Light10 = Color(0xFFD7D3C6)
val Medium = Color(0xFF9B9B9B)
val Dark10 = Color(0xFF656C6F)
val Dark15 = Color(0xFF4F5A60)
val Dark20 = Color(0xFF394852)
val Dark30 = Color(0xFF24303C)

// Light colors
val WarnLight = Color(0xFFFB4135)
val InfoLight = Color(0xFFE48F0F)
val CampaignBlueLight = Color(0xFF25B7CB)

// Dark colors
val WarnDark = Color(0xFFFC2323)
val InfoDark = Color(0xFFFF922E)
val CampaignBlueDark = Color(0xFF179BAD)

// Permanent colors
val Blue = Color(0xFF1072C2)
val Orange = Color(0xFFDB7C07)
val Green = Color(0xFF219428)
val Red = Color(0xFFCC3038)
val Purple = Color(0xFF593B5D)
val Gold = Color(0xFFBFA640)

@Immutable
data class CustomColors(
    val l30: Color,
    val l20: Color,
    val l15: Color,
    val l10: Color,
    val m: Color,
    val d10: Color,
    val d15: Color,
    val d20: Color,
    val d30: Color,
    val blue: Color = Blue,
    val orange: Color = Orange,
    val green: Color = Green,
    val red: Color = Red,
    val purple: Color = Purple,
    val gold: Color = Gold,
    val warn: Color,
    val info: Color,
    val campaignBlue: Color
)

val DarkColorScheme = CustomColors(
    l30 = Dark30,
    l20 = Dark20,
    l15 = Dark15,
    l10 = Dark10,
    m = Medium,
    d10 = Light10,
    d15 = Light15,
    d20 = Light20,
    d30 = Light30,
    warn = WarnDark,
    info = InfoDark,
    campaignBlue = CampaignBlueDark
)

val LightColorScheme = CustomColors(
    l30 = Light30,
    l20 = Light20,
    l15 = Light15,
    l10 = Light10,
    m = Medium,
    d10 = Dark10,
    d15 = Dark15,
    d20 = Dark20,
    d30 = Dark30,
    warn = WarnLight,
    info = InfoLight,
    campaignBlue = CampaignBlueLight
)

val LocalCustomColors = staticCompositionLocalOf { LightColorScheme }