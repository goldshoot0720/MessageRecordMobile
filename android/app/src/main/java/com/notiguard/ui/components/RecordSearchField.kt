package com.notiguard.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.notiguard.ui.theme.NG

@Composable
fun RecordSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    label: String = "搜尋應用程式或通知內容",
    modifier: Modifier = Modifier,
) {
    val focus = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "清除搜尋")
                }
            }
        },
        shape = NG.buttonShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = NG.ink,
            unfocusedTextColor = NG.ink,
            focusedBorderColor = NG.blueLight,
            unfocusedBorderColor = NG.inkFaint,
            focusedLabelColor = NG.blueLight,
            unfocusedLabelColor = NG.inkMuted,
            focusedLeadingIconColor = NG.blueLight,
            unfocusedLeadingIconColor = NG.inkMuted,
            focusedTrailingIconColor = NG.inkMuted,
            unfocusedTrailingIconColor = NG.inkMuted,
            cursorColor = NG.blueLight,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
    )
}
