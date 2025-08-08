package com.example.dailyexpense.ui.uiComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dailyexpense.ui.theme.DailyExpenseTheme

/**
 * A reusable Material Design TextField composable with an optional heading.
 *
 * @param value The input text to be shown in the text field.
 * @param onValueChange The callback that is triggered when the input service updates the text.
 * @param modifier Modifier for the entire component (Column).
 * @param heading An optional text to display as a heading above the TextField. If null, no heading is shown.
 * @param headingStyle The TextStyle for the heading. Defaults to MaterialTheme.typography.titleMedium.
 * @param headingColor The color of the heading text. Defaults to MaterialTheme.colorScheme.onSurface.
 * @param textFieldModifier Modifier specifically for the TextField.
 * @param enabled Controls the enabled state of the TextField.
 * @param readOnly Controls the read-only state of the TextField.
 * @param textStyle The TextStyle to be applied to the input text. Defaults to LocalTextStyle.
 * @param label The label to be displayed inside the TextField container.
 * @param placeholder The placeholder to be displayed when the text field is in focus and the input text is empty.
 * @param leadingIcon The optional leading icon to be displayed at the beginning of the text field container.
 * @param trailingIcon The optional trailing icon to be displayed at the end of the text field container.
 * @param supportingText The optional supporting text to be displayed below the text field.
 * @param isError Indicates if the TextField's current value is in error.
 * @param visualTransformation Transforms the visual representation of the input value.
 * @param keyboardOptions Software keyboard options that instruct the keyboard how to behave.
 * @param keyboardActions When the input service emits an IME action, the corresponding callback is called.
 * @param singleLine When set to true, this text field becomes a single horizontally scrolling text field.
 * @param maxLines The maximum number of lines to be displayed in the TextField.
 * @param minLines The minimum number of lines for the TextField.
 * @param colors Colors that will be used to resolve color values for this text field in different states.
 */
@Composable
fun AppTextField( // Renamed from HeadingTextField
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    heading: String? = null,
    headingStyle: TextStyle = MaterialTheme.typography.titleMedium,
    headingColor: Color = Color.Unspecified,
    textFieldModifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    colors: TextFieldColors = TextFieldDefaults.colors()
) {
    Column(modifier = modifier) {
        if (heading != null) {
            AppText( // Still uses AppText for the heading internally
                text = heading,
                style = headingStyle,
                color = headingColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = textFieldModifier,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            supportingText = supportingText,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            colors = colors
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppTextFieldPreview() { // Renamed Preview function
    DailyExpenseTheme {
        Surface(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                var text1 by remember { mutableStateOf("") }
                AppTextField( // Using the new name
                    heading = "Full Name",
                    value = text1,
                    onValueChange = { text1 = it },
                    label = { Text("Enter your full name") },
                    placeholder = { Text("e.g., Jane Doe") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                var text2 by remember { mutableStateOf("Default Value") }
                AppTextField( // Using the new name
                    value = text2,
                    onValueChange = { text2 = it },
                    label = { Text("Notes (No Heading)") },
                    singleLine = false,
                    maxLines = 3,
                    supportingText = { Text("Max 3 lines.")}
                )

                var text3 by remember { mutableStateOf("") }
                AppTextField( // Using the new name
                    heading = "Email Address",
                    value = text3,
                    onValueChange = { text3 = it },
                    label = { Text("Email") },
                    isError = text3.isNotEmpty() && !text3.contains("@"),
                    supportingText = {
                        if (text3.isNotEmpty() && !text3.contains("@")) {
                            Text("Please enter a valid email.", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                var text4 by remember { mutableStateOf("")}
                AppTextField( // Using the new name
                    value = text4,
                    onValueChange = { text4 = it },
                    label = { Text("Just a simple field")},
                    placeholder = {Text("No heading, just placeholder & label")}
                )
            }
        }
    }
}

