// DateUtils.kt
package com.github.se.orator.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Returns the current date with the time set to midnight. */
fun getCurrentDate(): Date {
  val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  sdf.timeZone = TimeZone.getDefault()
  return sdf.parse(sdf.format(Date())) ?: throw ParseException("Unable to get current date", 0)
}

/**
 * Parses a date string in "yyyy-MM-dd" format to a Date object.
 *
 * @param dateString The date string to parse.
 * @return The parsed Date object.
 * @throws ParseException if the date string is invalid.
 */
fun parseDate(dateString: String): Date {
  val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  sdf.timeZone = TimeZone.getDefault()
  return sdf.parse(dateString) ?: throw ParseException("Invalid date format: $dateString", 0)
}

/**
 * Formats a Date object to a string in "yyyy-MM-dd" format.
 *
 * @param date The Date object to format.
 * @return The formatted date string.
 */
fun formatDate(date: Date): String {
  val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  sdf.timeZone = TimeZone.getDefault()
  return sdf.format(date)
}

/**
 * Calculates the difference in days between two dates.
 *
 * @param startDate The start date. Must not be null.
 * @param endDate The end date. Must not be null.
 * @return The difference in days.
 * @throws IllegalArgumentException if startDate is after endDate.
 */
fun getDaysDifference(startDate: Date, endDate: Date): Long {
  if (startDate.after(endDate)) {
    throw IllegalArgumentException("startDate must be before or equal to endDate")
  }
  val diffInMillis = endDate.time - startDate.time
  return diffInMillis / (1000 * 60 * 60 * 24)
}
