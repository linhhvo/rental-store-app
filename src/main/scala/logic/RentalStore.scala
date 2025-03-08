package logic

import ujson.IncompleteParseException
import upickle.default.*

import scala.collection.mutable.Buffer

object RentalStore:
   private val dataResourcePath = "src/main/resources/data"

   private val ownedItems =
      try ujson.read( os.read( os.pwd / os.RelPath( dataResourcePath ) / "items.json" ) ).arr
      catch
         case IncompleteParseException( msg ) =>
            Buffer.empty

//   private val bookings: Set[Booking] = Set.empty

   private val itemCategoryList: Buffer[String] = ItemCategory.getItemCategoryList

   def addItem( item: Item ) =
      if itemCategoryList.contains( item.category ) then
         if ownedItems.exists( existingItem => existingItem( "name" ).str == item.name ) then
            throw DuplicateItem( s"${item.name} already exists in the system." )
         else
            ownedItems += writeJs( item )
            os.write.over( os.pwd / os.RelPath( dataResourcePath ) / "items.json",
                           write( ownedItems, indent = 2 )
            )
      else
         throw InvalidItemCategory( s"Item category ${item.category} doesn't exist in the system." )

   def deleteItem( item: Item ) =
      if ownedItems.exists( existingItem => existingItem( "name" ).str == item.name ) then
         ownedItems -= writeJs( item )
         os.write.over( os.pwd / os.RelPath( dataResourcePath ) / "items.json",
                        write( ownedItems.toSet, indent = 2 )
         )
      else throw ItemDoesNotExist( s"${item.name} does not exist in the system." )

   def getAvailableItems = ownedItems

@main def tempFunc() =
   val item0 = Item( "tent", "tent for camping", 3, "Camping", 2.5 )
   val item1 = Item( "sleeping bag", "one sleeper bag", 10, "Camping", 1.0 )
   RentalStore.addItem( item1 )
   RentalStore.addItem( item0 )
   RentalStore.deleteItem( item1 )
//   println( RentalStore.getAvailableItems.find(item => item("name").str == "tent") )