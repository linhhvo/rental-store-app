package logic

import logic.IOHandler.{getDataFromFile, writeSavedItems}
import logic.RentalStore.{ownedItems, updateItemsDataSource}
import ujson.IncompleteParseException
import upickle.default.*

import scala.collection.mutable.Buffer

case class SavedItem( itemId: String, var rentQuantity: Int ) derives ReadWriter:
   def getDetailsFromItemList =
      ownedItems.find( _.itemId == this.itemId )

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
   def addToCart( requestItem: Item, requestQuantity: Int ) =
      // check if the added item already exists in the saved list
      savedItems.find( _.itemId == requestItem.itemId ) match
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
               savedItems += SavedItem( requestItem.itemId, requestQuantity )
               // reduce item's available quantity
               requestItem.reserve( requestQuantity )
            else
               throw ItemNotAvailable(
                   s"Item ${requestItem.name} doesn't have enough quantity for rent"
               )

      updateItemsDataSource() // to update available quantity for the item
      writeSavedItems( savedItems )

   /** check if item is included in the saved item list. If yes, delete item from saved item list.
     * If no, do nothing. Then update the saved list in external file
     */
   def removeFromCart( item: Item ) =
      savedItems.find( _.itemId == item.itemId ) match
         case Some( i ) =>
            savedItems -= i
            writeSavedItems( savedItems )
         case _         => // Do nothing
   /** remove all items from saved list */
   def resetSavedList() =
      savedItems.foreach( si => si.getDetailsFromItemList.foreach( _.free( si.rentQuantity ) ) )
      savedItems = Buffer.empty
      updateItemsDataSource()
      writeSavedItems( Buffer.empty )

   /** method to calculate total rate per hour for all saved items */
   def combinedRatePerHour =
      savedItems
         .map( si =>
            si.getDetailsFromItemList
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerHour * si.rentQuantity ) )
         )
         .sum

   /** method to calculate total rate per day for all saved items */
   def combinedRatePerDay =
      savedItems
         .map( si =>
            si.getDetailsFromItemList
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerDay * si.rentQuantity ) )
         )
         .sum

   /** method to calculate total rate per week for all saved items */
   def combinedRatePerWeek =
      savedItems
         .map( si =>
            si.getDetailsFromItemList
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerWeek * si.rentQuantity ) )
         )
         .sum