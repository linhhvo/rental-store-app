import upickle.default.*

import scala.collection.mutable.{Buffer, Set}

case class RentalStore( ownedItems: Set[Item] = Set.empty, bookings: Set[Booking] = Set.empty )
    derives ReadWriter:
   private val itemCategoryList: Buffer[String] = ItemCategory.getItemCategoryList

   def addItem( item: Item ) =
      if itemCategoryList.contains( item.category )
      then ownedItems += item
      else throw InvalidItemCategory( "Item category doesn't exist in the system" )

   def deleteItem( item: Item ) = ownedItems -= item

   def getAvailableItems = ownedItems