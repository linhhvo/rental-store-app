package logic.util

import logic.{Booking, Item, Renter, SavedItem}
import ujson.IncompleteParseException
import upickle.default.*

import scala.collection.mutable.Buffer

object IOHandler:
   val prodDataFilePath = "src/main/resources/data/"
   val prodImageFilePath = "src/main/resources/images/"

   def getDataFromFile( fileName: String, dataFilePath: String ) =
      try ujson.read( os.read( os.pwd / os.RelPath( dataFilePath + fileName + ".json" ) ) ).arr
      catch
         case IncompleteParseException( m ) => Buffer.empty
         case e: Exception =>
            println( e )
            Buffer.empty
   end getDataFromFile

   def getItemData( dataFilePath: String = prodDataFilePath ) =
      try getDataFromFile( "items", dataFilePath ).map( value => read[Item]( value ) )
      catch
         case e: Exception =>
            println( e )
            Buffer.empty

   def getBookingData( dataFilePath: String = prodDataFilePath ) =
      try getDataFromFile( "bookings", dataFilePath ).map( value => read[Booking]( value ) )
      catch
         case e: Exception =>
            println( e )
            Buffer.empty

   def getSavedListData( dataFilePath: String = prodDataFilePath ) =
      try getDataFromFile( "savedItems", dataFilePath ).map( value => read[SavedItem]( value ) )
      catch
         case e: Exception =>
            println( e )
            Buffer.empty

   def getCategoryData( dataFilePath: String = prodDataFilePath ) =
      try getDataFromFile( "itemCategories", dataFilePath ).map( _.str.toUpperCase )
      catch
         case e: Exception =>
            println( e )
            Buffer.empty

   def getRenterData( dataFilePath: String = prodDataFilePath ) =
      try getDataFromFile( "renters", dataFilePath ).map( value => read[Renter]( value ) )
      catch
         case e: Exception =>
            println( e )
            Buffer.empty

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