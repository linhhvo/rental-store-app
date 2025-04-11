package logic.enums

import upickle.default.*

enum IncidentType derives ReadWriter: 
   case Damage
   case Loss