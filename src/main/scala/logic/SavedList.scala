package logic

import logic.RentalStore.inventory
import logic.util.IOHandler.{getDataFromFile, prodDataFilePath, writeSavedItems}
import logic.util.{BookingDuration, MissingData}
import upickle.default.read

import scala.collection.mutable.Buffer

object SavedList:
   // retrieve saved items list from external file
   var savedItems =
      getDataFromFile( "savedItems" ).map( item => read[SavedItem]( item ) )

   /** Add item to saved list to prepare for booking */
   def addToCart( requestItems:    Set[Item],
                  requestQuantity: Int,
                  savedList:       Buffer[SavedItem] = savedItems,
                  dataFilePath:    String = prodDataFilePath
   ) =
      // check that item quantity is selected properly
      if requestQuantity <= 0 then throw MissingData( "Item quantity cannot be zero" )
      else
         requestItems.foreach( item =>
            // check if the added item already exists in the saved list
            savedList.find( _.itemId == item.itemId ) match
               case Some( i ) =>
                  // increase the saved item quantity by the requested quantity
                  i.rentQuantity += requestQuantity
               case _         =>
                  // add requested item to the saved list
                  savedList += SavedItem( item.itemId, requestQuantity )
         )
      writeSavedItems( savedList, dataFilePath )

   /** Remove a single item from the saved list when the item is hidden from the UI. */
   def removeFromCart( item:         Item,
                       savedList:    Buffer[SavedItem] = savedItems,
                       dataFilePath: String = prodDataFilePath
   ) =
      savedList.find( _.itemId == item.itemId ) match
         case Some( i ) =>
            savedList -= i
         case _         => // Do nothing
      writeSavedItems( savedList, dataFilePath )

   /** Remove all items from the saved list */
   def resetSavedList( dataFilePath: String = prodDataFilePath ): Buffer[SavedItem] =
      writeSavedItems( Buffer.empty, dataFilePath )
      Buffer[SavedItem]()

   /** Calculate the total rate per hour for a list of saved items */
   def combinedRatePerHour( savedList:    Buffer[SavedItem] = savedItems,
                            allItemsList: Buffer[Item] = inventory
   ): Double =
      savedList
         .map( si =>
            si.getDetailsFromItemList( allItemsList )
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerHour * si.rentQuantity ) )
         )
         .sum

   /** Calculate the total rate per day for a list of saved items */
   def combinedRatePerDay( savedList:    Buffer[SavedItem] = savedItems,
                           allItemsList: Buffer[Item] = inventory
   ): Double =
      savedList
         .map( si =>
            si.getDetailsFromItemList( allItemsList )
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerDay * si.rentQuantity ) )
         )
         .sum

   /** Calculate the total rate per week for a list of saved items */
   def combinedRatePerWeek( savedList:    Buffer[SavedItem] = savedItems,
                            allItemsList: Buffer[Item] = inventory
   ): Double =
      savedList
         .map( si =>
            si.getDetailsFromItemList( allItemsList )
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerWeek * si.rentQuantity ) )
         )
         .sum

   /** Calculate the total rental cost for a list of saved items */
   def calculateRentalCost( duration:  BookingDuration,
                            savedList: Buffer[SavedItem] = savedItems
   ): Double =
      combinedRatePerWeek( savedList ) * duration.durationInWeeks
         + combinedRatePerDay( savedList ) * duration.durationInDays
         + combinedRatePerHour( savedList ) * duration.durationInHours
