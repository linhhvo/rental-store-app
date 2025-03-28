package logic

import logic.IOHandler.*
import ujson.IncompleteParseException
import upickle.default.*

import scala.collection.mutable.Buffer

object RentalStore:
   private val dataResourcePath = "src/main/resources/data"

   private val ownedItems =
      try getDataFromFile( "items" ).arr.map( item => read[Item]( item ) )
      catch
         case IncompleteParseException( msg ) =>
            Buffer.empty

   private val bookings =
      try getDataFromFile( "bookings" ).arr.map( item => read[Booking]( item ) )
      catch
         case IncompleteParseException( msg ) =>
            Buffer.empty

   def getAvailableItems = ownedItems

   def addItem( item: Item ) =
      val itemCategoryList: Buffer[String] = ItemCategory.getItemCategoryList
      println( itemCategoryList )
      if itemCategoryList.contains( item.category.toLowerCase ) then
         if ownedItems.exists( existingItem => existingItem.name == item.name ) then
            throw DuplicateItem( s"${item.name} already exists in the system" )
         else
            ownedItems += item
            writeItems( ownedItems )
      else
         throw InvalidItemCategory( s"Item category ${item.category} doesn't exist in the system" )

   def deleteItem( item: Item ) =
      val deletedItem =
         ownedItems.find( existingItem => existingItem.itemId == item.itemId )
      deletedItem match
         case Some( itemValue ) =>
            ownedItems -= itemValue
            writeItems( ownedItems )
         case None => throw ItemDoesNotExist( s"${item.name} does not exist in the system" )

   def updateItemComment( targetItem: Item, newCmt: String ) =
      val newCmtFormatted = newCmt.trim
      if newCmtFormatted.nonEmpty then
         ownedItems.find( existingItem => existingItem.itemId == targetItem.itemId ) match
            case Some( itemValue ) =>
               itemValue.addComment( newCmtFormatted )
               writeItems( ownedItems )
            case None              =>
               throw ItemDoesNotExist( s"${targetItem.name} does not exist in the system" )
      else throw MissingData( "Comment can't be empty" )

   def updateItemIncident( targetItem: Item, newIncident: Incident ) =
      ownedItems.find( existingItem => existingItem.itemId == targetItem.itemId ) match
         case Some( itemValue ) =>
            itemValue.reportIncident( newIncident )
            writeItems( ownedItems )
         case None              =>
            throw ItemDoesNotExist( s"${targetItem.name} does not exist in the system" )

   def addBooking( newBooking: Booking ) =
      bookings += newBooking
      writeBookings( bookings )

//@main def tempFunc() =
//   val item0 = Item( "tent", "tent for camping", 3, "camping", 2.5 )
//   val item1 = Item( "sleeping bag", "one sleeper bag", 10, "camping", 1.0 )
//   RentalStore.addItem( item1 )
//   RentalStore.addItem( item0 )
//   val incident1 = Incident( LocalDate.parse( "2025-03-09" ), Damage, "damage on the side", 12.0 )
//   RentalStore.updateItemIncident( item1, incident1 )
//   val incident2 = Incident( LocalDate.parse( "2025-03-09" ), Loss, "damage on the side", 12.0 )
//   RentalStore.updateItemIncident( item1, incident2 )