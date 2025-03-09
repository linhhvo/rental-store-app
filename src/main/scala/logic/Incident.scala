package logic

import com.aventrix.jnanoid.jnanoid.NanoIdUtils.*
import upickle.default.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class Incident( incidentDate: LocalDate,
                     incidentType: IncidentType,
                     comment:      String,
                     repairCost:   Double,
                     incidentId:   String = randomNanoId()
) derives ReadWriter

object Incident {
   implicit val dateTimeReadWrite: ReadWriter[LocalDate] =
      readwriter[String].bimap[LocalDate](
          dateTime => dateTime.format( DateTimeFormatter.ofPattern( "d-MMM-yyyy" ) ),
          str => LocalDate.parse( str, DateTimeFormatter.ofPattern( "d-MMM-yyyy" ) )
      )

}