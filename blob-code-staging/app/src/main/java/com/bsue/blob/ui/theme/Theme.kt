package com.bsue.blob.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 항상 밝은 테마 (출동 화면만 어두움). 색은 원래 팔레트 그대로.
private val BlobColors = lightColorScheme(
    primary = BlobOrange,
    onPrimary = Color.White,
    primaryContainer = BlobOrange.copy(alpha = 0.1f),
    onPrimaryContainer = BlobOrange,
    secondary = SilhouetteGray,
    onSecondary = BlobBlack,
    background = BlobCream,
    onBackground = BlobBlack,
    surface = Color.White,
    onSurface = BlobBlack,
    surfaceVariant = BlobCream,
    onSurfaceVariant = BlobGray,
    outline = SilhouetteInk,
    outlineVariant = SilhouetteGray,
    scrim = BlobBlack,
    // 빨간 오류색 없음 (혼내는 색이 없다)
    error = BlobOrange,
    onError = Color.White,
)

@Composable
fun BlobTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BlobColors,
        typography = BsueTypography,
        shapes = BsueShapes,
        content = content,
    )
}
