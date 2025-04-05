package logic

import logic.IOHandler.*
import logic.SavedItems.{removeFromCart, savedItems}
import ujson.IncompleteParseException
import upickle.default.*

import scala.collection.mutable.Buffer

object RentalStore:

   val ownedItems =
      try getDataFromFile( "items" ).arr.map( item => read[Item]( item ) )
      catch
         case IncompleteParseException( msg ) =>
            Buffer.empty

   val bookings =
      try getDataFromFile( "bookings" ).arr.map( item => read[Booking]( item ) )
      catch
         case IncompleteParseException( msg ) =>
            Buffer.empty

   /** make list of all items available globally */
   def getAllItems = ownedItems

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
         /* parent item is not required but if an item is passed, check that parent item exists in
          * the system */
         parentItem match
            // if parent item is passed
            case Some( parent ) =>
               // find parent item in items list
               ownedItems.find( i => i.name == parent ) match
                  /* if parent item found, add new item to items list and to parent's dependencies
                   * list */
                  case Some( pi ) =>
                     ownedItems += item
                     pi.addChildren( item.itemId )
                     updateItemsDataSource()
                  // if parent item not found
                  case None       =>
                     throw ItemDoesNotExist(
                         s"Parent item $parent doesn't exist in the system"
                     )
            // if no parent item is passed, only add new item to items list
            case None           =>
               ownedItems += item
               updateItemsDataSource()

   /** delete item from store */
   def deleteItem( item: Item ) =
      // check that item exists in the system before deletion
      ownedItems.find( _.itemId == item.itemId ) match
         // if item exists, remove item from items list and from parent item dependency if any
         case Some( di ) =>
            di.hideItem()

            // remove associated item image from resources folder
            os.remove(os.pwd / os.RelPath("src/main/resources/images/" + di.image))

            // check if deleted item is any other item's child
            ownedItems.find( _.children.contains( di.itemId ) ) match
               case Some( pi ) => // if parent item found, delete item from children list
                  pi.deleteChildren( di.itemId )
                  updateItemsDataSource()
                  // if parent item not found, only update items list
               case None       => updateItemsDataSource()

            // remove item from saved list if it was added to saved list before deleting
            savedItems = removeFromCart( di )
            writeSavedItems(savedItems)

         // if item is not found in the items list, throw error
         case None => throw ItemDoesNotExist( s"${item.name} does not exist in the system" )

   /** record rental booking */
   def addBooking( newBooking: Booking ) =
      bookings += newBooking
      newBooking.items.foreach( bi =>
         ownedItems.find( i => i.itemId == bi.itemId ).foreach( _.reserve( bi.rentQuantity ) )
         bi.confirmBooking()
      )
      updateItemsDataSource()
      writeBookings( bookings )

   /** update items list in external file */
   def updateItemsDataSource() = writeItems( ownedItems )