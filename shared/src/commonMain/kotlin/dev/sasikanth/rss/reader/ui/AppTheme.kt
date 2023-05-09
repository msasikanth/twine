package dev.sasikanth.rss.reader.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.compose.fontFamilyResource
import dev.sasikanth.rss.reader.CommonRes

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val fontFamily = fontFamilyResource(CommonRes.fonts.golos.medium)
    MaterialTheme(
        colorScheme = darkColorScheme(),
        typography = typography(fontFamily),
        content = content
    )
}
