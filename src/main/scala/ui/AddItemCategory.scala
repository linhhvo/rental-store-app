package ui

import logic.ItemCategory.{deleteItemCategory, getItemCategoryList}
import logic.{DuplicateItemCategory, InvalidItemCategory, ItemCategory}
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType.Information
import scalafx.scene.layout.VBox
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.Helper.displayAlert

object AddItemCategory:
   /** ADD ITEM CATEGORY PAGE */

   val addItemCatLabel = Label( "Enter new item category" )
   addItemCatLabel.font = Font( null, FontWeight( Bold ), 14 )

   // New item category text field
   val itemCatTextField = TextField()
   itemCatTextField.maxWidth = 200

   // Button to add category
   val addItemCatBtn = Button( "Add Category" )

   // Set up alert pop up to display user message
   val messageAlert = new Alert( Information )

   // get existing category list and convert to checkbox elements
   var existingCatList = categoryElements

   // Button action for add item category button
   addItemCatBtn.onAction = (
       ( e ) =>
          if itemCatTextField.getText.nonEmpty then
             try
                ItemCategory.addItemCategory( itemCatTextField.getText )
                displayAlert( messageAlert,
                              s"Category ${itemCatTextField.getText} has been added."
                )
                // if successful, clear category text field and update existing category list displayed on GUI
                itemCatTextField.clear()
                existingCatList = categoryElements
                categoryListContainer.children = Array( textContainer ) ++ existingCatList
             catch
                case DuplicateItemCategory( errorMsg ) =>
                   displayAlert( messageAlert, errorMsg )
                   itemCatTextField.clear()
          else displayAlert( messageAlert, "Category cannot be empty." )
   )

   // label for existing category list and instructions for deleting categories
   val existingCatLabel = Label( "Existing categories:" )
   existingCatLabel.font = Font( null, FontWeight( Bold ), 14 )

   val deleteInstruction = Label( "Select category to delete" )
   deleteInstruction.font = Font( null, 12 )

   // Group label and isntruction to avoid spacing in between
   val textContainer = VBox( existingCatLabel, deleteInstruction )

   // Container for displaying existing category list and delete button
   val categoryListContainer = new VBox:
      children = Array( textContainer ) ++ existingCatList
      spacing = 5
      margin = Insets( 10, 0, 0, 0 )

   // Button to delete category
   val deleteItemCatBtn = Button( "Delete Category" )
   deleteItemCatBtn.onAction = (
       ( e ) =>
          val targetCategories = existingCatList
             .filter( _.selected.value ) // keep only the ones selected
             .map( _.text.value ) // get only the category names

          if targetCategories.nonEmpty then // check that there are categories selected
             try
                targetCategories.foreach( cat =>
                   deleteItemCategory( cat )
                ) // delete selected categories
                displayAlert( messageAlert, "Selected categories have been removed" )
                // update existing list displayed on GUI
                existingCatList = categoryElements
                categoryListContainer.children = Array( textContainer ) ++ existingCatList
             catch
                case InvalidItemCategory( m ) =>
                   displayAlert( messageAlert, "Selected category doesn't exist in the system" )
   )

   // Overall container of the page
   val addItemCatContainer =
      VBox( addItemCatLabel,
            itemCatTextField,
            addItemCatBtn,
            categoryListContainer,
            deleteItemCatBtn
      )
   addItemCatContainer.margin = Insets( 20 )
   addItemCatContainer.spacing = 10
   addItemCatContainer.setVisible( false )

   // retrieve list of categories and generate UI elements
   def categoryElements = getItemCategoryList.map( c => CheckBox( c ) )
