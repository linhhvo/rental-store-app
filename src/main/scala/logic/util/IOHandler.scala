package logic.util

import logic.{Booking, Item, SavedItem}
import ujson.{IncompleteParseException, ParseException}
import upickle.default.*

import java.nio.file.NoSuchFileException
import scala.collection.mutable.Buffer

object IOHandler:
   val prodDataFilePath = "src/main/resources/data/"

   def getDataFromFile( fileName: String, dataFilePath: String = prodDataFilePath ) =
      try ujson.read( os.read( os.pwd / os.RelPath( dataFilePath + fileName + ".json" ) ) ).arr
      catch
         case IncompleteParseException( msg ) =>
            Buffer.empty
         case ParseException( msg, num )      =>
            println( "Data source file does not have the correct format\n" + msg )
            Buffer.empty
         case e: NoSuchFileException =>
            println( "Missing data source file\n" + e )
            Buffer.empty
         case _ =>
            println( "Cannot load data source" )
            Buffer.empty
   end getDataFromFile

   def writeItems( itemList: Iterable[Item], dataFilePath: String = prodDataFilePath ) =
      os.write.over( os.pwd / os.RelPath( dataFilePath ) / "items.json",
                     write( itemList, indent = 2 )
      )

   def writeSavedItems( itemList: Iterable[SavedItem], dataFilePath: String ) =
      os.write.over( os.pwd / os.RelPath( dataFilePath ) / "savedItems.json",
                     write( itemList, indent = 2 )
      )

   def writeBookings( bookingList: Iterable[Booking], dataFilePath: String = prodDataFilePath ) =
      os.write.over( os.pwd / os.RelPath( dataFilePath ) / "bookings.json",
                     write( bookingList, indent = 2 )
      )