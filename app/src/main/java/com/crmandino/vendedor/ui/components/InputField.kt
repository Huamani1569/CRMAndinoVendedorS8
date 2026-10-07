package com.crmandino.vendedor.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation

// Campo de texto reutilizable con etiqueta, íconos y mensaje de error debajo.
@Composable
fun InputField(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    iconoInicio: @Composable (() -> Unit)? = null,
    iconoFin: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    habilitado: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        leadingIcon = iconoInicio,
        trailingIcon = iconoFin,
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        singleLine = true,
        enabled = habilitado,
        modifier = modifier.fillMaxWidth()
    )
}
