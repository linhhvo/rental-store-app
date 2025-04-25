package logic.util

import logic.{Booking, Item, Renter, SavedItem}
import ujson.{IncompleteParseException, ParseException}
import upickle.default.*

import java.nio.file.NoSuchFileException
import scala.collection.mutable.Buffer

object IOHandler:
   val prodDataFilePath = "src/main/resources/data/"
   val prodImageFilePath = "src/main/resources/images/"

   def getDataFromFile( fileName: String, dataFilePath: String ) =
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

   def getItemData( dataFilePath: String = prodDataFilePath ) =
      getDataFromFile( "items", dataFilePath ).map( value => read[Item]( value ) )

   def getBookingData( dataFilePath: String = prodDataFilePath ) =
      getDataFromFile( "bookings", dataFilePath ).map( value => read[Booking]( value ) )

   def getSavedListData( dataFilePath: String = prodDataFilePath ) =
      getDataFromFile( "savedItems", dataFilePath ).map( value => read[SavedItem]( value ) )

   def getCategoryData( dataFilePath: String = prodDataFilePath ) =
      getDataFromFile( "itemCategories", dataFilePath ).map( _.str.toUpperCase )

   def getRenterData( dataFilePath: String = prodDataFilePath ) =
      getDataFromFile( "renters", dataFilePath ).map( value => read[Renter]( value ) )

   def writeItemData( itemList: Iterable[Item], dataFilePath: String ) =
      os.write.over( os.pwd / os.RelPath( dataFilePath ) / "items.json",
                     write( itemList, indent = 2 )
      )

   def writeSavedListData( itemList: Iterable[SavedItem], dataFilePath: String ) =
      os.write.over( os.pwd / os.RelPath( dataFilePath ) / "savedItems.json",
                     write( itemList, indent = 2 )
      )

   def writeBookingData( bookingList: Iterable[Booking], dataFilePath: String ) =
      os.write.over( os.pwd / os.RelPath( dataFilePath ) / "bookings.json",
                     write( bookingList, indent = 2 )
      )

   def writeCategoryData( categoryList: Iterable[String], dataFilePath: String ) =
      os.write.over( os.pwd / os.RelPath( dataFilePath ) / "itemCategories.json",
                     write( categoryList.toSet )
      )