import logic.*
import logic.ItemList.{addItem, deleteItem}
import logic.enums.IncidentType.{Damage, Loss}
import logic.enums.ItemStatus
import logic.util.IOHandler.getItemData
import logic.util.{DuplicateItem, ItemDoesNotExist}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.*

import java.io.File
import java.time.LocalDate
import scala.collection.mutable.Buffer

class ItemSpec extends AnyFlatSpec with Matchers:
   val childItem1 = Item( "child1", "child item 1", 10, "valid", 10, 100, 1000 )
   val childItem2 = Item( "child2", "child item 2", 10, "valid", 10, 100, 1000 )

   val validItem = Item( "item1", "a valid test item", 10, "valid", 10, 100, 1000 )

   val invalidItem = Item( "item2", "an invalid test item", 5, "invalid", 10, 100, 1000 )

   val itemImageFile = new File(
       os.pwd.toString + "/src/test/resources/input-images/test_input_image.png"
   )

   val testItems = Buffer.empty[Item]
   val testCategories = Buffer("VALID")

   val testDataFilePath = "src/test/resources/test-data/"
   val testImageFilePath = "src/test/resources/test-images/"

   "addItem" should "add a new item to the item list and save item data in JSON file" in {
      assert(testItems.isEmpty)
      addItem(validItem,
              None,
              itemImageFile,
              testItems,
              testCategories,
              testDataFilePath,
              testImageFilePath
              )

      val image = new File( os.pwd.toString + "/" + testImageFilePath + "item1.png" )

      testItems should have size 1
      testItems should contain(validItem)
      testItems.head.image shouldBe "item1.png"
      image should exist
   }

   it should "add the new item to the item list and to its parent item's children list" in {
      addItem(childItem1,
              Some( validItem.name ),
              itemImageFile,
              testItems,
              testCategories,
              testDataFilePath,
              testImageFilePath
              )

      val image = new File( os.pwd.toString + "/" + testImageFilePath + "item1.png" )

      testItems should have size 2
      testItems should contain(childItem1)
      testItems.find(_.itemId == validItem.itemId).get.children should contain(childItem1.itemId)
      image should exist
   }

   "withAllChildren" should "return a list of the item along with its child items" in {
      validItem.withAllChildren(testItems) should contain allOf ( validItem, childItem1 )
   }

   it should "throw error if the item category doesn't exist" in {
      the[ItemDoesNotExist] thrownBy {
         addItem(invalidItem,
                 None,
                 itemImageFile,
                 testItems,
                 testCategories,
                 testDataFilePath,
                 testImageFilePath
                 )
      } should have message s"Item category invalid doesn't exist in the system"

      testItems should have size 2
   }

   it should "throw error if there is already an active item with the same name" in {
      the[DuplicateItem] thrownBy {
         addItem(validItem,
                 None,
                 itemImageFile,
                 testItems,
                 testCategories,
                 testDataFilePath,
                 testImageFilePath
                 )
      } should have message "item1 already exists in the system"

      testItems should have size 2
   }

   it should "throw error if the given parent item doesn't exist" in {
      the[ItemDoesNotExist] thrownBy {
         addItem(childItem2,
                 Some( "item2" ),
                 itemImageFile,
                 testItems,
                 testCategories,
                 testDataFilePath,
                 testImageFilePath
                 )
      } should have message "Parent item item2 doesn't exist in the system"

      testItems should have size 2
   }

   "deteleItem" should "set an existing item's status to Inactive without actually removing it" in {
      deleteItem(childItem1, testItems, testDataFilePath, testImageFilePath)

      testItems should have size 2
      testItems should contain(childItem1)
      childItem1.status shouldBe ItemStatus.Inactive
      testItems
         .find( _.itemId == validItem.itemId )
         .get
         .children should not contain ( childItem1.itemId )
   }

   it should "throw an error if the item doesn't exist in the item list" in {
      the[ItemDoesNotExist] thrownBy {
         deleteItem(childItem2, testItems, testDataFilePath, testImageFilePath)
      } should have message "child2 does not exist or is not active in the system"
   }

   it should "throw an error if the item is already inactive" in {
      the[ItemDoesNotExist] thrownBy {
         deleteItem(childItem1, testItems, testDataFilePath, testImageFilePath)
      } should have message "child1 does not exist or is not active in the system"

      testItems should contain(childItem1)
   }

   val incident1 = Incident( Damage, LocalDate.of( 2025, 4, 4 ), 1, "test incident damage", 100 )
   val incident2 = Incident( Loss, LocalDate.of( 2025, 3, 3 ), 1, "test incident loss", 100 )

   "reportIncident" should "add a new incident to the item's incident list and adjust lost quantity if needed" in {
      validItem.reportIncident(incident1, testItems, testDataFilePath)
      validItem.reportIncident(incident2, testItems, testDataFilePath)

      validItem.incidents should have size 2
      validItem.incidents should contain allOf ( incident1, incident2 )
      validItem.lostQuantity shouldBe 1
   }

   "incidentsByDates" should "return a list of incidents within a given date range" in {
      validItem.incidentsByDates( null, null ) should contain allOf ( incident1, incident2 )
      validItem.incidentsByDates( LocalDate.of( 2025, 3, 1 ),
                                  LocalDate.of( 2025, 4, 1 )
      ) should contain only ( incident2 )
   }

   "totalExpense" should "calculate the total cost from item's incidents" in {
      validItem.totalExpense( Damage, null, null ) shouldBe 100.0
      validItem.totalExpense( Damage,
                              LocalDate.of( 2025, 3, 1 ),
                              LocalDate.of( 2025, 4, 1 )
      ) shouldBe 0.0
   }

   "getDataFromFile" should "parse items data from items.json file into a list of Item instances" in {
      val newItemList = getItemData( testDataFilePath )
      newItemList should have size 2
      newItemList should contain theSameElementsAs ( testItems )
   }