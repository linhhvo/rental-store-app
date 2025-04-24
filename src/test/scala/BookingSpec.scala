import logic.BookingList.addBooking
import logic.SavedList.*
import logic.util.{BookingDuration, ItemNotAvailable, MissingData}
import logic.{Booking, Item, SavedItem}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import scala.collection.mutable.Buffer

class BookingSpec extends AnyFlatSpec with Matchers:
   val formatter = DateTimeFormatter.ofPattern( "d-MM-yyyy HH:mm" )
   val testDataFilePath = "src/test/resources/test-data/"

   val item1 = Item( "item1", "a valid test item", 10, "valid", 10, 100, 1000 )
   val item2 = Item( "item2", "another valid test item", 10, "valid", 15, 50, 2000 )
   val testItems = Buffer( item1, item2 )

   val testSavedList = Buffer.empty[SavedItem]
   val testBookingList = Buffer.empty[Booking]

   "addToCart" should "add a saved item to the saved list if it's not already included" in {
      assert( testSavedList.isEmpty )
      addToCart( Set( item1, item2 ), 2, testSavedList, testDataFilePath )

      testSavedList should have size 2
      testSavedList.map( _.itemId ) should contain allOf ( item1.itemId, item2.itemId )
   }

   it should "increase the rent quantity of the saved item if it's already included" in {
      assert( testSavedList.size == 2 )
      addToCart( Set( item1 ), 1, testSavedList, testDataFilePath )
      testSavedList should have size 2
      testSavedList.find( _.itemId == item1.itemId ).get.rentQuantity shouldBe 3
   }

   it should "throw an eror if the rent quantity is less than or equal to 0" in {
      the[MissingData] thrownBy {
         addToCart( Set( item1 ), 0, testSavedList, testDataFilePath )
      } should have message "Item quantity cannot be zero"
      testSavedList.find( _.itemId == item1.itemId ).get.rentQuantity shouldBe 3
   }

   "calculateRentalCost" should "calculate the total rental cost for the given duration" in {
      val startDate = LocalDateTime.of( 2025, 4, 4, 10, 0 )
      val endDate = LocalDateTime.of( 2025, 4, 12, 12, 0 )
      val testRentalCost =
         calculateRentalCost( BookingDuration( startDate, endDate ), testSavedList, testItems )
      testRentalCost shouldBe 7520
   }

   val testBooking1 = Booking( testSavedList,
                               LocalDateTime.of( 2025, 4, 4, 10, 0 ),
                               LocalDateTime.of( 2025, 4, 12, 12, 0 ),
                               7520,
                               "testrenter1"
   )

   val testBooking2 = Booking( Buffer( SavedItem( item1.itemId, 1 ) ),
                               LocalDateTime.of( 2025, 4, 11, 10, 0 ),
                               LocalDateTime.of( 2025, 4, 11, 12, 0 ),
                               20,
                               "testrenter1"
   )

   val invalidBooking = Booking( Buffer( SavedItem( item1.itemId, 10 ) ),
                                 LocalDateTime.of( 2025, 4, 10, 10, 0 ),
                                 LocalDateTime.of( 2025, 4, 12, 12, 0 ),
                                 2000,
                                 "testrenter1"
   )

   "addBooking" should "add a new booking to the booking list" in {
      assert( testBookingList.isEmpty )
      addBooking( testBooking1, testBookingList, testItems, testDataFilePath )

      testBookingList should have size 1
   }

   it should "throw an error if an item in the booking doesn't have enough quantity for rent" in {
      the[ItemNotAvailable] thrownBy {
         addBooking( invalidBooking, testBookingList, testItems, testDataFilePath )
      } should have message s"Some item in this bundle is not available for rent"
   }

   "totalIncome" should "calculate the total income from bookings within the given date range" in {
      addBooking( testBooking2, testBookingList, testItems, testDataFilePath )
      item1.totalIncome( LocalDate.of( 2025, 4, 11 ),
                         LocalDate.of( 2025, 4, 13 ),
                         testBookingList,
                         testItems
      ) shouldBe 3380
   }

   "totalDemand" should "calculate the total rented quantity from bookings within the given date range" in {
      item2.totalDemand(LocalDate.of( 2025, 4, 11 ),
                         LocalDate.of( 2025, 4, 13 ),
                         testBookingList) shouldBe 2
   }

   "removeFromCart" should "remove the given item from the saved list" in {
      removeFromCart( item1, testSavedList, testDataFilePath )

      testSavedList should have size 1
      testSavedList.find( _.itemId == item1.itemId ) shouldBe empty
   }