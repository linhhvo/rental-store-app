package logic

import upickle.default.*

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

case class Incident( incidentId:   String = UUID.randomUUID().toString,
                     incidentDate: LocalDateTime,
                     incidentType: IncidentType,
                     comment:      String,
                     repairCost:   Double
) derives ReadWriter

object Incident {
   implicit val dateTimeReadWrite: ReadWriter[LocalDateTime] =
      readwriter[String].bimap[LocalDateTime](
          dateTime => dateTime.format( DateTimeFormatter.ISO_LOCAL_DATE_TIME ),
          str => LocalDateTime.parse( str, DateTimeFormatter.ISO_LOCAL_DATE_TIME )
      )

}
