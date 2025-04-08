import logic.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.*

import java.time.LocalDate
import scala.collection.mutable.Buffer

/** test methods in Item class */
class ItemSpec extends AnyFlatSpec with Matchers:
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

   "addChildren" should "add a new child item ID to the children list of the target item" in {
      targetItem.addChildren( childItem2.itemId )
      targetItem.children should contain allOf ( childItem1.itemId, childItem2.itemId )
   }

   "deleteChildren" should "remove an existing child item ID from the children list of the target item" in {
      targetItem.deleteChildren( childItem2.itemId )
      targetItem.children should contain only ( childItem1.itemId )
   }

   "getChildrenDetails" should "return item instances of the children items" in {
      targetItem.getChildrenDetails( allItems ) should equal( Set( childItem1 ) )
   }

   "availableQuantity" should "return the total quantity minus lost quantity and rented quantity" in {
      targetItem.availableQuantity shouldBe 4
   }

   "hasEnoughQuantity" should "verify that available quantity is more than requested quantity" in {
      targetItem.hasEnoughQuantity( 5 ) shouldBe false
      targetItem.hasEnoughQuantity( 1 ) shouldBe true
   }

   "reserve" should "increment rented quantity by requested quantity" in {
      targetItem.reserve( 2 )
      targetItem.availableQuantity shouldBe 2
   }

   "free" should "decrement rented quantity by requested quantity" in {
      targetItem.free( 3 )
      targetItem.availableQuantity shouldBe 5
   }

   "addComment" should "add a new comment to target item's comment list" in {
      targetItem.addComment( "good tent " )
      targetItem.comments should contain( "good tent" )
   }

   it should "throw MissingDate exception when comment string is empty" in {
      the[MissingData] thrownBy targetItem.addComment(
          ""
      ) should have message "Comment can't be empty"
   }

   "reportIncident" should "add a new incident to target item's incident list" in {
      val incident1 = Incident( LocalDate.now(), IncidentType.Loss, "lost item", 12.5 )
      val incident2 = Incident( LocalDate.now(), IncidentType.Damage, "damage item", 12.5 )
      targetItem.reportIncident( incident1, 2 )
      targetItem.incidents should contain( incident1 )
      targetItem.availableQuantity shouldBe 3

      targetItem.reportIncident( incident2, 1 )
      targetItem.incidents should contain allOf ( incident1, incident2 )
      targetItem.availableQuantity shouldBe 3
   }

   "withAllChildren" should "return a list of the target item's children and children of children" in {
      childItem1.addChildren( childItem2.itemId )
      targetItem.withAllChildren( allItems ) should ( have size 3 and contain allOf ( targetItem, childItem1,
                                                                                     childItem2
      ) )

   }