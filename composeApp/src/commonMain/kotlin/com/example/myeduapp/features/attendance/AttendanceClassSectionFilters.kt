package com.example.myeduapp.features.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myeduapp.core.ui.components.AppLoader
import com.example.myeduapp.core.ui.components.ClearFiltersButton
import com.example.myeduapp.core.ui.components.FilterOptionDropdown
import com.example.myeduapp.core.ui.theme.AttendanceDimens
import com.example.myeduapp.core.ui.theme.CardBackground
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.data.model.FilterOption
import com.example.myeduapp.data.model.GradeOption
import com.example.myeduapp.data.model.SchoolClass
import com.example.myeduapp.data.repository.ClassRepository

fun schoolClassFrom(grade: String, section: String): SchoolClass =
    SchoolClass(id = 0, grade = grade, section = section)

@Composable
fun rememberAttendanceClassSectionFilters(): AttendanceClassSectionState {
    val classRepository = remember { ClassRepository() }
    var grades by remember { mutableStateOf<List<GradeOption>>(emptyList()) }
    var sections by remember { mutableStateOf<List<FilterOption>>(emptyList()) }
    var selectedGrade by remember { mutableStateOf<String?>(null) }
    var selectedSection by remember { mutableStateOf<String?>(null) }
    var isLoadingGrades by remember { mutableStateOf(true) }

    val gradeOptions = remember(grades) {
        grades.map { FilterOption(value = it.value, label = it.label) }
    }

    LaunchedEffect(Unit) {
        isLoadingGrades = true
        classRepository.getGrades().onSuccess { grades = it }
        isLoadingGrades = false
    }

    LaunchedEffect(selectedGrade) {
        classRepository.getSections(selectedGrade).onSuccess { sections = it }
        if (selectedGrade == null) selectedSection = null
    }

    val selectedClass = remember(selectedGrade, selectedSection) {
        if (selectedGrade != null && selectedSection != null) {
            schoolClassFrom(selectedGrade!!, selectedSection!!)
        } else null
    }

    return AttendanceClassSectionState(
        gradeOptions = gradeOptions,
        sectionOptions = sections,
        selectedGrade = selectedGrade,
        selectedSection = selectedSection,
        selectedClass = selectedClass,
        isLoadingGrades = isLoadingGrades,
        isReady = selectedGrade != null && selectedSection != null,
        onGradeSelected = {
            selectedGrade = it
            selectedSection = null
        },
        onSectionSelected = { selectedSection = it },
        onClear = {
            selectedGrade = null
            selectedSection = null
        }
    )
}

data class AttendanceClassSectionState(
    val gradeOptions: List<FilterOption>,
    val sectionOptions: List<FilterOption>,
    val selectedGrade: String?,
    val selectedSection: String?,
    val selectedClass: SchoolClass?,
    val isLoadingGrades: Boolean,
    val isReady: Boolean,
    val onGradeSelected: (String?) -> Unit,
    val onSectionSelected: (String?) -> Unit,
    val onClear: () -> Unit
)

@Composable
fun AttendanceFiltersPanel(
    state: AttendanceClassSectionState,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasFilters = state.selectedGrade != null || state.selectedSection != null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = AttendanceDimens.ScreenHorizontal,
                vertical = AttendanceDimens.ScreenVertical
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(AttendanceDimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(AttendanceDimens.ItemSpacing)
        ) {
            Text(
                "Select Class & Date",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryBlue
            )

            if (state.isLoadingGrades) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AppLoader(size = 56.dp, message = "Loading classes")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AttendanceDimens.ChipSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterOptionDropdown(
                    label = "Class",
                    options = state.gradeOptions,
                    selectedValue = state.selectedGrade,
                    onOptionSelected = state.onGradeSelected,
                    allowAll = false,
                    modifier = Modifier.weight(1f)
                )
                FilterOptionDropdown(
                    label = "Section",
                    options = state.sectionOptions,
                    selectedValue = state.selectedSection,
                    onOptionSelected = state.onSectionSelected,
                    allowAll = false,
                    modifier = Modifier.weight(1f)
                )
                if (hasFilters) {
                    ClearFiltersButton(onClear = state.onClear)
                }
            }

            AttendanceDatePicker(
                selectedDate = selectedDate,
                onDateChange = onDateChange,
                label = "Attendance Date"
            )
        }
    }
}

/** @deprecated Use [AttendanceFiltersPanel] */
@Composable
fun AttendanceFilterBar(
    state: AttendanceClassSectionState,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit = {}
) {
    AttendanceFiltersPanel(
        state = state,
        selectedDate = selectedDate,
        onDateChange = onDateChange,
        modifier = modifier
    )
}

@Composable
fun AttendanceClassSectionFilters(
    state: AttendanceClassSectionState,
    modifier: Modifier = Modifier
) {
    val hasFilters = state.selectedGrade != null || state.selectedSection != null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = AttendanceDimens.ScreenHorizontal,
                vertical = AttendanceDimens.ScreenVertical
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AttendanceDimens.CardPadding),
            horizontalArrangement = Arrangement.spacedBy(AttendanceDimens.ChipSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterOptionDropdown(
                label = "Class",
                options = state.gradeOptions,
                selectedValue = state.selectedGrade,
                onOptionSelected = state.onGradeSelected,
                allowAll = false,
                modifier = Modifier.weight(1f)
            )
            FilterOptionDropdown(
                label = "Section",
                options = state.sectionOptions,
                selectedValue = state.selectedSection,
                onOptionSelected = state.onSectionSelected,
                allowAll = false,
                modifier = Modifier.weight(1f)
            )
            if (hasFilters) {
                ClearFiltersButton(onClear = state.onClear)
            }
        }
    }
}
