package com.jack.neuroapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

@Composable
fun NeuroappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColors(
            primary = Purple80,
            secondary = PurpleGrey80,
            background = Pink80
        )
    } else {
        lightColors(
            primary = Purple40,
            secondary = PurpleGrey40,
            background = Pink40
        )
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        content = content
    )
}