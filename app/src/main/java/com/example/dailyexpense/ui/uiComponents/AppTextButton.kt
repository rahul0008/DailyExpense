package com.example.dailyexpense.ui.uiComponents

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons // For preview
import androidx.compose.material.icons.filled.Add // For preview
import com.example.dailyexpense.ui.theme.DailyExpenseTheme // Your app's theme


/**
 * A reusable Material Design text button.
 *
 * @param text The text to display on the button.
 * @param onClick The callback to be invoked when this button is clicked.
 * @param modifier Modifier for this button.
 * @param enabled Controls the enabled state of the button. When `false`, this button will not be clickable.
 * @param textStyle The TextStyle for the button text. Defaults to MaterialTheme.typography.labelLarge.
 * @param colors ButtonColors that will be used to resolve the colors for this button in different states.
 * @param contentPadding The spacing values to apply internally between the button container and the content.
 * @param leadingIcon Optional icon to display at the start of the button text.
 * @param trailingIcon Optional icon to display at the end of the button text.
 */
@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        }
        AppText(
            text = text,
            style = textStyle,
            textAlign = TextAlign.Center
            // Similar color consideration as in AppPrimaryButton
        )
        if (trailingIcon != null) {
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            trailingIcon()
        }
    }
}