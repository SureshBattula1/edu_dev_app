package com.example.myeduapp.features.leaves

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myeduapp.core.ui.components.AppLoaderCompact
import com.example.myeduapp.core.util.DateUtils
import com.example.myeduapp.data.model.LeaveCategory
import com.example.myeduapp.data.model.LeaveTypes
import com.example.myeduapp.features.attendance.AttendanceDatePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLeaveSheet(
    category: LeaveCategory,
    onDismiss: () -> Unit,
    onSubmit: (leaveType: String, fromDate: String, toDate: String, reason: String, remarks: String) -> Unit,
    isSubmitting: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val leaveTypes = when (category) {
        LeaveCategory.STUDENT -> LeaveTypes.STUDENT
        LeaveCategory.TEACHER -> LeaveTypes.TEACHER
    }

    var leaveType by remember { mutableStateOf(leaveTypes.first()) }
    var fromDate by remember { mutableStateOf(DateUtils.currentLocalDate().toString()) }
    var toDate by remember { mutableStateOf(DateUtils.currentLocalDate().toString()) }
    var reason by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }

    val isValid = fromDate.isNotBlank() && toDate.isNotBlank() && reason.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                if (category == LeaveCategory.STUDENT) "Apply Student Leave" else "Apply Teacher Leave",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                OutlinedTextField(
                    value = leaveType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Leave Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    leaveTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                leaveType = type
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            AttendanceDatePicker(
                selectedDate = fromDate,
                onDateChange = { fromDate = it },
                label = "From Date"
            )
            AttendanceDatePicker(
                selectedDate = toDate,
                onDateChange = { toDate = it },
                label = "To Date"
            )

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("Remarks (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Button(
                onClick = { onSubmit(leaveType, fromDate, toDate, reason, remarks) },
                enabled = isValid && !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isSubmitting) {
                    AppLoaderCompact(size = 28.dp)
                } else {
                    Text("Submit Application")
                }
            }
        }
    }
}
