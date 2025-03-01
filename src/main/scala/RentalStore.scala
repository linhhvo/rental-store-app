import ItemCategory.Camping
import upickle.default.*

import scala.collection.mutable.Set

case class RentalStore( ownedItems: Set[Item] = Set.empty, bookings: Set[Booking] = Set.empty )
    derives ReadWriter:
   def addItem( item: Item ) =
      ownedItems += item

   def deleteItem( item: Item ) = ownedItems -= item

   def getAvailableItems = ownedItems

@main def testItem() =
   val item0 = Item( "tent", "tent for camping", 3, Camping, 2.5 )
   val item1 = Item( "sleeping bag", "one sleeper bag", 10, Camping, 1.0 )
   val store = RentalStore()
   store.addItem( item0 )
   store.addItem( item1 )
   println( store.getAvailableItems )
   val jsonItems = ujson.read( os.read( os.pwd / "src/main/resources/data/items.json" ) )
   println( jsonItems.arr( 0 )( "images" )( 0 ).str )
   //   println(item0.saveItemAsJson())
//   println( write( store.ownedItems, indent = 2 ) )
//   os.write.over( os.pwd / "src/main/resources/data/items.json",
//                  write( store.ownedItems, indent = 2 )
//   )