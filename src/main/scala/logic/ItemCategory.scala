package logic

import logic.Inventory.{activeItems, deleteItem}
import logic.util.{DuplicateItemCategory, IOHandler, ItemDoesNotExist}
import upickle.default.*

import scala.collection.mutable.Buffer

object ItemCategory:
   val dataFilePath = "src/main/resources/data/itemCategories.json"

   val itemCategoryList =
      IOHandler.getDataFromFile( "itemCategories" ).map( _.str.toUpperCase )

   def addItemCategory( newCategory: String ) =
      if !itemCategoryList.contains( newCategory.toUpperCase ) then
         itemCategoryList += newCategory.toUpperCase

         os.write.over( os.pwd / os.RelPath( dataFilePath ), write( itemCategoryList.toSet ) )
      else throw DuplicateItemCategory( "Category already exists" )

   def deleteItemCategory( targetCategory: String ) =
      itemCategoryList.find( _ == targetCategory.toUpperCase ) match
         case Some( category ) =>
            itemCategoryList -= targetCategory.toUpperCase
            // delete related items if any
            if ( itemInCategoryCount( targetCategory ) > 0 ) then
               getItemsInCategory( targetCategory ).foreach( item => deleteItem( item ) )
            os.write.over( os.pwd / os.RelPath( dataFilePath ), write( itemCategoryList.toSet ) )
         case None             =>
            throw ItemDoesNotExist(
                s"Item category $targetCategory doesn't exist in the system"
            )

   /** Get active items that belong to an item category */
   def getItemsInCategory( category: String ): Buffer[Item] =
      activeItems.filter( i => i.category == category )

   def itemInCategoryCount( category: String ): Int =
      this.getItemsInCategory( category ).size