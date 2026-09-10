package com.example.myeduapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.data.model.FilterOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val filterOptions = remember(options) {
        options.map { FilterOption(value = it, label = it) }
    }
    FilterOptionDropdown(
        label = label,
        options = filterOptions,
        selectedValue = selectedOption,
        onOptionSelected = onOptionSelected,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterOptionDropdown(
    label: String,
    options: List<FilterOption>,
    selectedValue: String?,
    onOptionSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    allowAll: Boolean = true,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = options.find { it.value == selectedValue }?.label ?: "All $label"

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = if (selectedValue == null) "All $label" else displayText,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            if (allowAll) {
                DropdownMenuItem(
                    text = { Text("All $label", style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        onOptionSelected(null)
                        expanded = false
                    }
                )
            }
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label, style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        onOptionSelected(option.value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ClearFiltersButton(
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClear,
        modifier = modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape)
    ) {
        Icon(
            Icons.Default.Refresh,
            contentDescription = "Clear Filters",
            tint = MaterialTheme.colorScheme.error
        )
    }
}
