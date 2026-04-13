package com.focuscity.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Monospace gives a pixel-font feel out of the box.
// For a true pixel font, download "Press Start 2P" from fonts.google.com,
// place press_start_2p.ttf in res/font/, and change to:
//   val PixelFont = FontFamily(Font(R.font.press_start_2p))
val PixelFont = FontFamily.Monospace
val BodyFont = FontFamily.Default

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PixelFont,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    ),
    displayMedium = TextStyle(
        fontFamily = PixelFont,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp
    ),
    displaySmall = TextStyle(
        fontFamily = PixelFont,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PixelFont,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PixelFont,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PixelFont,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PixelFont,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFont,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFont,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFont,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontFamily = PixelFont,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PixelFont,
        fontSize = 9.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PixelFont,
        fontSize = 8.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
)
