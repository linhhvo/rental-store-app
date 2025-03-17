package logic

import logic.IOHandler.{getDataFromFile, writeSavedItems}
import logic.RentalStore.getAvailableItems
import ujson.IncompleteParseException
import upickle.default.*

import scala.collection.mutable.Buffer

case class SavedItem( itemId: String, var rentQuantity: Int ) derives ReadWriter

object SavedItems:
   private val savedItems =
      try getDataFromFile( "savedItems" ).arr.map( item => read[SavedItem]( item ) )
      catch
         case IncompleteParseException( msg ) =>
            Buffer.empty

   def getSavedItems = savedItems

   def addToCart( requestItem: Item, requestQuantity: Int ) =
      val existingItem =
         savedItems.find( existingItem => existingItem.itemId == requestItem.itemId )
      existingItem match
         case Some( itemValue ) =>
            throw DuplicateItem( "Item has already been added to the saved list" )
         case None              =>
            savedItems += SavedItem( requestItem.itemId, requestQuantity )
            requestItem.dependencies.foreach( d =>
               savedItems += ( SavedItem( d.itemId, requestQuantity ) )
            )

      writeSavedItems( savedItems )

   def resetSavedList() =
      savedItems.clear()
      writeSavedItems( Buffer.empty )

   def rentalRatePerHour =
      val allItems = getAvailableItems
      this.getSavedItems
         .map( savedItem =>
            allItems.find( i => i.itemId == savedItem.itemId ) match
               case Some( i ) => i.ratePerHour * savedItem.rentQuantity
               case None      => 0
         )
         .sum