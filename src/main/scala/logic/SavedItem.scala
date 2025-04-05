package logic

import logic.IOHandler.{getDataFromFile, writeSavedItems}
import logic.RentalStore.{ownedItems, updateItemsDataSource}
import logic.SavedItemStatus.*
import ujson.IncompleteParseException
import upickle.default.*

import scala.collection.mutable.Buffer

@upickle.implicits.serializeDefaults( true )
case class SavedItem( itemId:              String,
                      var rentQuantity:    Int,
                      var savedItemStatus: SavedItemStatus = Pending
) derives ReadWriter:

   def getDetailsFromItemList( allItemsList: Buffer[Item] = ownedItems ): Option[Item] =
      allItemsList.find( _.itemId == this.itemId )

   def confirmBooking() =
      this.savedItemStatus = Booked

   def returnFromBooking() =
      this.savedItemStatus = Returned

end SavedItem

object SavedItems:
   // retrieve saved items list from external file
   var savedItems =
      try getDataFromFile( "savedItems" ).arr.map( item => read[SavedItem]( item ) )
      catch
         case IncompleteParseException( msg ) =>
            Buffer.empty

   /** make saved items list available globally */
   def getSavedItems = savedItems

   /** add item to saved items list to prepre for booking */
   def addToCart( requestItem:     Item,
                  requestQuantity: Int,
                  savedList:       Buffer[SavedItem] = savedItems
   ) =
      // check if the added item already exists in the saved list
      savedList.find( _.itemId == requestItem.itemId ) match
         /* if the item is already in the saved list, add the requested quantity to the saved
          * quantity */
         case Some( i ) =>
            // check if item has enough quantity
            if requestItem.hasEnoughQuantity( requestQuantity ) then
               // add the requested quantity to saved list quantity
               i.rentQuantity += requestQuantity
               // reduce item's available quantity
               requestItem.reserve( requestQuantity )
            else
               throw ItemNotAvailable(
                   s"Item ${requestItem.name} doesn't have enough quantity for rent"
               )
         // if the item is not already in the saved list
         case None      =>
            if requestItem.hasEnoughQuantity( requestQuantity ) then
               // add requested item to the saved list
               savedList += SavedItem( requestItem.itemId, requestQuantity )
               // reduce item's available quantity
               requestItem.reserve( requestQuantity )
            else
               throw ItemNotAvailable(
                   s"Item ${requestItem.name} doesn't have enough quantity for rent"
               )

      updateItemsDataSource() // to update available quantity for the item
//      writeSavedItems( savedList )
      savedList

   /** check if item is included in the saved item list. If yes, delete item from saved item list.
     * If no, do nothing. Then update the saved list in external file
     */
   def removeFromCart( item: Item, savedList: Buffer[SavedItem] = savedItems ) =
      savedList.find( _.itemId == item.itemId ) match
         case Some( i ) =>
            savedList -= i
//            writeSavedItems( savedList )
         case _         => // Do nothing
      savedList

   /** remove all items from saved list */
   def resetSavedList( savedList: Buffer[SavedItem] = savedItems ) =
      savedList.foreach( si => si.getDetailsFromItemList().foreach( _.free( si.rentQuantity ) ) )
//      savedList = Buffer.empty
      updateItemsDataSource()
      writeSavedItems( Buffer.empty )
      Buffer[SavedItem]()

   /** method to calculate total rate per hour for all saved items */
   def combinedRatePerHour( savedList: Buffer[SavedItem] = savedItems ): Double =
      savedList
         .map( si =>
            si.getDetailsFromItemList()
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerHour * si.rentQuantity ) )
         )
         .sum

   /** method to calculate total rate per day for all saved items */
   def combinedRatePerDay( savedList: Buffer[SavedItem] = savedItems ): Double =
      savedList
         .map( si =>
            si.getDetailsFromItemList()
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerDay * si.rentQuantity ) )
         )
         .sum

   /** method to calculate total rate per week for all saved items */
   def combinedRatePerWeek( savedList: Buffer[SavedItem] = savedItems ): Double =
      savedList
         .map( si =>
            si.getDetailsFromItemList()
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerWeek * si.rentQuantity ) )
         )
         .sum
