package ui

import logic.ItemCategory
import logic.ItemCategory.*
import logic.util.{DuplicateItemCategory, InvalidInput, ItemDoesNotExist}
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType.{Confirmation, Information}
import scalafx.scene.layout.VBox
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.util.Helper.displayAlert
import ui.util.Validator.validateShortTextInput

object AddItemCategory:
   private val btnWidth = 125

   /* ADD ITEM CATEGORY PAGE */

   private val addItemCatLabel = new Label:
      text = "Enter new item category"
      font = Font( null, FontWeight( Bold ), 14 )

   // input field for item category name
   private val itemCatTextField = new TextField:
      maxWidth = 200

   // alert pop up to display user message
   private val messageAlert = new Alert( Information )

   // get existing category list and convert to checkbox elements
   private var existingCatList = categoryElements

   // button to add category
   private val addItemCatBtn = new Button:
      text = "Add Category"
      prefWidth = btnWidth
      onAction = (
          ( e ) =>
             try {
                ItemCategory.addItemCategory(
                    validateShortTextInput( itemCatTextField.getText, "Item Category" )
                )
                resetItemCatPage()
             } catch
                case InvalidInput( m )                 => displayAlert( messageAlert, m )
                case DuplicateItemCategory( errorMsg ) =>
                   displayAlert( messageAlert, errorMsg )
      )

   // label for existing category list and instructions for deleting categories
   private val existingCatLabel = new Label:
      text = "Existing categories:"
      font = Font( null, FontWeight( Bold ), 14 )

   private val deleteInstruction = new Label:
      text = "Select category to delete"
      font = Font( null, 12 )

   // group label and isntruction to avoid spacing in between
   private val textContainer = VBox( existingCatLabel, deleteInstruction )

   // group existing category list and delete button
   private val categoryListContainer = new VBox:
      children = Array( textContainer ) ++ existingCatList
      spacing = 5
      margin = Insets( 10, 0, 0, 0 )

   // button to delete category
   private val deleteItemCatBtn = new Button:
      text = "Delete Category"
      prefWidth = btnWidth
      onAction = (
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
                        s"There are ${itemInCategoryCount( c )} items in category $c\n\nThe associated items will also be deleted."
                     else s"There are no item in category $c\n" )
             )

             // check that there are categories selected
             if targetCategories.nonEmpty then
                // display a confirmation alert with messsage warning about related items
                new Alert( Confirmation ):
                   headerText =
                      deleteCatMsg + "\nAre you sure you want to delete selected categories?"
                   buttonTypes = Seq( ButtonType.Yes, ButtonType.No )
                .showAndWait() match
                   // if user selects Yes button, proceed with deletion
                   case Some( ButtonType.Yes ) =>
                      try
                         // delete selected categories
                         targetCategories.foreach( cat => deleteItemCategory( cat ) )
                         displayAlert( messageAlert, "Selected categories have been removed" )
                         // update existing list displayed on GUI
                         existingCatList = categoryElements
                         categoryListContainer.children = Array( textContainer ) ++ existingCatList
                      catch
                         case ItemDoesNotExist( m ) =>
                            displayAlert( messageAlert,
                                          "Selected category doesn't exist in the system"
                            )
                   // if user selects No button, do nothing
                   case _                      => // Do nothing
      )

   // overall container of the page
   val addItemCatContainer = new VBox:
      children = Array( addItemCatLabel,
                        itemCatTextField,
                        addItemCatBtn,
                        categoryListContainer,
                        deleteItemCatBtn
      )
      margin = Insets( 20 )
      spacing = 10
      visible = false

   /** Retrieve list of categories and generate UI elements */
   private def categoryElements = itemCategoryList.map( c => CheckBox( c ) )

   def resetItemCatPage() =
      itemCatTextField.clear()
      existingCatList = categoryElements
      categoryListContainer.children = Array( textContainer ) ++ existingCatList
      if categoryElements.isEmpty then
         categoryListContainer.visible = false
         deleteItemCatBtn.visible = false
      else
         categoryListContainer.visible = true
         deleteItemCatBtn.visible = true