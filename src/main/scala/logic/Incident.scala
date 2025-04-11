package logic

import com.aventrix.jnanoid.jnanoid.NanoIdUtils.*
import logic.enums.IncidentType
import upickle.default.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@upickle.implicits.serializeDefaults( true )
case class Incident( incidentType:     IncidentType,
                     incidentDate:     LocalDate,
                     affectedQuantity: Int,
                     comment:          String,
                     repairCost:       Double,
                     incidentId:       String = randomNanoId()
) derives ReadWriter

object Incident:
   implicit val dateReadWrite: ReadWriter[LocalDate] =
      readwriter[String].bimap[LocalDate](
          dateTime => dateTime.format( DateTimeFormatter.ofPattern( "d-MM-yyyy" ) ),
          str => LocalDate.parse( str, DateTimeFormatter.ofPattern( "d-MM-yyyy" ) )
      )
