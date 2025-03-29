package logic

import ujson.IncompleteParseException
import upickle.default.*

import scala.collection.mutable.Buffer

object ItemCategory:
   val dataFilePath = "src/main/resources/data/itemCategories.json"

   def getItemCategoryList =
      try IOHandler.getDataFromFile( "itemCategories" ).arr.map( value => value.str )
      catch
         case IncompleteParseException( msg ) =>
            Buffer.empty

   def addItemCategory( newCategory: String ) =
      val existingCategories = this.getItemCategoryList
      if !existingCategories.contains( newCategory.toLowerCase ) then
         existingCategories += newCategory.toLowerCase

         os.write.over( os.pwd / os.RelPath( dataFilePath ), write( existingCategories.toSet ) )
      else throw DuplicateItemCategory( "Category already exists" )

   def deleteItemCategory( target: String ) =
      val existingCategories = this.getItemCategoryList
      existingCategories.find( c => c == target.toLowerCase ) match
         case Some( category ) =>
            existingCategories -= target.toLowerCase
            os.write.over( os.pwd / os.RelPath( dataFilePath ), write( existingCategories.toSet ) )
         case None             =>
            throw InvalidItemCategory(
                s"Item category ${target} doesn't exist in the system"
            )

   def itemInCategoryCount( category: String ) =
      val ownedItems = RentalStore.getAllItems
      ownedItems.count( item => item.category == category )

   /** retrieve items that belong to an item category */
   def getItemsInCategory( category: String ) =
      val ownedItems = RentalStore.getAllItems
      ownedItems.filter( item => item.category == category )