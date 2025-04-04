package logic

import com.aventrix.jnanoid.jnanoid.NanoIdUtils.*
import upickle.default.*

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.mutable.Buffer

@upickle.implicits.serializeDefaults( true )
case class Booking( items:            Buffer[SavedItem] = Buffer.empty,
                    bookingStartTime: LocalDateTime,
                    bookingEndTime:   LocalDateTime,
                    totalCost:        Double,
                    renter:           String,
                    bookingId:        String = randomNanoId()
) derives ReadWriter:
   def getItems = this.items.flatMap( _.getDetailsFromItemList )

object Booking:
   implicit val dateTimeReadWrite: ReadWriter[LocalDateTime] =
      readwriter[String].bimap[LocalDateTime](
          dateTime => dateTime.format( DateTimeFormatter.ofPattern( "d-MM-yyyy HH:mm" ) ),
          str => LocalDateTime.parse( str, DateTimeFormatter.ofPattern( "d-MM-yyyy HH:mm" ) )
      )