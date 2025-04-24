import com.aventrix.jnanoid.jnanoid.NanoIdUtils.randomNanoId
import logic.SavedList.*
import logic.enums.SavedItemStatus.{Booked, Returned}
import logic.util.ItemNotAvailable
import logic.{Item, SavedItem}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.Buffer

class SavedItemSpec extends AnyFlatSpec with Matchers:

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
                          lostQuantity = 1
   )

   val allItems = Buffer( targetItem, childItem1, childItem2 )

   val savedItem1 = SavedItem( targetItem.itemId, 3 )
   val savedItem2 = SavedItem( childItem1.itemId, 3 )

   val savedList: Buffer[SavedItem] = Buffer()

//   "getDetailsFromItemList" should "retrieve the item instance from the saved item ID" in {
//      val savedItem3 = SavedItem( randomNanoId(), 1 )
//      savedItem1.getDetailsFromItemList( allItems ) shouldBe Some( targetItem )
//      savedItem3.getDetailsFromItemList( allItems ) shouldBe None
//   }
//
//   "confirmBooking" should "change saved item's status to Booked" in {
//      savedItem1.confirmBooking()
//      savedItem1.savedItemStatus shouldBe Booked
//   }
//
//   "returnFromBooking" should "change saved item's status to Returned" in {
//      savedItem1.returnFromBooking()
//      savedItem1.savedItemStatus shouldBe Returned
//   }
//
//   "addToCart" should "add a saved item to the saved list if it isn't already included" in {
//      addToCart( Set( targetItem, childItem1 ), 3, savedList ) should have size 2
//      savedList should contain allOf ( SavedItem( targetItem.itemId, 3 ),
//                                       SavedItem( childItem1.itemId, 3 )
//      )
//   }
//
//   it should "decrease target item's available quantity" in {
//      targetItem.availableQuantity shouldBe 1
//   }
//
//   it should "increase a saved item's quantity if the item already exists in saved list" in {
//      addToCart( Set( targetItem ), 1, savedList ) should have size 2
//      savedList.find( _.itemId == targetItem.itemId ).get.rentQuantity shouldBe 4
//   }
//
//   it should "decrease target item's available quantity further" in {
//      targetItem.availableQuantity shouldBe 0
//   }
//
//   it should "throw exception if the target item doesn't have enough quantity for rent" in {
//      the[ItemNotAvailable] thrownBy {
//         addToCart( Set( targetItem ), 1, savedList )
//      } should have message s"Some item in this bundle doesn't have enough quantity for rent"
//   }
//
//   "combinedRatePerHour" should "calculate the total rate per hour for all items in the saved list" in {
//      combinedRatePerHour( savedList, allItems ) shouldBe 70
//   }
//
//   "combinedRatePerDay" should "calculate the total rate per day for all items in the saved list" in {
//      combinedRatePerDay( savedList, allItems ) shouldBe 700
//   }
//
//   "combinedRatePerWeek" should "calculate the total rate per week for all items in the saved list" in {
//      combinedRatePerWeek( savedList, allItems ) shouldBe 7000
//   }
//
//   "removeFromCart" should "keep the saved list inact if the target item is not in the saved list" in {
//      removeFromCart( childItem2, savedList ) should have size 2
//      savedList should contain allOf ( SavedItem( targetItem.itemId, 4 ), savedItem2 )
//   }
//
//   it should "remove target item from saved list if it exists in the saved list" in {
//      removeFromCart( childItem1, savedList ) should have size 1
//      savedList should contain only ( SavedItem( targetItem.itemId, 4 ) )
//   }
//
//   "resetSavedList" should "remove all items from saved list and increase removed items' available quantity" in {
//      resetSavedList( savedList, allItems ) shouldBe empty
//      targetItem.availableQuantity shouldBe 4
//   }