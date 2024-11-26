// DateUtilsTest.kt
package com.github.se.orator.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class DateUtilsTest {

  @Test
  fun testGetCurrentDate() {
    val currentDate = getCurrentDate()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val expectedDate = sdf.parse(sdf.format(Date())) ?: throw ParseException("Invalid format", 0)
    assertEquals(expectedDate, currentDate)
  }

  @Test
  fun testParseDate_ValidDate() {
    val dateString = "2023-10-15"
    val date = parseDate(dateString)

    val calendar = Calendar.getInstance()
    calendar.time = date

    assertEquals(2023, calendar.get(Calendar.YEAR))
    assertEquals(Calendar.OCTOBER, calendar.get(Calendar.MONTH))
    assertEquals(15, calendar.get(Calendar.DAY_OF_MONTH))
  }

  @Test(expected = ParseException::class)
  fun testParseDate_InvalidDate() {
    val dateString = "invalid-date"
    parseDate(dateString)
  }

  @Test
  fun testFormatDate() {
    val calendar = Calendar.getInstance()
    calendar.set(2023, Calendar.OCTOBER, 15, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val date = calendar.time

    val formattedDate = formatDate(date)
    assertEquals("2023-10-15", formattedDate)
  }

  @Test
  fun testGetDaysDifference_SameDate() {
    val date = getCurrentDate()
    val daysDifference = getDaysDifference(date, date)
    assertEquals(0, daysDifference)
  }

  @Test
  fun testGetDaysDifference_OneDayApart() {
    val startDate = getCurrentDate()
    val calendar = Calendar.getInstance()
    calendar.time = startDate
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    val endDate = calendar.time

    val daysDifference = getDaysDifference(startDate, endDate)
    assertEquals(1, daysDifference)
  }

  @Test(expected = IllegalArgumentException::class)
  fun testGetDaysDifference_StartDateAfterEndDate() {
    val endDate = getCurrentDate()
    val calendar = Calendar.getInstance()
    calendar.time = endDate
    calendar.add(Calendar.DAY_OF_MONTH, 1) // startDate = endDate +1 day
    val startDate = calendar.time

    getDaysDifference(startDate, endDate)
  }

  @Test
  fun testGetDaysDifference_MultipleDays() {
    val startDate = getCurrentDate()
    val calendar = Calendar.getInstance()
    calendar.time = startDate
    calendar.add(Calendar.DAY_OF_MONTH, 10)
    val endDate = calendar.time

    val daysDifference = getDaysDifference(startDate, endDate)
    assertEquals(10, daysDifference)
  }

  @Test
  fun testParseDate_DifferentTimeZones() {
    val dateString = "2023-10-15"
    val date = parseDate(dateString)
    val formattedDate = formatDate(date)
    assertEquals(dateString, formattedDate)
  }

  @Test
  fun testGetDaysDifference_LargeDateDifference() {
    val startDate = parseDate("2000-01-01")
    val endDate = parseDate("2020-01-01")

    val daysDifference = getDaysDifference(startDate, endDate)
    assertEquals(7305, daysDifference) // 20 years including 5 leap days
  }

  @Test(expected = IllegalArgumentException::class)
  fun testGetDaysDifference_SameDayButStartAfterEnd() {
    val endDate = getCurrentDate()
    val calendar = Calendar.getInstance()
    calendar.time = endDate
    calendar.add(Calendar.HOUR, 1) // startDate = endDate +1 hour
    val startDate = calendar.time

    getDaysDifference(startDate, endDate)
  }

  @Test
  fun testGetDaysDifference_StartDateEqualsEndDate() {
    val date = getCurrentDate()
    val daysDifference = getDaysDifference(date, date)
    assertEquals(0, daysDifference)
  }

  @Test
  fun testGetCurrentDate_MidnightTime() {
    val currentDate = getCurrentDate()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val expectedDate = sdf.parse(sdf.format(Date())) ?: throw ParseException("Invalid format", 0)
    assertEquals(expectedDate, currentDate)
  }

  // Removed tests that pass null to non-nullable functions as Kotlin prevents passing nulls to
  // non-null parameters.
}
