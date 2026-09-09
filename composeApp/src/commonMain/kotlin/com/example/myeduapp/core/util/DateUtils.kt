package com.example.myeduapp.core.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

object DateUtils {
    private val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    fun today(): String = currentLocalDate().toString()

    fun currentLocalDate(): LocalDate {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    fun parse(date: String): LocalDate = LocalDate.parse(date)

    fun formatDisplay(date: String): String {
        return try {
            val parts = date.split("-")
            if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else date
        } catch (_: Exception) {
            date
        }
    }

    fun formatShort(date: String): String {
        return try {
            val parts = date.split("-")
            if (parts.size == 3) "${parts[2]}/${parts[1]}" else date
        } catch (_: Exception) {
            date
        }
    }

    fun formatLongDisplay(date: String): String {
        return try {
            val local = parse(date)
            val dayName = dayNames.getOrElse(dayOfWeekIndex(local.dayOfWeek)) { "" }
            val monthName = monthNames.getOrElse(local.monthNumber - 1) { "" }
            "$dayName, ${local.dayOfMonth} $monthName ${local.year}"
        } catch (_: Exception) {
            formatDisplay(date)
        }
    }

    fun formatMonthYear(year: Int, month: Month): String {
        return "${monthNames.getOrElse(monthNumber(month) - 1) { month.name }} $year"
    }

    fun shiftDate(date: String, days: Int): String {
        val local = parse(date)
        val shifted = if (days >= 0) local.plus(DatePeriod(days = days)) else local.minus(DatePeriod(days = -days))
        return shifted.toString()
    }

    fun shiftMonth(date: String, months: Int): String {
        val local = parse(date)
        val shifted = if (months >= 0) local.plus(DatePeriod(months = months)) else local.minus(DatePeriod(months = -months))
        return shifted.toString()
    }

    fun daysInMonth(year: Int, month: Month): Int {
        return when (month) {
            Month.FEBRUARY -> if (isLeapYear(year)) 29 else 28
            Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
            else -> 31
        }
    }

    fun calendarCells(year: Int, month: Month): List<LocalDate?> {
        val firstDay = LocalDate(year, month, 1)
        val leadingBlanks = dayOfWeekIndex(firstDay.dayOfWeek)
        val totalDays = daysInMonth(year, month)
        val cells = mutableListOf<LocalDate?>()
        repeat(leadingBlanks) { cells.add(null) }
        for (day in 1..totalDays) {
            cells.add(LocalDate(year, month, day))
        }
        while (cells.size % 7 != 0) cells.add(null)
        return cells
    }

    fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    fun dayHeaders(): List<String> = dayNames

    fun isToday(date: LocalDate): Boolean = date == currentLocalDate()

    fun isSameDay(a: String, b: String): Boolean = a == b

    data class DateParts(val year: Int, val month: Int, val day: Int)

    fun parseParts(date: String): DateParts {
        val local = parse(date)
        return DateParts(local.year, monthNumber(local.month), local.dayOfMonth)
    }

    fun buildDate(year: Int, month: Int, day: Int): String {
        val monthEnum = monthFromNumber(month)
        val safeDay = day.coerceIn(1, daysInMonth(year, monthEnum))
        return LocalDate(year, monthEnum, safeDay).toString()
    }

    fun yearRange(pastYears: Int = 5, futureYears: Int = 1): List<Int> {
        val current = currentLocalDate().year
        return (current - pastYears..current + futureYears).toList()
    }

    data class PickerOption(val value: Int, val label: String)

    fun monthOptions(): List<PickerOption> =
        monthNames.mapIndexed { index, name -> PickerOption(index + 1, name) }

    fun dayOptions(year: Int, month: Int): List<PickerOption> {
        val count = daysInMonth(year, monthFromNumber(month))
        return (1..count).map { PickerOption(it, it.toString().padStart(2, '0')) }
    }

    private fun monthFromNumber(number: Int): Month = when (number) {
        1 -> Month.JANUARY
        2 -> Month.FEBRUARY
        3 -> Month.MARCH
        4 -> Month.APRIL
        5 -> Month.MAY
        6 -> Month.JUNE
        7 -> Month.JULY
        8 -> Month.AUGUST
        9 -> Month.SEPTEMBER
        10 -> Month.OCTOBER
        11 -> Month.NOVEMBER
        12 -> Month.DECEMBER
        else -> Month.JANUARY
    }

    private fun dayOfWeekIndex(dayOfWeek: DayOfWeek): Int = when (dayOfWeek) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
    }

    private fun monthNumber(month: Month): Int = when (month) {
        Month.JANUARY -> 1
        Month.FEBRUARY -> 2
        Month.MARCH -> 3
        Month.APRIL -> 4
        Month.MAY -> 5
        Month.JUNE -> 6
        Month.JULY -> 7
        Month.AUGUST -> 8
        Month.SEPTEMBER -> 9
        Month.OCTOBER -> 10
        Month.NOVEMBER -> 11
        Month.DECEMBER -> 12
    }
}
