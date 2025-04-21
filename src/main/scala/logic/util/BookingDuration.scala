package logic.util

import java.time.{Duration, LocalDateTime}

/** Calculate duration between two given dates */
class BookingDuration( val start: LocalDateTime, val end: LocalDateTime ):
   val durationInWeeks = Duration.between( start, end ).toDays.toInt / 7
   val durationInDays = Duration.between( start, end ).toDays.toInt   % 7
   val durationInHours = Duration.between( start, end ).toHours.toInt % 24