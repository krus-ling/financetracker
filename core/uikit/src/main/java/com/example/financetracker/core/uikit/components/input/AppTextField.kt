package com.example.financetracker.core.uikit.components.input

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
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.uikit.theme.FinancetrackerTheme

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    focusRequester: FocusRequester? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
) {
    // Вычисляем supportingText: если передан кастомный слот — используем его,
    // иначе если есть ошибка и текст ошибки — выводим сообщение об ошибке
    val effectiveSupportingText: (@Composable () -> Unit)? = when {
        supportingText != null -> supportingText
        isError && !errorMessage.isNullOrBlank() -> {
            { Text(errorMessage) }
        }
        else -> null
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = effectiveSupportingText,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = MaterialTheme.shapes.medium,
        colors = colors
    )
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun AppTextFieldLightPreview() {
    FinancetrackerTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTextField(
                value = "Основной счёт",
                onValueChange = {},
                label = "Название счёта",
                placeholder = "Введите название"
            )
            AppTextField(
                value = "",
                onValueChange = {},
                label = "Сумма",
                placeholder = "0.00",
                isError = true,
                errorMessage = "Поле не может быть пустым"
            )
        }
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun AppTextFieldDarkPreview() {
    FinancetrackerTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppTextField(
                    value = "Основной счёт",
                    onValueChange = {},
                    label = "Название счёта",
                    placeholder = "Введите название"
                )
                AppTextField(
                    value = "",
                    onValueChange = {},
                    label = "Сумма",
                    placeholder = "0.00",
                    isError = true,
                    errorMessage = "Поле не может быть пустым"
                )
            }
        }
    }
}