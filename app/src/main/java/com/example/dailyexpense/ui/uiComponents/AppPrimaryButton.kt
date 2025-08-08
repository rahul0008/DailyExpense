package com.example.dailyexpense.ui.uiComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dailyexpense.ui.theme.DailyExpenseTheme

/**
 * A reusable Material Design primary button.
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
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        }
        AppText( // Using AppText for consistency, though Button's own text handling is usually fine
            text = text,
            style = textStyle,
            // Color is typically handled by ButtonColors, but AppText might try to apply onSurface
            // For Buttons, it's better to let ButtonColors dictate the text color.
            // So, we don't explicitly set color here unless AppText defaults are problematic.
            // If AppText's default color (e.g. onSurface) overrides button's text color,
            // you might need to pass `color = LocalContentColor.current` to AppText
            // or ensure AppText's default is Color.Unspecified when used in such contexts.
            textAlign = TextAlign.Center
        )
        if (trailingIcon != null) {
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            trailingIcon()
        }
    }
}


@Preview(showBackground = true, name = "App Buttons Preview")
@Composable
fun AppButtonsPreview() {
    DailyExpenseTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AppPrimaryButton(
                    text = "Submit Action",
                    onClick = { /*TODO*/ },
                    leadingIcon = { Icon(Icons.Filled.Add, contentDescription = "Add") }
                )

                AppPrimaryButton(
                    text = "Disabled Primary",
                    onClick = { /*TODO*/ },
                    enabled = false
                )

                AppTextButton(
                    text = "Learn More",
                    onClick = { /*TODO*/ }
                )

                AppTextButton(
                    text = "Disabled Text",
                    onClick = { /*TODO*/ },
                    enabled = false,
                    trailingIcon = { Icon(Icons.Filled.Add, contentDescription = "Add") }
                )

                AppOutlinedButton(
                    text = "Cancel Process",
                    onClick = { /*TODO*/ }
                )

                AppOutlinedButton(
                    text = "Disabled Outline",
                    onClick = { /*TODO*/ },
                    enabled = false
                )
            }
        }
    }
}
