package com.example.dailyexpense.ui.uiComponents

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign


/**
 * A reusable Material Design outlined button.
 *
 * @param text The text to display on the button.
 * @param onClick The callback to be invoked when this button is clicked.
 * @param modifier Modifier for this button.
 * @param enabled Controls the enabled state of the button. When `false`, this button will not be clickable.
 * @param textStyle The TextStyle for the button text. Defaults to MaterialTheme.typography.labelLarge.
 * @param colors ButtonColors that will be used to resolve the colors for this button in different states.
 * @param border BorderStroke to draw the border of this button.
 * @param contentPadding The spacing values to apply internally between the button container and the content.
 * @param leadingIcon Optional icon to display at the start of the button text.
 * @param trailingIcon Optional icon to display at the end of the button text.
 */
@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    border: androidx.compose.foundation.BorderStroke? = ButtonDefaults.outlinedButtonBorder,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        border = border,
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
