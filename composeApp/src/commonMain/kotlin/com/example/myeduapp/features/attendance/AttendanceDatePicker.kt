package com.example.myeduapp.features.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myeduapp.core.ui.theme.Background
import com.example.myeduapp.core.ui.theme.CardBackground
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceDatePicker(
    selectedDate: String,
    onDateChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Attendance Date",
    compact: Boolean = false
) {
    var showPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    AttendanceDatePickerField(
        selectedDate = selectedDate,
        label = label,
        onClick = { showPicker = true },
        modifier = modifier,
        compact = compact
    )

    if (showPicker) {
        ModalBottomSheet(
            onDismissRequest = { showPicker = false },
            sheetState = sheetState,
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BottomSheetDefaults.DragHandle(color = PrimaryBlue.copy(alpha = 0.35f))
                    Text(
                        "Select Date",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        ) {
            YearMonthDayPicker(
                selectedDate = selectedDate,
                onConfirm = { date ->
                    onDateChange(date)
                    showPicker = false
                },
                modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
fun AttendanceDatePickerField(
    selectedDate: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    if (compact) {
        OutlinedCard(
            modifier = modifier
                .fillMaxWidth()
                .height(36.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = CardBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    DateUtils.formatShort(selectedDate),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = PrimaryBlue,
                    maxLines = 1
                )
            }
        }
    } else {
        OutlinedCard(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (label.isNotBlank()) {
                        Text(label, fontSize = 12.sp, color = SecondaryText, fontWeight = FontWeight.Medium)
                    }
                    Text(
                        DateUtils.formatLongDisplay(selectedDate),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = PrimaryBlue
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Open date picker",
                    tint = SecondaryText,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun YearMonthDayPicker(
    selectedDate: String,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val parsed = remember(selectedDate) { DateUtils.parseParts(selectedDate) }
    var year by remember(parsed) { mutableIntStateOf(parsed.year) }
    var month by remember(parsed) { mutableIntStateOf(parsed.month) }
    var day by remember(parsed) { mutableIntStateOf(parsed.day) }

    val years = remember { DateUtils.yearRange() }
    val months = remember { DateUtils.monthOptions() }
    val days = remember(year, month) { DateUtils.dayOptions(year, month) }

    LaunchedEffect(year, month) {
        val maxDay = days.lastOrNull()?.value ?: 1
        if (day > maxDay) day = maxDay
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Background)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DateWheelColumn(
                label = "Year",
                items = years.map { it.toString() },
                selectedIndex = years.indexOf(year).coerceAtLeast(0),
                onSelected = { year = years[it] },
                modifier = Modifier.weight(1f)
            )
            DateWheelColumn(
                label = "Month",
                items = months.map { it.label },
                selectedIndex = months.indexOfFirst { it.value == month }.coerceAtLeast(0),
                onSelected = { month = months[it].value },
                modifier = Modifier.weight(1.2f)
            )
            DateWheelColumn(
                label = "Date",
                items = days.map { it.label },
                selectedIndex = days.indexOfFirst { it.value == day }.coerceAtLeast(0),
                onSelected = { day = days[it].value },
                modifier = Modifier.weight(0.8f)
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val today = DateUtils.parseParts(DateUtils.today())
                    year = today.year
                    month = today.month
                    day = today.day
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
            ) {
                Text("Today")
            }
            Button(
                onClick = { onConfirm(DateUtils.buildDate(year, month, day)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                )
            ) {
                Text("Confirm")
            }
        }
    }
}

@Composable
private fun DateWheelColumn(
    label: String,
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0 && items.isNotEmpty()) {
            listState.scrollToItem(selectedIndex.coerceIn(0, items.lastIndex))
        }
    }

    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = SecondaryText,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardBackground)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(PrimaryBlue.copy(alpha = 0.08f))
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(items) { index, item ->
                    val isSelected = index == selectedIndex
                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelected(index)
                                scope.launch { listState.animateScrollToItem(index) }
                            }
                            .padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        fontSize = if (isSelected) 16.sp else 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) PrimaryBlue else SecondaryText
                    )
                }
            }
        }
    }
}
