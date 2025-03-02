package logic

import upickle.default.*

enum IncidentType derives ReadWriter: 
   case Damage
   case Loss