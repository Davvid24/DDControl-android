package com.ddcontrol.ddcontrol_android.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy     = Color(0xFF0B1E3D)
val Primary  = Color(0xFF1A6FD4)
val Indigo   = Color(0xFF3D5AFE)
val Surface  = Color(0xFFF0F5FB)
val Border   = Color(0xFFD4E3F5)
val TextMuted  = Color(0xFF5A7A9E)
val TextLabel  = Color(0xFF8BA4C0)
val Green    = Color(0xFF0F9A5A)
val GreenBg  = Color(0xFFE8FAF2)
val Red      = Color(0xFFE84855)
val RedBg    = Color(0xFFFEE8EA)
val Yellow   = Color(0xFFC87D00)
val YellowBg = Color(0xFFFFF8E6)
val BluLight = Color(0xFFEBF2FB)

private val LightColors = lightColorScheme(
    primary         = Primary,
    onPrimary       = Color.White,
    secondary       = Indigo,
    background      = Surface,
    surface         = Color.White,
    onBackground    = Navy,
    onSurface       = Navy,
)

@Composable
fun DDControlTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content     = content
    )
}