package logic

import upickle.default.*

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class Booking( bookingId:        String = "booking1",
                    items:            Set[String] = Set.empty,
                    bookingStartTime: LocalDateTime,
                    bookingEndTime:   LocalDateTime,
                    totalCost:        Double,
                    renter:           Renter
) derives ReadWriter

object Booking:
   implicit val dateTimeReadWrite: ReadWriter[LocalDateTime] =
      readwriter[String].bimap[LocalDateTime](
          dateTime => dateTime.format( DateTimeFormatter.ofPattern( "d-MM-yyyy HH:mm" ) ),
          str => LocalDateTime.parse( str, DateTimeFormatter.ofPattern( "d-MM-yyyy HH:mm" ) )
      )