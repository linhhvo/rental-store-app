package logic

import upickle.default.*

object IOHandler:
   val dataFilePath = "src/main/resources/data/"

   def getDataFromFile( fileName: String ) =
      ujson.read( os.read( os.pwd / os.RelPath( dataFilePath + fileName + ".json" ) ) )

   def writeItems( itemList: Iterable[Item] ) =
      os.write.over( os.pwd / os.RelPath( dataFilePath ) / "items.json",
                     write( itemList, indent = 2 )
      )

   def writeSavedItems( itemList: Iterable[SavedItem] ) =
      os.write.over( os.pwd / os.RelPath( dataFilePath ) / "savedItems.json",
                     write( itemList, indent = 2 )
      )
      
   def writeBookings( bookingList: Iterable[Booking] ) =
      os.write.over( os.pwd / os.RelPath( dataFilePath ) / "bookings.json",
                     write( bookingList, indent = 2 )
      )