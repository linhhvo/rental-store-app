package logic

import logic.ItemCategory.itemCategoryList
import logic.SavedList.removeFromCart
import logic.enums.ItemStatus.Active
import logic.util.IOHandler.*
import logic.util.{DuplicateItem, ItemDoesNotExist}
import upickle.default.*

import java.io.File
import scala.collection.mutable.Buffer

object ItemList:
   val inventory = getItemData()

   /** Get only active items */
   def activeItems( itemList: Buffer[Item] = inventory ) =
      itemList.filter( i => i.status == Active )

   /** Add new item to inventory list */
   def addItem( item:           Item,
                parentItemName: Option[String],
                imageFile:      File,
                itemList:       Buffer[Item] = inventory,
                categoryList:   Buffer[String] = itemCategoryList,
                dataFilePath:   String = prodDataFilePath,
                imageFilePath:  String = prodImageFilePath
   ) = {
      var validItem = false
      // check that the selected item category exists in the system
      if !categoryList.contains( item.category.toUpperCase ) then
         throw ItemDoesNotExist( s"Item category ${item.category} doesn't exist in the system" )
      else
         // check that the same item doesn't already exist in the system
         if activeItems( itemList ).exists( _.name.trim.toLowerCase == item.name.trim.toLowerCase )
         then throw DuplicateItem( s"${item.name} already exists in the system" )
         else
            /* parent item is not required but if an item is selected, check that the parent item
             * exists in the system and is active */
            parentItemName match
               case Some( parent ) =>
                  activeItems( itemList ).find( _.name == parent ) match
                     /* if parent item found, add new item to items list and to parent's
                      * dependencies list */
                     case Some( pi ) =>
                        validItem = true
                        pi.addChildren( item.itemId )
                     // if parent item not found
                     case None       =>
                        throw ItemDoesNotExist(
                            s"Parent item $parent doesn't exist in the system"
                        )
               // if no parent item is selected, only add new item to items list
               case None           => validItem = true

      if validItem then
         item.image = item.name.toLowerCase + ".png"
         itemList += item
         writeItemData( itemList, dataFilePath )
         os.copy.over( os.Path( imageFile ), os.pwd / os.RelPath( imageFilePath + item.image ) )

   }

   /** Delete item from inventory list */
   def deleteItem( item:          Item,
                   itemList:      Buffer[Item] = inventory,
                   dataFilePath:  String = prodDataFilePath,
                   imageFilePath: String = prodImageFilePath
   ) =
      activeItems( itemList ).find( _.itemId == item.itemId ) match
         /* if item exists and is active, set item status to Inactive and remove from parent item
          * dependency if any */
         case Some( di ) =>
            di.hideItem()
            // remove associated item image from resources folder
            if di.image != "" then
               try
                  os.remove( os.pwd / os.RelPath( imageFilePath + di.image ) )
                  di.image = ""
               catch case _ => println( "cannot remove item image" )

            // check if deleted item is any other item's child
            itemList.find( _.children.contains( di.itemId ) ) match
               case Some( pi ) => // if parent item found, delete item from children list
                  pi.deleteChildren( di.itemId )
               case None       => // Do nothing
            // update items list in external file
            writeItemData( itemList, dataFilePath )

            // remove item from saved list if it was added to saved list before deleting
            removeFromCart( di )

         // if item is not found in the items list, throw error
         case None =>
            throw ItemDoesNotExist( s"${item.name} does not exist or is not active in the system" )

   /** Get active items that belong to an item category */
   def getItemsByCategory( category: String ): Buffer[Item] =
      activeItems().filter( i => i.category == category )

   def itemCountByCategory( category: String ): Int =
      this.getItemsByCategory( category ).size