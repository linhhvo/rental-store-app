package ui

import logic.{DuplicateItemCategory, ItemCategory}
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color

object AddItemCategory:
   /** ADD ITEM CATEGORY PAGE */

   // Pane for adding item category
   val itemCatTextField = TextField()
   itemCatTextField.maxWidth = 200
   val addItemCatBtn = Button( "Add Category" )
   val actionMsg = Label( null )
   actionMsg.textFill = Color.Red
   val addItemCatContainer =
      VBox( Label( "Enter new item category" ), itemCatTextField, addItemCatBtn, actionMsg )
   addItemCatContainer.margin = Insets( 20 )
   addItemCatContainer.spacing = 10
   addItemCatContainer.setVisible( false )

   // Add action for add item category button
   addItemCatBtn.onAction = (
       ( e ) =>
          if itemCatTextField.getText.nonEmpty then
             try
                ItemCategory.addItemCategory( itemCatTextField.getText )
                actionMsg.setText( s"Category ${itemCatTextField.getText} has been added." )
                itemCatTextField.clear()
             catch
                case DuplicateItemCategory( errorMsg ) =>
                   actionMsg.setText( errorMsg )
                   itemCatTextField.clear()
          else actionMsg.setText( "Category is empty." )
   )