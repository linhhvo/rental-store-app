package logic

import logic.IOHandler.{getDataFromFile, writeSavedItems}
import logic.RentalStore.ownedItems
import ujson.IncompleteParseException
import upickle.default.*

import scala.collection.mutable.Buffer

case class SavedItem( itemId: String, var rentQuantity: Int ) derives ReadWriter

object SavedItems:
   // retrieve saved items list from external file
   val savedItems =
      try getDataFromFile( "savedItems" ).arr.map( item => read[SavedItem]( item ) )
      catch
         case IncompleteParseException( msg ) =>
            Buffer.empty

   /** make saved items list available globally */
   def getSavedItems = savedItems

   /** add item to saved items list to prepre for booking */
   def addToCart( requestItem: Item, requestQuantity: Int ) =
      // check if the added item already exists in the saved list
      savedItems.find( existingItem => existingItem.itemId == requestItem.itemId ) match
         // if the item is already in the saved list, add the requested quantity to the saved quantity
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

      writeSavedItems( savedItems )

   def resetSavedList() =
      savedItems.foreach( si =>
         ownedItems.find( i => i.itemId == si.itemId ).foreach( _.free( si.rentQuantity ) )
      )
      savedItems.clear()
      writeSavedItems( Buffer.empty )

   /** method to calculate total rate per hour for all saved items */
   def combinedRatePerHour =
      this.savedItems
         .map( si =>
            ownedItems
               .find( i => i.itemId == si.itemId )
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerHour * si.rentQuantity ) )
         )
         .sum

   /** method to calculate total rate per day for all saved items */
   def combinedRatePerDay =
      this.savedItems
         .map( si =>
            ownedItems
               .find( i => i.itemId == si.itemId )
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerDay * si.rentQuantity ) )
         )
         .sum

   /** method to calculate total rate per week for all saved items */
   def combinedRatePerWeek =
      this.savedItems
         .map( si =>
            ownedItems
               .find( i => i.itemId == si.itemId )
               .foldLeft( 0.0 )( ( s, i ) => s + ( i.ratePerWeek * si.rentQuantity ) )
         )
         .sum