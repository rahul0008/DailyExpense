package com.example.dailyexpense.ui.uiComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview // Ensure this import is present
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp // Ensure this import is present
import androidx.compose.ui.unit.sp
import com.example.dailyexpense.ui.theme.DailyExpenseTheme // Import your app's theme

@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    preferredFontSize: TextUnit = TextUnit.Unspecified,
    minFontSize: TextUnit = 10.sp,
    maxLines: Int = 1,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    style: TextStyle = LocalTextStyle.current,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    // Determine the initial font size: from style, then preferredFontSize, then a default.
    val initialFontSize = remember(style, preferredFontSize) {
        if (style.fontSize != TextUnit.Unspecified) {
            style.fontSize
        } else if (preferredFontSize != TextUnit.Unspecified) {
            preferredFontSize
        } else {
            16.sp // Default fallback if no other size is specified
        }
    }

    var currentFontSize by remember { mutableStateOf(initialFontSize) }
    var hasFitted by remember { mutableStateOf(false) }

    // Create the effective text style, merging passed parameters with the base style
    val effectiveTextStyle = remember(
        style, currentFontSize, fontStyle, fontWeight, fontFamily, letterSpacing, lineHeight
    ) {
        style.merge(
            TextStyle(
                color = if (color != Color.Unspecified) color else style.color,
                fontSize = currentFontSize,
                fontStyle = fontStyle ?: style.fontStyle,
                fontWeight = fontWeight ?: style.fontWeight,
                fontFamily = fontFamily ?: style.fontFamily,
                letterSpacing = if (letterSpacing != TextUnit.Unspecified) letterSpacing else style.letterSpacing,
                lineHeight = if (lineHeight != TextUnit.Unspecified) lineHeight else style.lineHeight,
                textAlign = textAlign ?: style.textAlign,
                textDecoration = textDecoration ?: style.textDecoration
            )
        )
    }

    Text(
        text = text,
        modifier = modifier,
        color = if (color != Color.Unspecified) color else Color.Unspecified,
        fontSize = TextUnit.Unspecified,
        fontStyle = null,
        fontWeight = null,
        fontFamily = null,
        letterSpacing = TextUnit.Unspecified,
        textDecoration = null,
        textAlign = textAlign,
        lineHeight = TextUnit.Unspecified,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        style = effectiveTextStyle,
        onTextLayout = { textLayoutResult ->
            if (!hasFitted && currentFontSize > minFontSize) {
                if (textLayoutResult.hasVisualOverflow) {
                    val newSize = currentFontSize * 0.95f
                    currentFontSize = if (newSize >= minFontSize) newSize else minFontSize
                } else {
                    hasFitted = true
                }
            } else if (currentFontSize <= minFontSize && textLayoutResult.hasVisualOverflow) {
                hasFitted = true
            } else if (!textLayoutResult.hasVisualOverflow) {
                hasFitted = true
            }
            onTextLayout(textLayoutResult)
        }
    )

    LaunchedEffect(text, initialFontSize, minFontSize, maxLines, style, textAlign, overflow, softWrap) {
        hasFitted = false
        currentFontSize = initialFontSize
    }
}


@Preview(showBackground = true, widthDp = 300)
@Composable
fun AppTextPreview() {
    DailyExpenseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AppText(
                    text = "Short & Sweet",
                    modifier = Modifier.width(150.dp),
                    style = MaterialTheme.typography.titleMedium,
                    preferredFontSize = 20.sp,
                    minFontSize = 10.sp,
                    textAlign = TextAlign.Center
                )

                AppText(
                    text = "This is a longer piece of text that will definitely need to shrink to fit.",
                    modifier = Modifier.width(200.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    minFontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                AppText(
                    text = "Primary Color Text",
                    modifier = Modifier.width(180.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    preferredFontSize = 18.sp,
                    minFontSize = 10.sp,
                    textAlign = TextAlign.Left
                )
                AppText(
                    text = "Fill Width Example, MaxLines 1",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelLarge,
                    preferredFontSize = 22.sp,
                    minFontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
