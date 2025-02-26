import upickle.default._

enum IncidentType derives ReadWriter: 
   case Damage
   case Loss