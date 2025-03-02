package logic

import ujson.IncompleteParseException
import upickle.default._

import scala.collection.mutable.Buffer

object ItemCategory:
   val dataFilePath = "src/main/resources/data/itemCategories.json"

   def getItemCategoryList =
      try
         IOHandler.getDataFromFile("itemCategories").arr.map( value => value.str )
      catch
         case IncompleteParseException( msg ) =>
            Buffer.empty

   def addItemCategory( newCategory: String ) =
      val existingCategories = this.getItemCategoryList
      if !existingCategories.contains( newCategory ) then
         existingCategories += newCategory

         os.write.over( os.pwd / os.RelPath( dataFilePath ), write( existingCategories.toSet ) )
      else throw DuplicateItemCategory( "Category already exists" )