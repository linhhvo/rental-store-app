package logic

import logic.IOHandler.{getDataFromFile, writeItems}
import ujson.IncompleteParseException
import upickle.default.*

import scala.collection.mutable.Buffer

object RentalStore:
   private val dataResourcePath = "src/main/resources/data"

   private val ownedItems =
      try getDataFromFile( "items" ).arr
      catch
         case IncompleteParseException( msg ) =>
            Buffer.empty

//   private val bookings: Set[Booking] = Set.empty

   private val itemCategoryList: Buffer[String] = ItemCategory.getItemCategoryList

   def addItem( item: Item ) =
      if itemCategoryList.contains( item.category.toLowerCase ) then
         if ownedItems.exists( existingItem => existingItem( "name" ).str == item.name ) then
            throw DuplicateItem( s"${item.name} already exists in the system." )
         else
            ownedItems += writeJs( item )
            writeItems( ownedItems )
      else
         throw InvalidItemCategory( s"Item category ${item.category} doesn't exist in the system." )

   def deleteItem( item: Item ) =
//      if ownedItems.exists( existingItem => existingItem( "name" ).str == item.name ) then
      val deletedItem =
         ownedItems.find( existingItem => existingItem( "itemId" ).str == item.itemId )
      deletedItem match
         case Some( itemValue ) =>
            ownedItems -= writeJs( itemValue )
            writeItems( ownedItems )
         case None => throw ItemDoesNotExist( s"${item.name} does not exist in the system." )

   def getAvailableItems = ownedItems

@main def tempFunc() =
   val item0 = Item( "tent", "tent for camping", 3, "camping", 2.5 )
   val item1 = Item( "sleeping bag", "one sleeper bag", 10, "camping", 1.0 )
   RentalStore.addItem( item1 )
   println( RentalStore.getAvailableItems )
   RentalStore.addItem( item0 )
   println( RentalStore.getAvailableItems )
   RentalStore.deleteItem( item1 )
   println( RentalStore.getAvailableItems )

//   println( RentalStore.getAvailableItems.find(item => item("name").str == "tent") )