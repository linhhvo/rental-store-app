package logic

import logic.enums.ItemStatus.Active
import logic.RentalStore.inventory
import logic.util.{DuplicateItemCategory, IOHandler, ItemDoesNotExist}
import upickle.default.*

import scala.collection.mutable.Buffer

object ItemCategory:
   val dataFilePath = "src/main/resources/data/itemCategories.json"

   def getItemCategoryList =
      IOHandler.getDataFromFile( "itemCategories" ).map( _.str )

   def addItemCategory( newCategory: String ) =
      val existingCategories = this.getItemCategoryList
      if !existingCategories.contains( newCategory.toLowerCase ) then
         existingCategories += newCategory.toLowerCase

         os.write.over( os.pwd / os.RelPath( dataFilePath ), write( existingCategories.toSet ) )
      else throw DuplicateItemCategory( "Category already exists" )

   def deleteItemCategory( target: String ) =
      val existingCategories = this.getItemCategoryList
      existingCategories.find( _ == target.toLowerCase ) match
         case Some( category ) =>
            existingCategories -= target.toLowerCase
            os.write.over( os.pwd / os.RelPath( dataFilePath ), write( existingCategories.toSet ) )
         case None             =>
            throw ItemDoesNotExist(
                s"Item category $target doesn't exist in the system"
            )

   /** retrieve items that belong to an item category */
   def getItemsInCategory( category: String ): Buffer[Item] =
      inventory.filter( _.category == category )

   def itemInCategoryCount( category: String ): Int =
      this.getItemsInCategory( category ).size