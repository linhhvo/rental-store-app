import upickle.default._

enum ItemCategory derives ReadWriter:
   case Camping
   case Cooking
   case Hiking
   case Facility
   
   