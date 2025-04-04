package ui

import logic.ItemCategory.*
import logic.RentalStore.deleteItem
import logic.{DuplicateItemCategory, InvalidItemCategory, ItemCategory}
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType.{Confirmation, Information}
import scalafx.scene.layout.VBox
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.Helper.displayAlert

object AddItemCategory:
   private val btnWidth = 125

   /** ADD ITEM CATEGORY PAGE */

   private val addItemCatLabel = Label( "Enter new item category" )
   addItemCatLabel.font = Font( null, FontWeight( Bold ), 14 )

   // New item category text field
   private val itemCatTextField = TextField()
   itemCatTextField.maxWidth = 200

   // Set up alert pop up to display user message
   private val messageAlert = new Alert( Information )

   // get existing category list and convert to checkbox elements
   private var existingCatList = categoryElements

   // Button to add category
   private val addItemCatBtn = Button( "Add Category" )
   addItemCatBtn.prefWidth = btnWidth

   // Button action for add item category button
   addItemCatBtn.onAction = (
       ( e ) =>
          if itemCatTextField.getText.nonEmpty then
             try
                ItemCategory.addItemCategory( itemCatTextField.getText )
                displayAlert( messageAlert,
                              s"Category ${itemCatTextField.getText} has been added."
                )
                /* if successful, clear category text field and update existing category list
                 * displayed on GUI */
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
   private val existingCatLabel = Label( "Existing categories:" )
   existingCatLabel.font = Font( null, FontWeight( Bold ), 14 )

   private val deleteInstruction = Label( "Select category to delete" )
   deleteInstruction.font = Font( null, 12 )

   // Group label and isntruction to avoid spacing in between
   private val textContainer = VBox( existingCatLabel, deleteInstruction )

   // Container for displaying existing category list and delete button
   private val categoryListContainer = new VBox:
      children = Array( textContainer ) ++ existingCatList
      spacing = 5
      margin = Insets( 10, 0, 0, 0 )

   // Button to delete category
   private val deleteItemCatBtn = Button( "Delete Category" )
   deleteItemCatBtn.prefWidth = btnWidth

   deleteItemCatBtn.onAction = (
       ( e ) =>
          val targetCategories = existingCatList
             .filter( _.selected.value ) // keep only the ones selected
             .map( _.text.value ) // get only the category names

          // create a warning message to user when there are items in the selected category
          var deleteCatMsg = ""
          // list the number of items in each category
          targetCategories.foreach( c =>
             deleteCatMsg = deleteCatMsg +
                ( if itemInCategoryCount( c ) > 0 then
                     s"There are ${itemInCategoryCount( c )} items in category $c\n"
                  else s"There are no item in category $c\n" )
          )

          // check that there are categories selected
          if targetCategories.nonEmpty then
             // display a confirmation alert with messsage warning about related items
             new Alert( Confirmation ) {
                headerText =
                   deleteCatMsg + "\nItems belonging to the selected categories will also be deleted. Do you want to proceed?"
                buttonTypes = Seq( ButtonType.Yes, ButtonType.No )
             }.showAndWait() match
                // if user selects Yes button, proceed with deletion
                case Some( ButtonType.Yes ) =>
                   try
                      // delete selected categories
                      targetCategories.foreach( cat =>
                         deleteItemCategory( cat )
                         // delete related items if any
                         if ( itemInCategoryCount( cat ) > 0 ) then
                            getItemsInCategory( cat ).foreach( item => deleteItem( item ) )
                      )
                      displayAlert( messageAlert, "Selected categories have been removed" )
                      // update existing list displayed on GUI
                      existingCatList = categoryElements
                      categoryListContainer.children = Array( textContainer ) ++ existingCatList
                   catch
                      case InvalidItemCategory( m ) =>
                         displayAlert( messageAlert,
                                       "Selected category doesn't exist in the system"
                         )
                // if user selects No button, do nothing
                case _                      => // Do nothing
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
   private def categoryElements = getItemCategoryList.map( c => CheckBox( c ) )

   def resetItemCatPage() =
      itemCatTextField.clear()