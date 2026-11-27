package com.example.financetracker.core.uikit.components.input

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme

@Composable
fun AmountInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Сумма",
    placeholder: String = "0",
    isError: Boolean = false,
    errorMessage: String? = null,
    focusRequester: FocusRequester? = null,
    onDone: (() -> Unit)? = null,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
            val separatorCount = filtered.count { it == '.' || it == ',' }
            if (separatorCount <= 1) {
                val separatorIndex = filtered.indexOfFirst { it == '.' || it == ',' }
                val decimalLength = if (separatorIndex != -1) filtered.length - separatorIndex - 1 else 0
                if (decimalLength <= 2) {
                    onValueChange(filtered)
                } else if (input.length - value.length > 1) {
                    // При вставке (paste) числа с лишними знаками — обрезаем до 2 знаков после запятой
                    onValueChange(filtered.substring(0, separatorIndex + 3))
                }
            }
        },
        readOnly = readOnly,
        enabled = enabled,
        trailingIcon = trailingIcon,
        label = { Text(label) },
        placeholder = {
            Text(
                text = placeholder,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            )
        },
        suffix = {
            Text(
                text = "₽",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        textStyle = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        ),
        visualTransformation = ThousandsSeparatorVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                onDone?.invoke()
            }
        ),
        isError = isError,
        supportingText = if (isError && !errorMessage.isNullOrBlank()) {
            { Text(errorMessage) }
        } else null,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
    )
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun AmountInputFieldLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AmountInputField(
                    value = "1500",
                    onValueChange = {}
                )
                AmountInputField(
                    value = "",
                    onValueChange = {},
                    isError = true,
                    errorMessage = "Укажите сумму"
                )
            }
        }
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun AmountInputFieldDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AmountInputField(
                    value = "1500",
                    onValueChange = {}
                )
                AmountInputField(
                    value = "",
                    onValueChange = {},
                    isError = true,
                    errorMessage = "Укажите сумму"
                )
            }
        }
    }
}

