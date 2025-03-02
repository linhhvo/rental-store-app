import ujson.IncompleteParseException
import upickle.default.*

import scala.collection.mutable.Buffer

object ItemCategory:
   val dataFilePath = getClass.getResource( "/data/itemCategories.json" ).getPath

   def getItemCategoryList =
      try
         ujson
            .read( os.read( os.Path( dataFilePath ) ) )
            .arr
            .map( value => value.str )
      catch
         case IncompleteParseException( msg ) =>
            println( msg )
            Buffer.empty

   def addItemCategory( newCategory: String ) =
      val existingCategories = this.getItemCategoryList
      if !existingCategories.contains( newCategory ) then
         existingCategories += newCategory

         os.write.over( os.Path( dataFilePath ), write( existingCategories.toSet ) )
      else throw DuplicateItemCategory( "Category already exists" )