package logic

import logic.IOHandler.{getDataFromFile, writeSavedItems}
import logic.RentalStore.getAllItems
import ujson.IncompleteParseException
import upickle.default.*

import scala.collection.mutable.Buffer

case class SavedItem( itemId: String, var rentQuantity: Int ) derives ReadWriter

object SavedItems:
   // retrieve saved items list from external file
   private val savedItems =
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
            i.rentQuantity += requestQuantity
         // if the item is not already in the saved list
         case None      =>
            // add requested item to the saved list
            savedItems += SavedItem( requestItem.itemId, requestQuantity )
            // add children item, if any, to saved list as well with the same requested quantity
            requestItem.children.foreach( d => savedItems += ( SavedItem( d, requestQuantity ) ) )

      writeSavedItems( savedItems )

   def resetSavedList() =
      savedItems.clear()
      writeSavedItems( Buffer.empty )

   def rentalRatePerHour =
      val allItems = getAllItems
      this.getSavedItems
         .map( savedItem =>
            allItems.find( i => i.itemId == savedItem.itemId ) match
               case Some( i ) => i.ratePerHour * savedItem.rentQuantity
               case None      => 0
         )
         .sum
      
   def checkItemAvailability(requestedItem: Item, requestedQuantity: Int) = 
      