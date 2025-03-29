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

   /** make list of all items available globally */
   def getAvailableItems = ownedItems

   /** add new item to the store */
   def addItem( item: Item, parentItem: Option[String] ) =
      // retrieve list of existing item categories
      val itemCategoryList: Buffer[String] = ItemCategory.getItemCategoryList

      // check that selected item category exists in the system
      if !itemCategoryList.contains( item.category.toLowerCase ) then
         throw InvalidItemCategory( s"Item category ${item.category} doesn't exist in the system" )
      else // if category exists
      // check that the same item doesn't already exist in the system
      if ownedItems.exists( existingItem => existingItem.name == item.name ) then
         throw DuplicateItem( s"${item.name} already exists in the system" )
      else // if item doesn't already exist
         // parent item is not required but if an item is passed, check that parent item exists in the system
         parentItem match
            // if parent item is passed
            case Some( parent ) =>
               // find parent item in items list
               ownedItems.find( i => i.name == parent ) match
                  // if parent item found, add new item to items list and to parent's dependencies list
                  case Some( pi ) =>
                     ownedItems += item
                     pi.addChildren( item.itemId )
                     writeItems( ownedItems )
                  // if parent item not found
                  case None       =>
                     throw ItemDoesNotExist(
                         s"Parent item ${parent} doesn't exist in the system"
                     )
            // if no parent item is passed, only add new item to items list
            case None           =>
               ownedItems += item
               writeItems( ownedItems )

   /** delete item from store */
   def deleteItem( item: Item ) =
      // check that item exists in the system before deletion
      ownedItems.find( _.itemId == item.itemId ) match
         // if item exists, remove item from items list and from parent item dependency if any
         case Some( di ) =>
            ownedItems -= di
            // check if deleted item is any other item's child
            ownedItems.find( _.children.exists( _ == di.itemId ) ) match
               case Some( pi ) => // if parent item found, delete item from children list
                  pi.deleteChildren( di.itemId )
                  writeItems( ownedItems )
                  // if parent item not found, only update items list
               case None       => writeItems( ownedItems )
         // if item is not found in the items list, throw error
         case None       => throw ItemDoesNotExist( s"${item.name} does not exist in the system" )

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