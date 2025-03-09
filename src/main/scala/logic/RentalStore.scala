package logic

import logic.IOHandler.{getDataFromFile, writeItems}
import ujson.IncompleteParseException
import upickle.default.*

import scala.collection.mutable.Buffer

object RentalStore:
   private val dataResourcePath = "src/main/resources/data"

   private val ownedItems =
      try getDataFromFile( "items" ).arr.map(item => read[Item](item))
      catch
         case IncompleteParseException( msg ) =>
            Buffer.empty

//   private val bookings: Set[Booking] = Set.empty

   private val itemCategoryList: Buffer[String] = ItemCategory.getItemCategoryList

   def addItem( item: Item ) =
      if itemCategoryList.contains( item.category.toLowerCase ) then
         if ownedItems.exists( existingItem => existingItem.name == item.name ) then
            throw DuplicateItem( s"${item.name} already exists in the system." )
         else
            ownedItems +=  item 
            writeItems( ownedItems )
      else
         throw InvalidItemCategory( s"Item category ${item.category} doesn't exist in the system." )

   def deleteItem( item: Item ) =
//      if ownedItems.exists( existingItem => existingItem( "name" ).str == item.name ) then
      val deletedItem =
         ownedItems.find( existingItem => existingItem.itemId == item.itemId )
      deletedItem match
         case Some( itemValue ) =>
            ownedItems -= itemValue
            writeItems( ownedItems )
         case None => throw ItemDoesNotExist( s"${item.name} does not exist in the system." )

   def getAvailableItems = ownedItems
   
   def updateItemComment(targetItem: Item, newCmt: String) =
      ownedItems.find(existingItem => existingItem.itemId == targetItem.itemId) match
         case Some(itemValue) => 
            itemValue.addComment(newCmt)
            writeItems(ownedItems)
         case None => throw ItemDoesNotExist( s"${targetItem.name} does not exist in the system." )


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