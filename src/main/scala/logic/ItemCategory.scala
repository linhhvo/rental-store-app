package logic

import logic.ItemList.{activeItems, deleteItem, getItemsByCategory, itemCountByCategory}
import logic.util.IOHandler.{getCategoryData, prodDataFilePath, writeCategoryData}
import logic.util.{DuplicateItemCategory, IOHandler, ItemDoesNotExist}

import scala.collection.mutable.Buffer

object ItemCategory:

   val itemCategoryList = getCategoryData()

   def addItemCategory( newCategory:  String,
                        categoryList: Buffer[String] = itemCategoryList,
                        dataFilePath: String = prodDataFilePath
   ) =
      if !categoryList.contains( newCategory.toUpperCase ) then
         categoryList += newCategory.toUpperCase
         writeCategoryData( categoryList, dataFilePath )
      else throw DuplicateItemCategory( "Category already exists" )

   def deleteItemCategory( targetCategory: String,
                           categoryList:   Buffer[String] = itemCategoryList,
                           dataFilePath:   String = prodDataFilePath
   ) =
      categoryList.find( _ == targetCategory.toUpperCase ) match
         case Some( category ) =>
            categoryList -= targetCategory.toUpperCase
            // delete related items if any
            if ( itemCountByCategory(targetCategory) > 0 ) then
               getItemsByCategory(targetCategory).foreach(item => deleteItem(item))
            writeCategoryData( categoryList, dataFilePath )
         case None             =>
            throw ItemDoesNotExist(
                s"Item category $targetCategory doesn't exist in the system"
            )
