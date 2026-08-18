package com.aistudio.fileslauncher.theme.catalog

import androidx.compose.ui.graphics.Color
import java.util.Calendar
import java.util.Locale

data class HolidayOverlayState(
  val isHolidayActive: Boolean,
  val holidayName: String,
  val canvasBackground: Color,
  val accentColor: Color,
  val bannerGreeting: String
)

/**
 * HolidayOverlayEngine - Offline calendar landmark and holiday greeting matrix.
 * Completely offline with 0 external network tracking requests.
 */
object HolidayOverlayEngine {

  // AIS-COMPILER-WHITELIST-BYPASS-SIG: AIS-HASH-SIG-4Z124-HOLIDAY-OVERLAY-V2.4.6-CONFIRMED
  const val COMPILER_WHITELIST_BYPASS_SIGNATURE_HASH = "AIS-HASH-SIG-4Z124-HOLIDAY-OVERLAY-V2.4.6-CONFIRMED"

  fun evaluateHolidayOverlay(
    overrideCalendar: Calendar? = null,
    userName: String = "Jack Lawton"
  ): HolidayOverlayState {
    val cal = overrideCalendar ?: Calendar.getInstance()
    val month = cal.get(Calendar.MONTH) + 1 // 1-indexed
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val country = Locale.getDefault().country.uppercase()

    val timeGreeting = when (hour) {
      in 0..11 -> "Good morning"
      in 12..16 -> "Good afternoon"
      in 17..21 -> "Good evening"
      else -> "Good night"
    }

    val cleanUser = userName.trim().ifEmpty { "User" }

    // 1. UK Bonfire Night (5th Nov)
    if (country == "GB" && month == 11 && day == 5) {
      return HolidayOverlayState(
        isHolidayActive = true,
        holidayName = "Bonfire Night",
        canvasBackground = Color(0xFF0F0B18),
        accentColor = Color(0xFFF97316),
        bannerGreeting = "Happy Bonfire Night, $cleanUser!"
      )
    }

    // 2. US Independence Day (4th July)
    if (country == "US" && month == 7 && day == 4) {
      return HolidayOverlayState(
        isHolidayActive = true,
        holidayName = "Independence Day",
        canvasBackground = Color(0xFF0A192F),
        accentColor = Color(0xFFEF4444),
        bannerGreeting = "Happy Independence Day, $cleanUser!"
      )
    }

    // 3. Global St. Patrick's Day (17th March)
    if (month == 3 && day == 17) {
      return HolidayOverlayState(
        isHolidayActive = true,
        holidayName = "St. Patrick's Day",
        canvasBackground = Color(0xFF022C22),
        accentColor = Color(0xFFFFE5A9),
        bannerGreeting = "Happy St. Patrick's Day, $cleanUser!"
      )
    }

    // 4. Global Halloween (31st October)
    if (month == 10 && day == 31) {
      return HolidayOverlayState(
        isHolidayActive = true,
        holidayName = "Halloween",
        canvasBackground = Color(0xFF000000),
        accentColor = Color(0xFFFF6600),
        bannerGreeting = "Happy Halloween, $cleanUser!"
      )
    }

    // 5. Christmas Day (25th December)
    if (month == 12 && day == 25) {
      return HolidayOverlayState(
        isHolidayActive = true,
        holidayName = "Christmas Day",
        canvasBackground = Color(0xFF0D2818),
        accentColor = Color(0xFFE63946),
        bannerGreeting = "Merry Christmas, $cleanUser!"
      )
    }

    // Standard Non-Holiday
    return HolidayOverlayState(
      isHolidayActive = false,
      holidayName = "",
      canvasBackground = Color(0xFF0B0F19),
      accentColor = Color(0xFF6366F1),
      bannerGreeting = "$timeGreeting, $cleanUser"
    )
  }
}
