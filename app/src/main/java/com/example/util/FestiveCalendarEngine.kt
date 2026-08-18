package com.jackattackk246.files.util

import java.util.Calendar
import java.util.Locale

/**
 * Local Region Calendar & Theme Adaptation Interceptor Engine
 * 100% Offline with zero external network tracking requests.
 */
object FestiveCalendarEngine {

  enum class FestiveEvent(val title: String, val holidayGreeting: String) {
    BONFIRE_NIGHT("Bonfire Night", "Happy Bonfire Night"),
    INDEPENDENCE_DAY("Independence Day", "Happy 4th of July"),
    ST_PATRICKS_DAY("St. Patrick's Day", "Happy St. Patrick's Day"),
    HALLOWEEN("Halloween", "Happy Halloween"),
    CHRISTMAS_EVE("Christmas Eve", "Merry Christmas Eve"),
    CHRISTMAS_DAY("Christmas Day", "Merry Christmas"),
    NEW_YEARS("New Year's", "Happy New Year"),
    NONE("", "")
  }

  /**
   * Resolves active localized festive event by checking offline device clock and country locale.
   */
  fun getActiveFestiveEvent(mockMonth: Int? = null, mockDay: Int? = null): FestiveEvent {
    val cal = Calendar.getInstance()
    val month = mockMonth ?: (cal.get(Calendar.MONTH) + 1) // 1-12
    val day = mockDay ?: cal.get(Calendar.DAY_OF_MONTH)
    val country = Locale.getDefault().country.uppercase(Locale.getDefault())

    return when {
      month == 11 && day == 5 && country == "GB" -> FestiveEvent.BONFIRE_NIGHT
      month == 7 && day == 4 && country == "US" -> FestiveEvent.INDEPENDENCE_DAY
      month == 3 && day == 17 -> FestiveEvent.ST_PATRICKS_DAY
      month == 10 && day == 31 -> FestiveEvent.HALLOWEEN
      month == 12 && day == 24 -> FestiveEvent.CHRISTMAS_EVE
      month == 12 && day == 25 -> FestiveEvent.CHRISTMAS_DAY
      month == 1 && day == 1 -> FestiveEvent.NEW_YEARS
      else -> FestiveEvent.NONE
    }
  }

  /**
   * Formats dynamic greeting string according to Step 4Z.103 rules.
   */
  fun getDynamicGreeting(userName: String? = null, mockHour: Int? = null, mockMonth: Int? = null, mockDay: Int? = null): String {
    val cal = Calendar.getInstance()
    val hour = mockHour ?: cal.get(Calendar.HOUR_OF_DAY)
    val festive = getActiveFestiveEvent(mockMonth, mockDay)

    val baseGreeting = when {
      festive != FestiveEvent.NONE -> festive.holidayGreeting
      hour in 0..11 -> "Good morning"
      hour in 12..16 -> "Good afternoon"
      hour in 17..21 -> "Good evening"
      else -> "Good night"
    }

    val trimmedUser = userName?.trim()
    return if (!trimmedUser.isNullOrEmpty()) {
      if (festive != FestiveEvent.NONE) {
        "$baseGreeting, $trimmedUser!"
      } else {
        "$baseGreeting, $trimmedUser"
      }
    } else {
      if (festive != FestiveEvent.NONE) {
        "$baseGreeting!"
      } else {
        "$baseGreeting,"
      }
    }
  }

  /**
   * Checks if December advent calendar milestones are active.
   */
  fun getDecemberAdventDay(mockMonth: Int? = null, mockDay: Int? = null): Int? {
    val cal = Calendar.getInstance()
    val month = mockMonth ?: (cal.get(Calendar.MONTH) + 1)
    val day = mockDay ?: cal.get(Calendar.DAY_OF_MONTH)
    return if (month == 12) day else null
  }
}
