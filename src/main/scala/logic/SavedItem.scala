package logic

import logic.RentalStore.{inventory, updateItemsDataSource}
import logic.enums.SavedItemStatus
import logic.enums.SavedItemStatus.*
import logic.util.IOHandler.{getDataFromFile, writeSavedItems}
import logic.util.{BookingDuration, MissingData}
import upickle.default.*

import scala.collection.mutable.Buffer

@upickle.implicits.serializeDefaults( true )
case class SavedItem( itemId:              String,
                      var rentQuantity:    Int,
                      var savedItemStatus: SavedItemStatus = Pending
) derives ReadWriter:

   def getDetailsFromItemList( allItemsList: Buffer[Item] = inventory ): Option[Item] =
      allItemsList.find( _.itemId == this.itemId )

   def confirmBooking() =
      this.savedItemStatus = Booked

   def returnFromBooking() =
      this.savedItemStatus = Returned

end SavedItem

object SavedItems:
   // retrieve saved items list from external file
   var savedItems =
      getDataFromFile( "savedItems" ).map( item => read[SavedItem]( item ) )

   /** make saved items list available globally */
   def getSavedItems = savedItems

   /** add item to saved items list to prepare for booking */
   def addToCart( requestItems:    Set[Item],
                  requestQuantity: Int,
                  savedList:       Buffer[SavedItem] = savedItems
   ): Buffer[SavedItem] =
      // check that item quantity is selected properly
      if requestQuantity <= 0 then throw MissingData( "Item quantity cannot be zero" )
      else
         requestItems.foreach( item =>
            // check if the added item already exists in the saved list
            savedList.find( _.itemId == item.itemId ) match
               case Some( i ) =>
                  // add the requested quantity to saved list quantity
                  i.rentQuantity += requestQuantity
               case _         =>
                  // add requested item to the saved list
                  savedList += SavedItem( item.itemId, requestQuantity )

            item.reserve( requestQuantity )
         )

      updateItemsDataSource() // to update available quantity for the item
      savedList

   /** check if item is included in the saved item list. If yes, delete item from saved item list.
     * If no, do nothing. This method is only used when an item is deleted from the system
     */
   def removeFromCart( item: Item, savedList: Buffer[SavedItem] = savedItems ): Buffer[SavedItem] =
      savedList.find( _.itemId == item.itemId ) match
         case Some( i ) =>
            savedList -= i
         case _         => // Do nothing
      savedList

   /** remove all items from saved list */
   def resetSavedList( savedList:    Buffer[SavedItem] = savedItems,
                       allItemsList: Buffer[Item] = inventory
   ): Buffer[SavedItem] =
      savedList.foreach( si =>
         si.getDetailsFromItemList( allItemsList ).foreach( _.free( si.rentQuantity ) )
      )
      updateItemsDataSource()
      writeSavedItems( Buffer.empty )
      Buffer[SavedItem]()

   /** calculate total rate per hour for a list of saved items */
   def combinedRatePerHour( savedList:    Buffer[SavedItem] = savedItems,
                            allItemsList: Buffer[Item] = inventory
   ): Double =
      savedList
         .map( si =>
            si.getDetailsFromItemList( allItemsList )
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerHour * si.rentQuantity ) )
         )
         .sum

   /** calculate total rate per day for a list of saved items */
   def combinedRatePerDay( savedList:    Buffer[SavedItem] = savedItems,
                           allItemsList: Buffer[Item] = inventory
   ): Double =
      savedList
         .map( si =>
            si.getDetailsFromItemList( allItemsList )
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerDay * si.rentQuantity ) )
         )
         .sum

   /** calculate total rate per week for a list of saved items */
   def combinedRatePerWeek( savedList:    Buffer[SavedItem] = savedItems,
                            allItemsList: Buffer[Item] = inventory
   ): Double =
      savedList
         .map( si =>
            si.getDetailsFromItemList( allItemsList )
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerWeek * si.rentQuantity ) )
         )
         .sum

   /** calculate the total rental cost of a list of saved items */
   def calculateRentalCost( duration:  BookingDuration,
                            savedList: Buffer[SavedItem] = savedItems
   ): Double =
      combinedRatePerWeek( savedList ) * duration.durationInWeeks
         + combinedRatePerDay( savedList ) * duration.durationInDays
         + combinedRatePerHour( savedList ) * duration.durationInHours
