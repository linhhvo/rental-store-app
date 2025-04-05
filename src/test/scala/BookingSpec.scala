import com.aventrix.jnanoid.jnanoid.NanoIdUtils.randomNanoId
import logic.SavedItemStatus.{Booked, Returned}
import logic.SavedItems.addToCart
import logic.{Item, SavedItem}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.Buffer

class BookingSpec extends AnyFlatSpec with Matchers:

   val childItem1 = Item( "sleeping bag", "bag", 10, "camping", 10, 100, 1000 )
   val childItem2 = Item( "tent", "tent", 10, "camping", 10, 100, 1000 )

   val targetItem = Item( "cooking pot",
                          "a good pot",
                          10,
                          "cooking",
                          10,
                          100,
                          1000,
                          children = Set( childItem1.itemId ),
                          lostQuantity = 1,
                          rentedQuantity = 5
   )

   val allItems = Buffer( targetItem, childItem1, childItem2 )

   val savedItem1 = SavedItem( targetItem.itemId, 3 )
   val savedItem2 = SavedItem( childItem1.itemId, 2 )

   val savedList: Buffer[SavedItem] = Buffer.empty

   "getDetailsFromItemList" should "retrieve the item instance from the saved item ID" in {
      val savedItem3 = SavedItem( randomNanoId(), 1 )
      savedItem1.getDetailsFromItemList( allItems ) shouldBe Some( targetItem )
      savedItem3.getDetailsFromItemList( allItems ) shouldBe None
   }

   "confirmBooking" should "change saved item's status to Booked" in {
      savedItem1.confirmBooking()
      savedItem1.savedItemStatus shouldBe Booked
   }

   "returnFromBooking" should "change saved item's status to Returned" in {
      savedItem1.returnFromBooking()
      savedItem1.savedItemStatus shouldBe Returned
   }

   "addToCart" should "add a saved item to the saved list if it isn't already included" in {
      addToCart( targetItem, 3, savedList )
      savedList.length shouldBe 1
      savedList.head.itemId shouldBe targetItem.itemId
   }

   it should "increase a saved item's quantity if the item already exists in saved list" in {
      addToCart( targetItem, 1, savedList )
      savedList.length shouldBe 1
      savedList.head.itemId shouldBe targetItem.itemId
      savedList.head.rentQuantity shouldBe 4
   }