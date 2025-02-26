import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import upickle.default._

case class Incident(date: LocalDateTime, incidentType: IncidentType, comment: String, repairCost: Double) derives ReadWriter

implicit val dateTimeReadWrite: ReadWriter[LocalDateTime] =
   readwriter[String].bimap[LocalDateTime](
      dateTime => dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
      str => LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
   )
